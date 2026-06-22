package dev.thy.bedwarstoolbox.mixin;

import dev.thy.bedwarstoolbox.BedwarsToolbox;
import dev.thy.bedwarstoolbox.core.event.AttackEntityEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerControllerMP.class)
public class PlayerControllerMPMixin {
    @Inject(method = "attackEntity", at = @At("TAIL"))
    private void bedwarstoolbox$attackEntity(EntityPlayer player, Entity target, CallbackInfo callbackInfo) {
        if (BedwarsToolbox.INSTANCE != null && BedwarsToolbox.INSTANCE.getEventBus() != null) {
            BedwarsToolbox.INSTANCE.getEventBus().post(new AttackEntityEvent(player, target));
        }
    }
}
