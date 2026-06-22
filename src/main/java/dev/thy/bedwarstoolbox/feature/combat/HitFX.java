package dev.thy.bedwarstoolbox.feature.combat;

import dev.thy.bedwarstoolbox.core.event.AttackEntityEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;

public class HitFX extends Feature {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    public HitFX() {
        super(FeatureCategory.RENDER);
    }

    @Subscribe
    public void onAttackEntity(AttackEntityEvent event) {
        if (!isEnabled() || minecraft.theWorld == null || minecraft.thePlayer == null) {
            return;
        }

        if (event.getAttacker() != minecraft.thePlayer || event.getTarget() == null) {
            return;
        }

        minecraft.effectRenderer.emitParticleAtEntity(event.getTarget(), EnumParticleTypes.CRIT_MAGIC);
    }
}
