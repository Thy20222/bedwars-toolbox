package dev.thy.bedwarstoolbox.core.config;

public class StringSetting extends Setting<String> {
    public StringSetting(String name, String value) {
        super(name, value);
    }

    public StringSetting(String name, String description, String value) {
        super(name, description, value);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value == null ? "" : value);
    }
}
