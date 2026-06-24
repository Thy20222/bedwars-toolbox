package dev.thy.bedwarstoolbox.core.stats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public final class BedwarsStatsService {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(6);
    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Long> FAILED_UNTIL = new ConcurrentHashMap<>();
    private static final Set<String> BEDWARS_PLAYERS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<String> IN_FLIGHT = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<String, String> URCHIN_KEYS = new ConcurrentHashMap<>();
    private static final Map<String, String> HYPIXEL_KEYS = new ConcurrentHashMap<>();
    private static final String[] URCHIN_KEY_PRIORITY = {"nametags_stats", "bedwars_overlay", "default"};
    private static final String[] HYPIXEL_KEY_PRIORITY = {"nametags_stats", "bedwars_overlay", "default"};
    private static final long FAILURE_RETRY_DELAY_MS = 5L * 60L * 1000L;
    private static final long CACHE_TTL_MS = 10L * 60L * 1000L;
    private static final long CACHE_PRUNE_INTERVAL_MS = 30L * 1000L;
    private static volatile boolean urchinEnabled;
    private static volatile String urchinKey = "";
    private static volatile String hypixelKey = "";
    private static volatile boolean bedwarsGameActive;
    private static volatile boolean retryFailedLookups;
    private static volatile long lastCachePruneAt;

    private BedwarsStatsService() {
    }

    public static BedwarsStats get(String playerName) {
        pruneExpiredCacheIfNeeded();
        String key = normalize(playerName);
        if (bedwarsGameActive && !isBedwarsPlayer(key)) {
            return null;
        }

        return getCached(key);
    }

    public static boolean isBedwarsGameActive() {
        return bedwarsGameActive;
    }

    public static void setBedwarsGameActive(boolean active) {
        if (bedwarsGameActive == active) {
            return;
        }

        bedwarsGameActive = active;
        if (!active) {
            IN_FLIGHT.clear();
            BEDWARS_PLAYERS.clear();
            pruneExpiredCache();
        }
    }

    public static void setBedwarsPlayers(List<String> playerNames) {
        BEDWARS_PLAYERS.clear();
        if (playerNames == null) {
            return;
        }

        for (String playerName : playerNames) {
            String key = normalize(playerName);
            if (!key.isEmpty()) {
                BEDWARS_PLAYERS.add(key);
            }
        }
    }

    public static void configureUrchin(boolean enabled, String key) {
        configureUrchin("default", enabled, key);
    }

    public static void configureUrchin(String source, boolean enabled, String key) {
        String sourceKey = source == null || source.trim().isEmpty() ? "default" : source.trim();
        String cleanKey = key == null ? "" : key.trim();
        String previousKey = urchinKey;

        if (enabled && !cleanKey.isEmpty()) {
            URCHIN_KEYS.put(sourceKey, cleanKey);
        } else {
            URCHIN_KEYS.remove(sourceKey);
        }

        urchinKey = resolveUrchinKey();
        urchinEnabled = !urchinKey.isEmpty();
        if (!previousKey.equals(urchinKey)) {
            clearCache();
        }
    }

    public static void configureHypixelApiKey(String key) {
        configureHypixelApiKey("default", key);
    }

    public static void configureHypixelApiKey(String source, String key) {
        String sourceKey = source == null || source.trim().isEmpty() ? "default" : source.trim();
        String cleanKey = key == null ? "" : key.trim();
        String previousKey = hypixelKey;

        if (!cleanKey.isEmpty()) {
            HYPIXEL_KEYS.put(sourceKey, cleanKey);
        } else {
            HYPIXEL_KEYS.remove(sourceKey);
        }

        hypixelKey = resolveKey(HYPIXEL_KEYS, HYPIXEL_KEY_PRIORITY);
        if (!previousKey.equals(hypixelKey)) {
            clearCache();
        }
    }

    public static void configureRetryFailedLookups(boolean enabled) {
        if (retryFailedLookups == enabled) {
            return;
        }

        retryFailedLookups = enabled;
        FAILED_UNTIL.clear();
    }

    public static void clearCache() {
        CACHE.clear();
        FAILED_UNTIL.clear();
        IN_FLIGHT.clear();
    }

    public static void request(String playerName) {
        request(playerName, null);
    }

    public static void request(String playerName, StatsCallback callback) {
        pruneExpiredCacheIfNeeded();
        if (!bedwarsGameActive) {
            return;
        }

        String key = normalize(playerName);
        if (key.isEmpty() || !isBedwarsPlayer(key) || isFailureCoolingDown(key)) {
            return;
        }

        BedwarsStats cached = getCached(key);
        if (cached != null) {
            if (callback != null) {
                callback.onLoaded(cached);
            }
            return;
        }

        if (!IN_FLIGHT.add(key)) {
            return;
        }

        EXECUTOR.submit(() -> {
            try {
                if (!bedwarsGameActive) {
                    return;
                }

                BedwarsStats stats = fetch(playerName);
                if (!bedwarsGameActive || stats == null) {
                    return;
                }

                putCached(key, stats);
                if (callback != null) {
                    MINECRAFT.addScheduledTask(() -> callback.onLoaded(stats));
                }
            } catch (Exception exception) {
                markFailure(key);
                notifyFailure(exception);
            } finally {
                IN_FLIGHT.remove(key);
            }
        });
    }

    public static void requestAll(List<String> playerNames, StatsCallback callback) {
        for (String playerName : playerNames) {
            request(playerName, callback);
        }
    }

    public static String getTabSuffix(String playerName, TagVisibility visibility) {
        pruneExpiredCacheIfNeeded();
        String key = normalize(playerName);
        if (!bedwarsGameActive || !isBedwarsPlayer(key)) {
            return null;
        }

        BedwarsStats stats = getCached(key);
        if (stats == null) {
            request(playerName);
            return null;
        }

        String tags = stats.getVisibleTags(visibility);
        if (!tags.isEmpty()) {
            return tags + EnumChatFormatting.RESET + " " + stats.getFormattedFkdr();
        }

        return stats.getFormattedFkdr();
    }

    private static BedwarsStats fetch(String playerName) throws IOException {
        String uuid = getUUIDFromTab(playerName);
        if (uuid == null || uuid.isEmpty()) {
            uuid = fetchUUID(playerName);
        }

        JsonObject root = fetchHypixelPlayerData(uuid);
        JsonObject player = getObject(root, "player");
        if (player == null) {
            throw new IOException("Hypixel player data missing");
        }

        JsonObject achievements = getObject(player, "achievements");
        JsonObject stats = getObject(player, "stats");
        JsonObject bedwars = getObject(stats, "Bedwars");

        String name = getString(player, "displayname");
        if (name == null || name.trim().isEmpty()) {
            name = playerName;
        }
        int stars = getInt(achievements, "bedwars_level");
        int finalKills = getInt(bedwars, "final_kills_bedwars");
        int finalDeaths = getInt(bedwars, "final_deaths_bedwars");
        int winstreak = getInt(bedwars, "winstreak");
        long firstLogin = getLong(player, "firstLogin");
        double fkdr = finalDeaths == 0 ? finalKills : finalKills / (double) finalDeaths;
        List<BedwarsTag> tags = buildTags(name, stars, fkdr, winstreak, finalKills, finalDeaths, firstLogin);
        if (urchinEnabled && !urchinKey.isEmpty()) {
            tags.addAll(fetchUrchinTags(name, urchinKey));
        }

        return new BedwarsStats(playerName, stars, fkdr, finalKills, finalDeaths, winstreak, tags);
    }

    private static List<BedwarsTag> buildTags(String name, int stars, double fkdr, int winstreak, int finalKills, int finalDeaths, long firstLogin) {
        List<BedwarsTag> tags = new ArrayList<>();
        String lowerName = name.toLowerCase();
        String[] suspiciousWords = {
                "msmc", "kikin", "g0ld", "fxrina_", "mal_", "fer_", "ly_", "tzi_", "verse_",
                "uwunova", "anas_", "myloalt_", "rayl_", "mchk_", "hellalts_", "disruptive",
                "solaralts_", "g0ldalts_", "unwilling", "predicative"
        };

        for (String suspiciousWord : suspiciousWords) {
            if (lowerName.contains(suspiciousWord)) {
                tags.add(BedwarsTag.suspiciousName());
                break;
            }
        }

        if (!containsTag(tags, BedwarsTagType.SUSPICIOUS_NAME)
                && Pattern.compile("\\d.*\\d.*\\d.*\\d").matcher(name).find()) {
            tags.add(BedwarsTag.suspiciousName());
        }
        if (stars <= 6 && winstreak >= 1) {
            tags.add(BedwarsTag.lowStarWinstreak());
        }
        if (stars <= 6 && fkdr >= 4.0D) {
            tags.add(BedwarsTag.lowStarFkdr());
        }
        if (isNewLogin(firstLogin)) {
            tags.add(BedwarsTag.newLogin());
        }
        if (finalKills == 0 && finalDeaths == 0) {
            tags.add(BedwarsTag.zeroFinals());
        }

        return tags;
    }

    private static boolean containsTag(List<BedwarsTag> tags, BedwarsTagType type) {
        for (BedwarsTag tag : tags) {
            if (tag.getType() == type) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNewLogin(long firstLogin) {
        if (firstLogin <= 0L) {
            return false;
        }

        long now = System.currentTimeMillis();
        long oneDay = 24L * 60L * 60L * 1000L;
        return Math.abs(now - firstLogin) <= oneDay;
    }

    private static int getInt(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return 0;
        }

        try {
            return object.get(key).getAsInt();
        } catch (RuntimeException ignored) {
            try {
                return Integer.parseInt(object.get(key).getAsString().replace(",", ""));
            } catch (RuntimeException ignoredAgain) {
                return 0;
            }
        }
    }

    private static String getUUIDFromTab(String playerName) {
        if (MINECRAFT.getNetHandler() == null) {
            return null;
        }

        for (NetworkPlayerInfo info : MINECRAFT.getNetHandler().getPlayerInfoMap()) {
            if (info.getGameProfile().getName().equalsIgnoreCase(playerName)) {
                return String.valueOf(info.getGameProfile().getId());
            }
        }
        return null;
    }

    private static String fetchUUID(String playerName) throws IOException {
        HttpURLConnection connection = open("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + playerName);
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("UUID lookup failed: " + responseCode);
        }

        String response = read(connection);
        String[] parts = response.split("\"");
        if (parts.length >= 5) {
            return parts[3];
        }

        throw new IOException("UUID not found");
    }

    private static JsonObject fetchHypixelPlayerData(String uuid) throws IOException {
        if (hypixelKey.isEmpty()) {
            throw new MissingHypixelKeyException();
        }

        HttpURLConnection connection = open("https://api.hypixel.net/v2/player?uuid=" + URLEncoder.encode(uuid, "UTF-8"));
        connection.setRequestProperty("API-Key", hypixelKey);
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Hypixel failed: " + responseCode);
        }

        JsonObject root = new JsonParser().parse(read(connection)).getAsJsonObject();
        if (root.has("success") && !root.get("success").getAsBoolean()) {
            String cause = getString(root, "cause");
            throw new IOException("Hypixel failed: " + (cause == null ? "unknown" : cause));
        }
        return root;
    }

    private static List<BedwarsTag> fetchUrchinTags(String playerName, String key) {
        List<BedwarsTag> tags = new ArrayList<>();
        try {
            String url = "https://urchin.ws/player/"
                    + URLEncoder.encode(playerName, "UTF-8")
                    + "?key=" + URLEncoder.encode(key, "UTF-8")
                    + "&sources=MANUAL";
            HttpURLConnection connection = open(url);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return tags;
            }

            JsonElement root = new JsonParser().parse(read(connection));
            collectUrchinTags(root, tags);
        } catch (RuntimeException | IOException ignored) {
        }
        return tags;
    }

    private static void collectUrchinTags(JsonElement element, List<BedwarsTag> tags) {
        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            String type = getString(object, "type");
            BedwarsTag tag = BedwarsTag.urchin(type);
            if (tag != null && !containsTag(tags, tag.getType())) {
                tags.add(tag);
            }

            for (Entry<String, JsonElement> entry : object.entrySet()) {
                collectUrchinTags(entry.getValue(), tags);
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) {
                collectUrchinTags(child, tags);
            }
        }
    }

    private static String getString(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }

        try {
            return object.get(key).getAsString();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static JsonObject getObject(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }

        try {
            return object.getAsJsonObject(key);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static long getLong(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return 0L;
        }

        try {
            return object.get(key).getAsLong();
        } catch (RuntimeException ignored) {
            return 0L;
        }
    }

    private static HttpURLConnection open(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(12000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept", "application/json,text/html");
        return connection;
    }

    private static String read(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private static String normalize(String playerName) {
        return playerName == null ? "" : playerName.toLowerCase();
    }

    private static BedwarsStats getCached(String key) {
        CacheEntry entry = CACHE.get(key);
        return entry == null ? null : entry.getStats();
    }

    private static void putCached(String key, BedwarsStats stats) {
        CACHE.put(key, new CacheEntry(stats, System.currentTimeMillis()));
    }

    public static void pruneExpiredCacheIfNeeded() {
        if (bedwarsGameActive) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastCachePruneAt < CACHE_PRUNE_INTERVAL_MS) {
            return;
        }

        lastCachePruneAt = now;
        pruneExpiredCache();
    }

    private static void pruneExpiredCache() {
        if (bedwarsGameActive) {
            return;
        }

        long expiresBefore = System.currentTimeMillis() - CACHE_TTL_MS;
        for (Entry<String, CacheEntry> entry : CACHE.entrySet()) {
            if (entry.getValue().getLoadedAt() <= expiresBefore) {
                CACHE.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    public static boolean isSelfPlayer(String playerName) {
        if (playerName == null || MINECRAFT.thePlayer == null || MINECRAFT.thePlayer.getGameProfile() == null) {
            return false;
        }

        return playerName.equalsIgnoreCase(MINECRAFT.thePlayer.getGameProfile().getName());
    }

    private static boolean isBedwarsPlayer(String key) {
        return !BEDWARS_PLAYERS.isEmpty() && BEDWARS_PLAYERS.contains(key);
    }

    private static boolean isFailureCoolingDown(String key) {
        Long retryAt = FAILED_UNTIL.get(key);
        if (retryAt == null) {
            return false;
        }

        if (retryAt == Long.MAX_VALUE) {
            return true;
        }

        if (System.currentTimeMillis() < retryAt) {
            return true;
        }

        FAILED_UNTIL.remove(key);
        return false;
    }

    private static void markFailure(String key) {
        long retryAt = retryFailedLookups ? System.currentTimeMillis() + FAILURE_RETRY_DELAY_MS : Long.MAX_VALUE;
        FAILED_UNTIL.put(key, retryAt);
    }

    private static String resolveUrchinKey() {
        return resolveKey(URCHIN_KEYS, URCHIN_KEY_PRIORITY);
    }

    private static String resolveKey(Map<String, String> keys, String[] priority) {
        for (String source : priority) {
            String key = keys.get(source);
            if (key != null && !key.isEmpty()) {
                return key;
            }
        }

        for (String key : keys.values()) {
            if (key != null && !key.isEmpty()) {
                return key;
            }
        }
        return "";
    }

    private static void notifyFailure(Exception exception) {
        String message = exception instanceof MissingHypixelKeyException
                ? "Set a Hypixel API key with /bwthypixel <key> to show Bedwars stats."
                : "Bedwars stats lookup failed: " + exception.getMessage();
        MINECRAFT.addScheduledTask(() -> {
            if (MINECRAFT.thePlayer != null) {
                MINECRAFT.thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GRAY + "[" + EnumChatFormatting.AQUA + "BWT" + EnumChatFormatting.GRAY + "] "
                                + EnumChatFormatting.YELLOW + message
                ));
            }
        });
    }

    private static final class MissingHypixelKeyException extends IOException {
        private MissingHypixelKeyException() {
            super("Hypixel API key missing");
        }
    }

    private static final class CacheEntry {
        private final BedwarsStats stats;
        private final long loadedAt;

        private CacheEntry(BedwarsStats stats, long loadedAt) {
            this.stats = stats;
            this.loadedAt = loadedAt;
        }

        private BedwarsStats getStats() {
            return stats;
        }

        private long getLoadedAt() {
            return loadedAt;
        }
    }

    public interface StatsCallback {
        void onLoaded(BedwarsStats stats);
    }

    public interface TagVisibility {
        boolean isVisible(BedwarsTagType type);
    }

    public enum BedwarsTagType {
        SUSPICIOUS_NAME,
        LOW_STAR_WINSTREAK,
        LOW_STAR_FKDR,
        NEW_LOGIN,
        ZERO_FINALS,
        URCHIN_SNIPER,
        URCHIN_BLATANT_CHEATER,
        URCHIN_CLOSET_CHEATER,
        URCHIN_CONFIRMED_CHEATER,
        URCHIN_OTHER
    }

    public static final class BedwarsTag {
        private final BedwarsTagType type;
        private final String text;

        private BedwarsTag(BedwarsTagType type, String text) {
            this.type = type;
            this.text = text;
        }

        public static BedwarsTag suspiciousName() {
            return new BedwarsTag(BedwarsTagType.SUSPICIOUS_NAME, EnumChatFormatting.YELLOW + "N");
        }

        public static BedwarsTag lowStarWinstreak() {
            return new BedwarsTag(BedwarsTagType.LOW_STAR_WINSTREAK, EnumChatFormatting.GREEN + "W");
        }

        public static BedwarsTag lowStarFkdr() {
            return new BedwarsTag(BedwarsTagType.LOW_STAR_FKDR, EnumChatFormatting.DARK_RED + "F");
        }

        public static BedwarsTag newLogin() {
            return new BedwarsTag(BedwarsTagType.NEW_LOGIN, EnumChatFormatting.RED + "NL");
        }

        public static BedwarsTag zeroFinals() {
            return new BedwarsTag(BedwarsTagType.ZERO_FINALS, EnumChatFormatting.RED + "0F");
        }

        public static BedwarsTag urchin(String type) {
            if (type == null || type.trim().isEmpty()) {
                return null;
            }

            String normalized = type.trim().toLowerCase();
            if ("sniper".equals(normalized)) {
                return new BedwarsTag(BedwarsTagType.URCHIN_SNIPER, EnumChatFormatting.DARK_RED + "Sniper");
            }
            if ("blatant_cheater".equals(normalized)) {
                return new BedwarsTag(BedwarsTagType.URCHIN_BLATANT_CHEATER, EnumChatFormatting.RED + "Blatant");
            }
            if ("closet_cheater".equals(normalized)) {
                return new BedwarsTag(BedwarsTagType.URCHIN_CLOSET_CHEATER, EnumChatFormatting.YELLOW + "Closet");
            }
            if ("confirmed_cheater".equals(normalized)) {
                return new BedwarsTag(BedwarsTagType.URCHIN_CONFIRMED_CHEATER, EnumChatFormatting.DARK_PURPLE + "Confirmed");
            }
            return new BedwarsTag(BedwarsTagType.URCHIN_OTHER, EnumChatFormatting.LIGHT_PURPLE + normalized);
        }

        public BedwarsTagType getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }

    public static final class BedwarsStats {
        private final String playerName;
        private final int stars;
        private final double fkdr;
        private final int finalKills;
        private final int finalDeaths;
        private final int winstreak;
        private final List<BedwarsTag> tags;

        private BedwarsStats(String playerName, int stars, double fkdr, int finalKills, int finalDeaths, int winstreak, List<BedwarsTag> tags) {
            this.playerName = playerName;
            this.stars = stars;
            this.fkdr = fkdr;
            this.finalKills = finalKills;
            this.finalDeaths = finalDeaths;
            this.winstreak = winstreak;
            this.tags = tags;
        }

        public String getPlayerName() {
            return playerName;
        }

        public double getFkdr() {
            return fkdr;
        }

        public int getWinstreak() {
            return winstreak;
        }

        public String getFormattedFkdr() {
            DecimalFormat format = new DecimalFormat("#.##");
            return getFkdrColor() + format.format(fkdr);
        }

        public String getFormattedStars() {
            return formatStars(stars);
        }

        public String getVisibleTags(TagVisibility visibility) {
            StringBuilder builder = new StringBuilder();
            for (BedwarsTag tag : tags) {
                if (visibility.isVisible(tag.getType())) {
                    if (builder.length() > 0) {
                        builder.append(EnumChatFormatting.RESET).append(" ");
                    }
                    builder.append(tag.getText());
                }
            }
            return builder.toString();
        }

        public boolean hasVisibleTag(TagVisibility visibility) {
            return !getVisibleTags(visibility).isEmpty();
        }

        public String getThreatLine(TagVisibility visibility) {
            return getThreatLine(visibility, EnumChatFormatting.WHITE + playerName);
        }

        public String getThreatLine(TagVisibility visibility, String formattedPlayerName) {
            String tagsText = getVisibleTags(visibility);
            String statsText = getFormattedStars() + EnumChatFormatting.GRAY + " | " + EnumChatFormatting.RESET
                    + "FKDR: " + getFormattedFkdr();
            if (!tagsText.isEmpty()) {
                statsText += EnumChatFormatting.GRAY + " | " + EnumChatFormatting.RESET + "[" + tagsText + EnumChatFormatting.RESET + "]";
            }
            return formattedPlayerName + EnumChatFormatting.RESET + " " + statsText;
        }

        private String getFkdrColor() {
            if (fkdr >= 25.0D) {
                return String.valueOf(EnumChatFormatting.DARK_RED);
            }
            if (fkdr >= 16.0D) {
                return String.valueOf(EnumChatFormatting.LIGHT_PURPLE);
            }
            if (fkdr >= 8.0D) {
                return String.valueOf(EnumChatFormatting.GOLD);
            }
            if (fkdr >= 3.0D) {
                return String.valueOf(EnumChatFormatting.GREEN);
            }
            if (fkdr >= 1.0D) {
                return String.valueOf(EnumChatFormatting.WHITE);
            }
            return String.valueOf(EnumChatFormatting.GRAY);
        }

        private static String formatStars(int stars) {
            String color = String.valueOf(EnumChatFormatting.GRAY);
            if (stars >= 900) {
                color = String.valueOf(EnumChatFormatting.DARK_PURPLE);
            } else if (stars >= 800) {
                color = String.valueOf(EnumChatFormatting.BLUE);
            } else if (stars >= 700) {
                color = String.valueOf(EnumChatFormatting.LIGHT_PURPLE);
            } else if (stars >= 600) {
                color = String.valueOf(EnumChatFormatting.DARK_RED);
            } else if (stars >= 500) {
                color = String.valueOf(EnumChatFormatting.DARK_AQUA);
            } else if (stars >= 400) {
                color = String.valueOf(EnumChatFormatting.DARK_GREEN);
            } else if (stars >= 300) {
                color = String.valueOf(EnumChatFormatting.AQUA);
            } else if (stars >= 200) {
                color = String.valueOf(EnumChatFormatting.GOLD);
            } else if (stars >= 100) {
                color = String.valueOf(EnumChatFormatting.WHITE);
            }
            return color + stars + "\u272b";
        }
    }
}
