package dev.thy.bedwarstoolbox.feature.render;

import dev.thy.bedwarstoolbox.core.config.NumberSetting;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import net.minecraft.client.Minecraft;

public class FullBright extends Feature {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final NumberSetting brightness = new NumberSetting("Brightness", "Client gamma value", 15.0D, 1.0D, 100.0D);
    private float previousGamma;

    public FullBright() {
        super(FeatureCategory.RENDER);
        registerSetting(brightness);
    }

    @Override
    public void onEnable() {
        previousGamma = minecraft.gameSettings.gammaSetting;
        applyBrightness();
    }

    @Override
    public void onDisable() {
        minecraft.gameSettings.gammaSetting = previousGamma;
    }

    @Override
    public void onTick() {
        applyBrightness();
    }

    private void applyBrightness() {
        minecraft.gameSettings.gammaSetting = brightness.getValue().floatValue();
    }
}
