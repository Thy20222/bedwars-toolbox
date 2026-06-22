package dev.thy.bedwarstoolbox.feature.example;

import dev.thy.bedwarstoolbox.core.event.Render2DEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import net.minecraft.client.Minecraft;

public class ExampleFeature extends Feature {
    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled()) {
            return;
        }

        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("Example Feature Working", 6, 6, 0xFFFFFFFF);
    }
}
