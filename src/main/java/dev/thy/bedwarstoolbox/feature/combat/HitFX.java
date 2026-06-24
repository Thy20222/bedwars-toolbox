package dev.thy.bedwarstoolbox.feature.combat;

import dev.thy.bedwarstoolbox.core.Global;
import dev.thy.bedwarstoolbox.core.event.AttackEntityEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;

public class HitFX extends Feature implements Global {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    public HitFX() {
        super(FeatureCategory.COMBAT);
    }

    @Subscribe
    public void onAttackEntity(AttackEntityEvent event) {
        if (!isEnabled() || minecraft.theWorld == null || mc.thePlayer == null) {
            return;
        }

        if (event.getAttacker() != mc.thePlayer || event.getTarget() == null) {
            return;
        }

        minecraft.effectRenderer.emitParticleAtEntity(event.getTarget(), EnumParticleTypes.CRIT_MAGIC);
    }
}
