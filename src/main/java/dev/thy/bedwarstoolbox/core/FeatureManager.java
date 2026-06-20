package dev.thy.bedwarstoolbox.core;

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
}
