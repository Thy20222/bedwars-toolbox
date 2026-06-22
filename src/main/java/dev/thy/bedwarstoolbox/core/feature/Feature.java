package dev.thy.bedwarstoolbox.core.feature;

import dev.thy.bedwarstoolbox.core.config.BooleanSetting;
import dev.thy.bedwarstoolbox.core.config.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Feature {
    protected boolean enabled;
    private final FeatureCategory category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final BooleanSetting enabledSetting = new BooleanSetting("Enabled", false);

    public Feature() {
        this(FeatureCategory.RENDER);
    }

    public Feature(FeatureCategory category) {
        this.category = category;
        registerSetting(enabledSetting);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        enabledSetting.setValue(enabled);
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public BooleanSetting getEnabledSetting() {
        return enabledSetting;
    }

    public FeatureCategory getCategory() {
        return category;
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
