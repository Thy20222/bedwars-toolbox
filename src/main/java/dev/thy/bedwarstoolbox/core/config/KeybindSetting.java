package dev.thy.bedwarstoolbox.core.config;

public class KeybindSetting extends Setting<Integer> {
    public KeybindSetting(String name, int keyCode) {
        super(name, keyCode);
    }

    public KeybindSetting(String name, String description, int keyCode) {
        super(name, description, keyCode);
    }

    public int getKeyCode() {
        return getValue();
    }

    public void setKeyCode(int keyCode) {
        setValue(keyCode);
    }
}
