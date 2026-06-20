package dev.thy.bedwarstoolbox.core.feature;

import dev.thy.bedwarstoolbox.core.event.Render2DEvent;
import dev.thy.bedwarstoolbox.core.event.Render3DEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.core.event.TickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureManager {
    private final List<Feature> features = new ArrayList<>();

    public List<Feature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    public void register(Feature feature) {
        features.add(feature);
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
