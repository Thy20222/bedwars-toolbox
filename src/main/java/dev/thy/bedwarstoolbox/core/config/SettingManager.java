package dev.thy.bedwarstoolbox.core.config;

import dev.thy.bedwarstoolbox.core.feature.Feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class SettingManager {
    private final List<Setting<?>> settings = new ArrayList<>();
    private final Map<Feature, List<Setting<?>>> featureSettings = new IdentityHashMap<>();

    public void register(Setting<?> setting) {
        settings.add(setting);
    }

    public void register(Feature feature, Setting<?> setting) {
        register(setting);
        featureSettings.computeIfAbsent(feature, ignored -> new ArrayList<>()).add(setting);
    }

    public void register(Feature feature) {
        for (Setting<?> setting : feature.getSettings()) {
            register(feature, setting);
        }
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public List<Setting<?>> getSettings(Feature feature) {
        List<Setting<?>> settings = featureSettings.get(feature);
        if (settings == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(settings);
    }

    public Setting<?> getSetting(String name) {
        for (Setting<?> setting : settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }

        return null;
    }

    public Setting<?> getSetting(Feature feature, String name) {
        for (Setting<?> setting : getSettings(feature)) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }

        return null;
    }
}
