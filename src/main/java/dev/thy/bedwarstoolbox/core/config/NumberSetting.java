package dev.thy.bedwarstoolbox.core.config;

public class NumberSetting extends Setting<Double> {
    private final double minimum;
    private final double maximum;

    public NumberSetting(String name, double value) {
        this(name, null, value, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public NumberSetting(String name, String description, double value) {
        this(name, description, value, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public NumberSetting(String name, String description, double value, double minimum, double maximum) {
        super(name, description, value);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    @Override
    public void setValue(Double value) {
        super.setValue(Math.max(minimum, Math.min(maximum, value)));
    }
}
