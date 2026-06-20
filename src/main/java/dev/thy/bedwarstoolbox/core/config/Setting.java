package dev.thy.bedwarstoolbox.core.config;

public class Setting<T> {
    private final String name;
    private final String description;
    private T value;

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
        this.value = value;
    }
}
