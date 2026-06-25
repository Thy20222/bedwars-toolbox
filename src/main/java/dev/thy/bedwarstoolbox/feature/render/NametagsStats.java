package dev.thy.bedwarstoolbox.feature.render;

import dev.thy.bedwarstoolbox.core.config.BooleanSetting;
import dev.thy.bedwarstoolbox.core.config.StringSetting;
import dev.thy.bedwarstoolbox.core.event.RenderNameTagEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.core.Global;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import dev.thy.bedwarstoolbox.core.stats.BedwarsStatsService;
import dev.thy.bedwarstoolbox.core.stats.BedwarsStatsService.BedwarsStats;
import dev.thy.bedwarstoolbox.core.stats.BedwarsStatsService.BedwarsTagType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

public class NametagsStats extends Feature implements Global, BedwarsStatsService.TagVisibility {
    private static NametagsStats instance;

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final BooleanSetting autoWho = new BooleanSetting("Auto Who", true);
    private final BooleanSetting showSuspiciousName = new BooleanSetting("Show N Tag", true);
    private final BooleanSetting showLowStarWinstreak = new BooleanSetting("Show W Tag", true);
    private final BooleanSetting showLowStarFkdr = new BooleanSetting("Show F Tag", true);
    private final BooleanSetting showNewLogin = new BooleanSetting("Show NL Tag", true);
    private final BooleanSetting showZeroFinals = new BooleanSetting("Show 0F Tag", true);
    private final BooleanSetting useUrchinTags = new BooleanSetting("Use Urchin Tags", false);
    private final BooleanSetting showUrchinTags = new BooleanSetting("Show Urchin Tags", true);
    private final StringSetting hypixelApiKey = new StringSetting("Hypixel API Key", "");
    private final StringSetting urchinKey = new StringSetting("Urchin API Key", "");

    public NametagsStats() {
        super(FeatureCategory.RENDER);
        instance = this;
        registerSetting(autoWho);
        registerSetting(showSuspiciousName);
        registerSetting(showLowStarWinstreak);
        registerSetting(showLowStarFkdr);
        registerSetting(showNewLogin);
        registerSetting(showZeroFinals);
        registerSetting(useUrchinTags);
        registerSetting(showUrchinTags);
        registerSetting(hypixelApiKey);
        registerSetting(urchinKey);
    }

    @Override
    public void onTick() {
        syncUrchinConfig();
    }

    @Subscribe
    public void onRenderNameTag(RenderNameTagEvent event) {
        if (!isEnabled()
                || !BedwarsStatsService.isBedwarsGameActive()
                || mc.thePlayer == null
                || event.getPlayer() == mc.thePlayer) {
            return;
        }
        syncUrchinConfig();

        String playerName = event.getPlayer().getGameProfile().getName();
        BedwarsStats stats = BedwarsStatsService.get(playerName);
        if (stats == null) {
            BedwarsStatsService.request(playerName);
            return;
        }

        if (event.getPlayer().isInvisibleToPlayer(mc.thePlayer)) return;

        String tags = stats.getVisibleTags(this);
        if (!stats.hasHypixelStats() && tags.isEmpty()) {
            return;
        }

        String text = stats.hasHypixelStats() ? stats.getFormattedStars() : tags;
        if (stats.hasHypixelStats() && !tags.isEmpty()) {
            text += EnumChatFormatting.GRAY + " | " + tags;
        }

        renderText(text, event.getX(), event.getY() + event.getPlayer().height + 0.8D, event.getZ(), !event.getPlayer().isSneaking());
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

    private void syncUrchinConfig() {
        syncStatsConfig();
    }

    private void syncStatsConfig() {
        BedwarsStatsService.configureHypixelApiKey("nametags_stats", hypixelApiKey.getValue());
        BedwarsStatsService.configureUrchin("nametags_stats", useUrchinTags.getValue(), urchinKey.getValue());
    }

    private boolean isUrchinTag(BedwarsTagType type) {
        return type == BedwarsTagType.URCHIN_SNIPER
                || type == BedwarsTagType.URCHIN_BLATANT_CHEATER
                || type == BedwarsTagType.URCHIN_CLOSET_CHEATER
                || type == BedwarsTagType.URCHIN_CONFIRMED_CHEATER
                || type == BedwarsTagType.URCHIN_OTHER;
    }

    private void renderText(String text, double x, double y, double z, boolean seeThrough) {
        RenderManager renderManager = minecraft.getRenderManager();
        FontRenderer fontRenderer = minecraft.fontRendererObj;
        float scale = 0.02666667F;
        int halfWidth = fontRenderer.getStringWidth(text) / 2;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableLighting();
        if (seeThrough) {
            GlStateManager.disableDepth();
        }
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        fontRenderer.drawStringWithShadow(text, -halfWidth, 0, 0xFFFFFFFF);

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        if (seeThrough) {
            GlStateManager.enableDepth();
        }
        GlStateManager.enableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
