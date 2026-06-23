package dev.thy.bedwarstoolbox.mixin;

import dev.thy.bedwarstoolbox.feature.combat.BlockHit;
import dev.thy.bedwarstoolbox.core.animation.BlockHitAnimationState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void bedwarstoolbox$captureRenderPartialTicks(float partialTicks, CallbackInfo callbackInfo) {
        BlockHitAnimationState.setRenderPartialTicks(partialTicks);
    }

    @ModifyArg(
            method = "renderItemInFirstPerson",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V"),
            index = 1
    )
    private float bedwarstoolbox$useFakeSwingProgress(float swingProgress) {
        return BlockHit.getRenderSwingProgress(swingProgress);
    }

    @Inject(method = "doBlockTransformations", at = @At("TAIL"))
    private void bedwarstoolbox$applyBlockHitOverlay(CallbackInfo callbackInfo) {
        if (!BlockHit.shouldRenderBlockHitOverlay()) {
            return;
        }

        float progress = BlockHit.getBlockHitOverlayProgress();
        float sin = MathHelper.sin(progress * progress * (float) Math.PI);
        float sqrtSin = MathHelper.sin(MathHelper.sqrt_float(progress) * (float) Math.PI);

        GlStateManager.translate(-sin * 0.12F, 0.03F, -sqrtSin * 0.18F);
        GlStateManager.rotate(sqrtSin * 18.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-sin * 12.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-sin * 8.0F, 1.0F, 0.0F, 0.0F);
    }
}
