package dev.thy.bedwarstoolbox;

import dev.thy.bedwarstoolbox.core.FeatureManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "bedwarstoolbox", useMetadata=true)
public class BedwarsToolbox {
    private FeatureManager featureManager;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        featureManager = new FeatureManager();
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }
}
