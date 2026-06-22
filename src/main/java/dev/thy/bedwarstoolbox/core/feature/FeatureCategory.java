package dev.thy.bedwarstoolbox.core.feature;

public enum FeatureCategory {
    COMBAT("Combat"),
    RENDER("Render");

    private final String displayName;

    FeatureCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
