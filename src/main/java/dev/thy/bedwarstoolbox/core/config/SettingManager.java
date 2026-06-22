package dev.thy.bedwarstoolbox.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class SettingManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<Setting<?>> settings = new ArrayList<>();
    private final List<Feature> features = new ArrayList<>();
    private final Map<Feature, List<Setting<?>>> featureSettings = new IdentityHashMap<>();
    private final Map<Setting<?>, Feature> settingOwners = new IdentityHashMap<>();
    private final File configFile;
    private boolean loading;
    private boolean dirty;

    public SettingManager() {
        this(new File(Loader.instance().getConfigDir(), "bedwarstoolbox/settings.json"));
    }

    public SettingManager(File configFile) {
        this.configFile = configFile;
    }

    public void register(Setting<?> setting) {
        settings.add(setting);
        setting.setChangeListener(this::markDirty);
    }

    public void register(Feature feature, Setting<?> setting) {
        register(setting);
        settingOwners.put(setting, feature);
        featureSettings.computeIfAbsent(feature, ignored -> new ArrayList<>()).add(setting);
    }

    public void register(Feature feature) {
        if (!features.contains(feature)) {
            features.add(feature);
        }

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

    public void load() {
        if (!configFile.exists()) {
            save();
            return;
        }

        loading = true;
        try (FileReader reader = new FileReader(configFile)) {
            JsonElement rootElement = new JsonParser().parse(reader);
            if (rootElement == null || !rootElement.isJsonObject()) {
                return;
            }

            JsonObject root = rootElement.getAsJsonObject();
            readGlobalSettings(root.getAsJsonObject("settings"));
            readFeatureSettings(root.getAsJsonObject("features"));
            dirty = false;
        } catch (RuntimeException | IOException exception) {
            exception.printStackTrace();
        } finally {
            loading = false;
        }

        save();
    }

    public void saveIfDirty() {
        if (dirty) {
            save();
        }
    }

    public void save() {
        File parent = configFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return;
        }

        JsonObject root = new JsonObject();
        root.add("settings", writeGlobalSettings());
        root.add("features", writeFeatureSettings());

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(root, writer);
            dirty = false;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void readGlobalSettings(JsonObject globalSettings) {
        if (globalSettings == null) {
            return;
        }

        for (Setting<?> setting : settings) {
            if (!settingOwners.containsKey(setting)) {
                applyValue(setting, globalSettings.get(setting.getName()));
            }
        }
    }

    private void readFeatureSettings(JsonObject featuresObject) {
        if (featuresObject == null) {
            return;
        }

        for (Feature feature : features) {
            JsonObject featureObject = featuresObject.getAsJsonObject(getFeatureKey(feature));
            if (featureObject == null) {
                continue;
            }

            for (Setting<?> setting : getSettings(feature)) {
                applyValue(feature, setting, featureObject.get(setting.getName()));
            }
        }
    }

    private JsonObject writeGlobalSettings() {
        JsonObject globalSettings = new JsonObject();
        for (Setting<?> setting : settings) {
            if (!settingOwners.containsKey(setting)) {
                writeSetting(globalSettings, setting);
            }
        }

        return globalSettings;
    }

    private JsonObject writeFeatureSettings() {
        JsonObject featuresObject = new JsonObject();
        for (Feature feature : features) {
            JsonObject featureObject = new JsonObject();
            for (Setting<?> setting : getSettings(feature)) {
                writeSetting(featureObject, setting);
            }

            featuresObject.add(getFeatureKey(feature), featureObject);
        }

        return featuresObject;
    }

    private void writeSetting(JsonObject target, Setting<?> setting) {
        Object value = setting.getValue();
        if (value instanceof Boolean) {
            target.addProperty(setting.getName(), (Boolean) value);
        } else if (value instanceof Number) {
            target.addProperty(setting.getName(), (Number) value);
        } else if (value instanceof String) {
            target.addProperty(setting.getName(), (String) value);
        }
    }

    private void applyValue(Setting<?> setting, JsonElement valueElement) {
        Feature owner = settingOwners.get(setting);
        if (owner != null) {
            applyValue(owner, setting, valueElement);
            return;
        }

        setSettingValue(setting, valueElement);
    }

    private void applyValue(Feature feature, Setting<?> setting, JsonElement valueElement) {
        if (setting == feature.getEnabledSetting()) {
            Boolean value = getBoolean(valueElement);
            if (value != null) {
                feature.setEnabled(value);
            }
            return;
        }

        setSettingValue(setting, valueElement);
    }

    private void setSettingValue(Setting<?> setting, JsonElement valueElement) {
        if (valueElement == null || valueElement.isJsonNull()) {
            return;
        }

        try {
            if (setting instanceof BooleanSetting) {
                Boolean value = getBoolean(valueElement);
                if (value != null) {
                    ((BooleanSetting) setting).setValue(value);
                }
            } else if (setting instanceof NumberSetting) {
                ((NumberSetting) setting).setValue(valueElement.getAsDouble());
            } else if (setting instanceof ColorSetting) {
                ((ColorSetting) setting).setValue(valueElement.getAsInt());
            } else if (setting instanceof KeybindSetting) {
                ((KeybindSetting) setting).setKeyCode(valueElement.getAsInt());
            }
        } catch (RuntimeException ignored) {
        }
    }

    private Boolean getBoolean(JsonElement valueElement) {
        if (valueElement == null || valueElement.isJsonNull()) {
            return null;
        }

        try {
            return valueElement.getAsBoolean();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String getFeatureKey(Feature feature) {
        return feature.getClass().getName();
    }

    private void markDirty() {
        if (!loading) {
            dirty = true;
        }
    }
}
