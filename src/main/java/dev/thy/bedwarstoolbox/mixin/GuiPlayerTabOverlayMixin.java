package dev.thy.bedwarstoolbox.mixin;

import dev.thy.bedwarstoolbox.feature.render.BedwarsOverlay;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiPlayerTabOverlay.class)
public class GuiPlayerTabOverlayMixin {
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void bedwarstoolbox$getPlayerName(NetworkPlayerInfo playerInfo, CallbackInfoReturnable<String> callbackInfo) {
        if (playerInfo == null || playerInfo.getGameProfile() == null) {
            return;
        }

        String suffix = BedwarsOverlay.getTabSuffix(playerInfo.getGameProfile().getName());
        if (suffix != null && !suffix.isEmpty()) {
            callbackInfo.setReturnValue(callbackInfo.getReturnValue() + " \u00a77" + suffix);
        }
    }
}
