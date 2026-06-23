package dev.thy.bedwarstoolbox.core.animation;

public final class BlockHitAnimationState {
    private static final int ANIMATION_DURATION = 6;
    private static int fakeSwingTicks;
    private static int fakeMineTicks;
    private static float renderPartialTicks;

    private BlockHitAnimationState() {
    }

    public static void startFakeSwing() {
        fakeSwingTicks = ANIMATION_DURATION;
    }

    public static void startFakeMine() {
        fakeMineTicks = ANIMATION_DURATION;
    }

    public static void stopFakeMine() {
        fakeMineTicks = 0;
    }

    public static void tick() {
        if (fakeSwingTicks > 0) {
            fakeSwingTicks--;
        }

        if (fakeMineTicks > 0) {
            fakeMineTicks--;
        }
    }

    public static void reset() {
        fakeSwingTicks = 0;
        fakeMineTicks = 0;
    }

    public static void setRenderPartialTicks(float partialTicks) {
        renderPartialTicks = partialTicks;
    }

    public static boolean isFakeSwingActive() {
        return fakeSwingTicks > 0;
    }

    public static boolean isFakeMineActive() {
        return fakeMineTicks > 0;
    }

    public static boolean canRestartFakeMine(double restartProgress) {
        return canRestart(fakeMineTicks, restartProgress);
    }

    public static float getFakeSwingProgress() {
        return getFakeSwingProgress(renderPartialTicks);
    }

    public static float getFakeSwingProgress(float partialTicks) {
        return getProgress(fakeSwingTicks, partialTicks);
    }

    public static float getFakeMineProgress() {
        return getFakeMineProgress(renderPartialTicks);
    }

    public static float getFakeMineProgress(float partialTicks) {
        return getProgress(fakeMineTicks, partialTicks);
    }

    private static float getProgress(int ticks, float partialTicks) {
        if (ticks <= 0) {
            return 0.0F;
        }

        float progress = 1.0F - (ticks - partialTicks) / (float) ANIMATION_DURATION;
        return Math.max(0.0F, Math.min(1.0F, progress));
    }

    private static boolean canRestart(int ticks, double restartProgress) {
        if (ticks <= 0) {
            return true;
        }

        double clampedProgress = Math.max(0.0D, Math.min(1.0D, restartProgress));
        int restartTicks = Math.max(1, (int) Math.ceil(ANIMATION_DURATION * (1.0D - clampedProgress)));
        return ticks <= restartTicks;
    }
}
