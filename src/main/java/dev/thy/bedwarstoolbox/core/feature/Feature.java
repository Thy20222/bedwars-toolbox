package dev.thy.bedwarstoolbox.core.feature;

import dev.thy.bedwarstoolbox.core.config.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Feature {
    protected boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    protected void registerSetting(Setting<?> setting) {
        settings.add(setting);
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    public void onRender() {
    }
}
