package dev.thy.bedwarstoolbox;

import dev.thy.bedwarstoolbox.core.feature.FeatureManager;
import dev.thy.bedwarstoolbox.core.config.SettingManager;
import dev.thy.bedwarstoolbox.core.event.EventBus;
import dev.thy.bedwarstoolbox.core.event.Render2DEvent;
import dev.thy.bedwarstoolbox.core.event.Render3DEvent;
import dev.thy.bedwarstoolbox.core.gui.GuiManager;
import dev.thy.bedwarstoolbox.feature.example.ExampleFeature;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "bedwarstoolbox", useMetadata = true)
public class BedwarsToolbox {
    private FeatureManager featureManager;
    private SettingManager settingManager;
    private EventBus eventBus;
    private GuiManager guiManager;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        eventBus = new EventBus();
        settingManager = new SettingManager();
        featureManager = new FeatureManager();
        guiManager = new GuiManager(featureManager);

        ExampleFeature exampleFeature = new ExampleFeature();
        featureManager.register(exampleFeature);
        settingManager.register(exampleFeature);
        eventBus.register(featureManager);
        eventBus.register(exampleFeature);

        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public SettingManager getSettingManager() {
        return settingManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    @SubscribeEvent
    public void onClientTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (event.phase == net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) {
            eventBus.post(new dev.thy.bedwarstoolbox.core.event.TickEvent());
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            eventBus.post(new Render2DEvent(event.partialTicks));
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        eventBus.post(new Render3DEvent(event.partialTicks));
    }
}
