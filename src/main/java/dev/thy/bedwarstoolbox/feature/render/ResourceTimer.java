package dev.thy.bedwarstoolbox.feature.render;

import dev.thy.bedwarstoolbox.core.Global;
import dev.thy.bedwarstoolbox.core.config.ColorSetting;
import dev.thy.bedwarstoolbox.core.config.NumberSetting;
import dev.thy.bedwarstoolbox.core.event.ChatReceivedEvent;
import dev.thy.bedwarstoolbox.core.event.Render2DEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceTimer extends Feature implements Global {
    private static final Pattern SPAWN_TIME_PATTERN = Pattern.compile("^Spawns in (\\d+) seconds?$");

    private final NumberSetting xOffset = new NumberSetting("X Offset", "HUD horizontal offset", 5.0D, 0.0D, 2000.0D);
    private final NumberSetting yOffset = new NumberSetting("Y Offset", "HUD vertical offset", 5.0D, 0.0D, 2000.0D);
    private final ColorSetting diamondColor = new ColorSetting("Diamond Color", 185, 242, 255, 255);
    private final ColorSetting emeraldColor = new ColorSetting("Emerald Color", 80, 200, 120, 255);

    private Vec3 diamondPos = null;
    private Vec3 emeraldPos = null;
    private String diamondTime = "";
    private String emeraldTime = "";
    private int tickCounter = 0;
    private static boolean active = false;

    public ResourceTimer() {
        super(FeatureCategory.RENDER);
        registerSetting(xOffset);
        registerSetting(yOffset);
        registerSetting(diamondColor);
        registerSetting(emeraldColor);
    }

    @Override
    public void onDisable() {
        setBedwarsGameActive(false);
        clearCache();
    }

    @Subscribe
    public void onChat(ChatReceivedEvent event) {
        if (!isEnabled() || mc.thePlayer == null) {
            return;
        }

        String message = event.getMessage().getUnformattedText();
        if (isLobbyOrServerTransferMessage(message)) {
            setBedwarsGameActive(false);
            clearCache();
            return;
        }

        if (isBedwarsStartMessage(message)) {
            setBedwarsGameActive(true);
            clearCache();
        }
    }

    @Override
    public void onTick() {
        if (!isEnabled() || !active || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        tickCounter++;
        if (tickCounter % 10 != 0) {
            return;
        }

        updateResourceTimer(true);
        updateResourceTimer(false);
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled() || !active || mc.fontRendererObj == null) {
            return;
        }

        FontRenderer font = mc.fontRendererObj;
        int x = xOffset.getValue().intValue();
        int y = yOffset.getValue().intValue();

        font.drawStringWithShadow("Diamond: " + formatTime(diamondTime), x, y, diamondColor.toArgb());
        font.drawStringWithShadow("Emerald: " + formatTime(emeraldTime), x, y + font.FONT_HEIGHT + 2, emeraldColor.toArgb());
    }

    private void updateResourceTimer(boolean diamond) {
        Vec3 resourcePos = diamond ? diamondPos : emeraldPos;
        String resourceName = diamond ? "Diamond" : "Emerald";

        if (resourcePos == null) {
            ResourceMatch match = findResourceMatch(resourceName);
            if (match == null) {
                setTime(diamond, "");
                return;
            }

            setPosition(diamond, match.position);
            setTime(diamond, match.time);
            return;
        }

        String time = findSpawnTimeNear(resourcePos);
        setTime(diamond, time == null ? "" : time);
    }

    private ResourceMatch findResourceMatch(String resourceName) {
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityArmorStand)) {
                continue;
            }

            String name = getArmorStandName((EntityArmorStand) entity);
            if (name == null || !name.contains(resourceName)) {
                continue;
            }

            Vec3 position = entity.getPositionVector();
            String time = findSpawnTimeNear(position);
            if (time != null) {
                return new ResourceMatch(position, time);
            }
        }

        return null;
    }

    private String findSpawnTimeNear(Vec3 position) {
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityArmorStand) || !isNearResource(position, entity)) {
                continue;
            }

            String name = getArmorStandName((EntityArmorStand) entity);
            if (name == null) {
                continue;
            }

            Matcher matcher = SPAWN_TIME_PATTERN.matcher(name);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    private boolean isNearResource(Vec3 position, Entity entity) {
        return Math.abs(entity.posX - position.xCoord) <= 0.5D
                && Math.abs(entity.posZ - position.zCoord) <= 0.5D
                && Math.abs(entity.posY - position.yCoord) <= 1.0D;
    }

    private String getArmorStandName(EntityArmorStand stand) {
        if (!stand.hasCustomName()) {
            return null;
        }

        return EnumChatFormatting.getTextWithoutFormattingCodes(stand.getCustomNameTag());
    }

    private void setPosition(boolean diamond, Vec3 position) {
        if (diamond) {
            diamondPos = position;
        } else {
            emeraldPos = position;
        }
    }

    private void setTime(boolean diamond, String time) {
        if (diamond) {
            diamondTime = time;
        } else {
            emeraldTime = time;
        }
    }

    private String formatTime(String time) {
        return time == null || time.isEmpty() ? "--" : time + "s";
    }

    private void clearCache() {
        diamondPos = null;
        emeraldPos = null;
        diamondTime = "";
        emeraldTime = "";
        tickCounter = 0;
    }

    private boolean isBedwarsStartMessage(String message) {
        return message.contains("Protect your bed and destroy the enemy beds.")
                && !message.contains(":")
                && !message.contains("SHOUT");
    }

    private boolean isLobbyOrServerTransferMessage(String message) {
        return message.contains("joined the lobby!")
                || message.startsWith("Sending you to ")
                || message.contains("You are currently connected to server");
    }

    public static void setBedwarsGameActive(boolean active) {
        ResourceTimer.active = active;
    }

    private static class ResourceMatch {
        private final Vec3 position;
        private final String time;

        private ResourceMatch(Vec3 position, String time) {
            this.position = position;
            this.time = time;
        }
    }
}
