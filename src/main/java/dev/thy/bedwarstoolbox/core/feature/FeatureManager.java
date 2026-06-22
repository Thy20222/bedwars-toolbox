package dev.thy.bedwarstoolbox.core.feature;

import dev.thy.bedwarstoolbox.core.config.SettingManager;
import dev.thy.bedwarstoolbox.core.event.EventBus;
import dev.thy.bedwarstoolbox.core.event.Render2DEvent;
import dev.thy.bedwarstoolbox.core.event.Render3DEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.core.event.TickEvent;
import dev.thy.bedwarstoolbox.feature.combat.HitFX;
import dev.thy.bedwarstoolbox.feature.render.BlockOverlay;
import dev.thy.bedwarstoolbox.feature.render.TNTTimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureManager {
    private final List<Feature> features = new ArrayList<>();
    private final SettingManager settingManager;
    private final EventBus eventBus;

    public FeatureManager(SettingManager settingManager, EventBus eventBus) {
        this.settingManager = settingManager;
        this.eventBus = eventBus;

        eventBus.register(this);
        registerFeatures();
    }

    private void registerFeatures() {
        register(new BlockOverlay());
        register(new HitFX());
        register(new TNTTimer());
    }

    public List<Feature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    public List<Feature> getFeatures(FeatureCategory category) {
        List<Feature> categoryFeatures = new ArrayList<>();
        for (Feature feature : features) {
            if (feature.getCategory() == category) {
                categoryFeatures.add(feature);
            }
        }

        return Collections.unmodifiableList(categoryFeatures);
    }

    public void register(Feature feature) {
        features.add(feature);
        settingManager.register(feature);
        eventBus.register(feature);
    }

    public void onTick() {
        for (Feature feature : features) {
            if (feature.isEnabled()) {
                feature.onTick();
            }
        }
    }

    public void onRender() {
        for (Feature feature : features) {
            if (feature.isEnabled()) {
                feature.onRender();
            }
        }
    }

    @Subscribe
    public void onTick(TickEvent event) {
        onTick();
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        onRender();
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        onRender();
    }
}
