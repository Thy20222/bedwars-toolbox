package dev.thy.bedwarstoolbox.core.config;

import java.util.Objects;

public class Setting<T> {
    private final String name;
    private final String description;
    private T value;
    private Runnable changeListener;

    public Setting(String name, T value) {
        this(name, null, value);
    }

    public Setting(String name, String description, T value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if (Objects.equals(this.value, value)) {
            return;
        }

        this.value = value;
        if (changeListener != null) {
            changeListener.run();
        }
    }

    void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }
}
