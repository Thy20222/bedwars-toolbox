package dev.thy.bedwarstoolbox.feature.example;

import dev.thy.bedwarstoolbox.core.config.BooleanSetting;
import dev.thy.bedwarstoolbox.core.event.Render2DEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import net.minecraft.client.Minecraft;

public class ExampleFeature extends Feature {
    private final BooleanSetting enabledSetting = new BooleanSetting("enabled", false);

    public ExampleFeature() {
        registerSetting(enabledSetting);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        enabledSetting.setValue(enabled);
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled()) {
            return;
        }

        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("Example Feature Working", 6, 6, 0xFFFFFFFF);
    }
}
