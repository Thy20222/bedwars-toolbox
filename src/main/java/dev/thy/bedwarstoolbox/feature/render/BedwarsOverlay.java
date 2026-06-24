package dev.thy.bedwarstoolbox.feature.render;

import dev.thy.bedwarstoolbox.core.config.BooleanSetting;
import dev.thy.bedwarstoolbox.core.config.NumberSetting;
import dev.thy.bedwarstoolbox.core.config.StringSetting;
import dev.thy.bedwarstoolbox.core.Global;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import dev.thy.bedwarstoolbox.core.stats.BedwarsStatsService;
import dev.thy.bedwarstoolbox.core.stats.BedwarsStatsService.BedwarsStats;
import dev.thy.bedwarstoolbox.core.stats.BedwarsStatsService.BedwarsTagType;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class BedwarsOverlay extends Feature implements Global, BedwarsStatsService.TagVisibility {
    private static BedwarsOverlay instance;

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final BooleanSetting autoWho = new BooleanSetting("Auto Who", true);
    private final BooleanSetting threatChat = new BooleanSetting("Threat Chat", true);
    private final NumberSetting threatFkdr = new NumberSetting("Threat FKDR", "Show players at or above this FKDR", 8.0D, 0.0D, 100.0D);
    private final BooleanSetting showSuspiciousName = new BooleanSetting("Show N Tag", true);
    private final BooleanSetting showLowStarWinstreak = new BooleanSetting("Show W Tag", true);
    private final BooleanSetting showLowStarFkdr = new BooleanSetting("Show F Tag", true);
    private final BooleanSetting showNewLogin = new BooleanSetting("Show NL Tag", true);
    private final BooleanSetting showZeroFinals = new BooleanSetting("Show 0F Tag", true);
    private final BooleanSetting useUrchinTags = new BooleanSetting("Use Urchin Tags", false);
    private final BooleanSetting showUrchinTags = new BooleanSetting("Show Urchin Tags", true);
    private final BooleanSetting retryFailedLookups = new BooleanSetting("Retry Failed Lookups", false);
    private final StringSetting hypixelApiKey = new StringSetting("Hypixel API Key", "");
    private final StringSetting urchinKey = new StringSetting("Urchin API Key", "");

    public BedwarsOverlay() {
        super(FeatureCategory.RENDER);
        instance = this;
        registerSetting(autoWho);
        registerSetting(threatChat);
        registerSetting(threatFkdr);
        registerSetting(showSuspiciousName);
        registerSetting(showLowStarWinstreak);
        registerSetting(showLowStarFkdr);
        registerSetting(showNewLogin);
        registerSetting(showZeroFinals);
        registerSetting(useUrchinTags);
        registerSetting(showUrchinTags);
        registerSetting(retryFailedLookups);
        registerSetting(hypixelApiKey);
        registerSetting(urchinKey);
    }

    @Override
    public void onTick() {
        syncUrchinConfig();
    }

    public static String getTabSuffix(String playerName) {
        if (instance == null || !instance.isEnabled()) {
            return null;
        }
        instance.syncUrchinConfig();
        return BedwarsStatsService.getTabSuffix(playerName, instance);
    }

    public static void setUrchinKey(String key) {
        if (instance == null) {
            return;
        }

        instance.urchinKey.setValue(key);
        instance.syncUrchinConfig();
        BedwarsStatsService.clearCache();
    }

    public static void setUrchinEnabled(boolean enabled) {
        if (instance == null) {
            return;
        }

        instance.useUrchinTags.setValue(enabled);
        instance.syncUrchinConfig();
        BedwarsStatsService.clearCache();
    }

    public static void setHypixelApiKey(String key) {
        if (instance == null) {
            return;
        }

        instance.hypixelApiKey.setValue(key);
        instance.syncStatsConfig();
        BedwarsStatsService.clearCache();
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static boolean shouldAutoWho() {
        return instance != null && instance.autoWho.getValue();
    }

    public static void syncConfig() {
        if (instance != null) {
            instance.syncStatsConfig();
        }
    }

    public static void showThreatIfNeeded(BedwarsStats stats) {
        if (instance != null) {
            instance.sendThreatIfNeeded(stats);
        }
    }

    @Override
    public boolean isVisible(BedwarsTagType type) {
        if (isUrchinTag(type)) {
            return useUrchinTags.getValue() && showUrchinTags.getValue();
        }
        if (type == BedwarsTagType.SUSPICIOUS_NAME) {
            return showSuspiciousName.getValue();
        }
        if (type == BedwarsTagType.LOW_STAR_WINSTREAK) {
            return showLowStarWinstreak.getValue();
        }
        if (type == BedwarsTagType.LOW_STAR_FKDR) {
            return showLowStarFkdr.getValue();
        }
        if (type == BedwarsTagType.NEW_LOGIN) {
            return showNewLogin.getValue();
        }
        if (type == BedwarsTagType.ZERO_FINALS) {
            return showZeroFinals.getValue();
        }
        return true;
    }

    private void syncUrchinConfig() {
        syncStatsConfig();
    }

    private void syncStatsConfig() {
        BedwarsStatsService.configureHypixelApiKey("bedwars_overlay", hypixelApiKey.getValue());
        BedwarsStatsService.configureRetryFailedLookups(retryFailedLookups.getValue());
        BedwarsStatsService.configureUrchin("bedwars_overlay", useUrchinTags.getValue(), urchinKey.getValue());
    }

    private boolean isUrchinTag(BedwarsTagType type) {
        return type == BedwarsTagType.URCHIN_SNIPER
                || type == BedwarsTagType.URCHIN_BLATANT_CHEATER
                || type == BedwarsTagType.URCHIN_CLOSET_CHEATER
                || type == BedwarsTagType.URCHIN_CONFIRMED_CHEATER
                || type == BedwarsTagType.URCHIN_OTHER;
    }

    private void sendThreatIfNeeded(BedwarsStats stats) {
        if (!isEnabled()
                || !threatChat.getValue()
                || mc.thePlayer == null
                || BedwarsStatsService.isSelfPlayer(stats.getPlayerName())) {
            return;
        }

        if (stats.hasVisibleTag(this) || stats.getFkdr() >= threatFkdr.getValue()) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GRAY + "[" + EnumChatFormatting.AQUA + "BWT" + EnumChatFormatting.GRAY + "] "
                            + EnumChatFormatting.RED + "\u26a0 " + EnumChatFormatting.RESET
                            + stats.getThreatLine(this, getTeamColoredName(stats.getPlayerName()))
            ));
        }
    }

    private String getTeamColoredName(String playerName) {
        if (minecraft.theWorld == null || playerName == null) {
            return playerName;
        }

        ScorePlayerTeam team = minecraft.theWorld.getScoreboard().getPlayersTeam(playerName);
        if (team == null) {
            return playerName;
        }
        return ScorePlayerTeam.formatPlayerName(team, playerName);
    }
}
