package dev.thy.bedwarstoolbox.mixin;

import dev.thy.bedwarstoolbox.feature.combat.BlockHit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void bedwarstoolbox$clickMouse(CallbackInfo callbackInfo) {
        if (BlockHit.handleClickMouse(objectMouseOver)) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"), cancellable = true)
    private void bedwarstoolbox$sendClickBlockToController(boolean leftClick, CallbackInfo callbackInfo) {
        if (BlockHit.handleClickBlock(leftClick)) {
            callbackInfo.cancel();
        }
    }
}
