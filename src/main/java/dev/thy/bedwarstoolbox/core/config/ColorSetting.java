package dev.thy.bedwarstoolbox.core.config;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, int red, int green, int blue, int alpha) {
        super(name, pack(red, green, blue, alpha));
    }

    public ColorSetting(String name, String description, int red, int green, int blue, int alpha) {
        super(name, description, pack(red, green, blue, alpha));
    }

    public int getRed() {
        return getValue() >> 16 & 255;
    }

    public void setRed(int red) {
        setColor(red, getGreen(), getBlue(), getAlpha());
    }

    public int getGreen() {
        return getValue() >> 8 & 255;
    }

    public void setGreen(int green) {
        setColor(getRed(), green, getBlue(), getAlpha());
    }

    public int getBlue() {
        return getValue() & 255;
    }

    public void setBlue(int blue) {
        setColor(getRed(), getGreen(), blue, getAlpha());
    }

    public int getAlpha() {
        return getValue() >> 24 & 255;
    }

    public void setAlpha(int alpha) {
        setColor(getRed(), getGreen(), getBlue(), alpha);
    }

    public void setColor(int red, int green, int blue, int alpha) {
        setValue(pack(red, green, blue, alpha));
    }

    public float getRedFloat() {
        return getRed() / 255.0F;
    }

    public float getGreenFloat() {
        return getGreen() / 255.0F;
    }

    public float getBlueFloat() {
        return getBlue() / 255.0F;
    }

    public float getAlphaFloat() {
        return getAlpha() / 255.0F;
    }

    public int toArgb() {
        return getValue();
    }

    private static int pack(int red, int green, int blue, int alpha) {
        return clamp(alpha) << 24 | clamp(red) << 16 | clamp(green) << 8 | clamp(blue);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
