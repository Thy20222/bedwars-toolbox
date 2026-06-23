package dev.thy.bedwarstoolbox.feature.combat;

import dev.thy.bedwarstoolbox.core.config.BooleanSetting;
import dev.thy.bedwarstoolbox.core.config.NumberSetting;
import dev.thy.bedwarstoolbox.core.animation.BlockHitAnimationState;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

public class BlockHit extends Feature {
    private static BlockHit instance;

    private final BooleanSetting blockHitAnimation = new BooleanSetting("BlockHit Animation", true);
    private final BooleanSetting fakeMineAnimation = new BooleanSetting("Fake Mine Animation", true);
    private final NumberSetting restartProgress = new NumberSetting("Restart Progress", "Fake mine progress needed before another fake mine can start", 0.85D, 0.0D, 1.0D);
    private boolean fakeSwingStartedThisHold;

    public BlockHit() {
        super(FeatureCategory.COMBAT);
        instance = this;
        registerSetting(blockHitAnimation);
        registerSetting(fakeMineAnimation);
        registerSetting(restartProgress);
    }

    @Override
    public void onTick() {
        BlockHitAnimationState.tick();
        if (!Mouse.isButtonDown(0)) {
            fakeSwingStartedThisHold = false;
        }
    }

    @Override
    public void onDisable() {
        BlockHitAnimationState.reset();
        fakeSwingStartedThisHold = false;
    }

    public static boolean handleClickMouse(MovingObjectPosition target) {
        if (!isCancellingRealClicks()) {
            return false;
        }

        if (target != null && target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            startFakeMine();
        } else {
            BlockHitAnimationState.stopFakeMine();
            startFakeSwing();
        }

        return true;
    }

    public static boolean handleClickBlock(boolean leftClick) {
        if (!leftClick || !isCancellingRealClicks()) {
            return false;
        }

        if (isLookingAtBlock()) {
            startFakeMine();
        } else {
            BlockHitAnimationState.stopFakeMine();
            startFakeSwing();
        }

        return true;
    }

    public static float getRenderSwingProgress(float vanillaSwingProgress) {
        if (!isUsingItem()) {
            return vanillaSwingProgress;
        }

        if (isLookingAtBlock() && BlockHitAnimationState.isFakeMineActive()) {
            return Math.max(vanillaSwingProgress, BlockHitAnimationState.getFakeMineProgress());
        }

        if (BlockHitAnimationState.isFakeSwingActive()) {
            return Math.max(vanillaSwingProgress, BlockHitAnimationState.getFakeSwingProgress());
        }

        return vanillaSwingProgress;
    }

    public static boolean shouldRenderBlockHitOverlay() {
        return isBlocking() && BlockHitAnimationState.isFakeSwingActive();
    }

    public static float getBlockHitOverlayProgress() {
        return BlockHitAnimationState.getFakeSwingProgress();
    }

    private static void startFakeSwing() {
        if (instance == null || !instance.blockHitAnimation.getValue()) {
            return;
        }

        if (instance.fakeSwingStartedThisHold) {
            return;
        }

        BlockHitAnimationState.startFakeSwing();
        instance.fakeSwingStartedThisHold = true;
    }

    private static void startFakeMine() {
        if (instance != null
                && instance.fakeMineAnimation.getValue()
                && BlockHitAnimationState.canRestartFakeMine(instance.restartProgress.getValue())) {
            BlockHitAnimationState.startFakeMine();
        }
    }

    private static boolean isCancellingRealClicks() {
        return instance != null && instance.isEnabled() && isUsingItem();
    }

    private static boolean isUsingItem() {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.thePlayer;
        return player != null && player.isUsingItem();
    }

    private static boolean isLookingAtBlock() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft.objectMouseOver != null
                && minecraft.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    private static boolean isBlocking() {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.thePlayer;
        if (player == null || !player.isUsingItem()) {
            return false;
        }

        ItemStack heldItem = player.getHeldItem();
        return heldItem != null && heldItem.getItemUseAction() == EnumAction.BLOCK;
    }
}
