package dev.thy.bedwarstoolbox.core.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class AttackEntityEvent extends Event {
    private final EntityPlayer attacker;
    private final Entity target;

    public AttackEntityEvent(EntityPlayer attacker, Entity target) {
        this.attacker = attacker;
        this.target = target;
    }

    public EntityPlayer getAttacker() {
        return attacker;
    }

    public Entity getTarget() {
        return target;
    }
}
