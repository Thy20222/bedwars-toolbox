package dev.thy.bedwarstoolbox.core.event;

import net.minecraft.client.entity.AbstractClientPlayer;

public class RenderNameTagEvent extends Event {
    private final AbstractClientPlayer player;
    private final double x;
    private final double y;
    private final double z;

    public RenderNameTagEvent(AbstractClientPlayer player, double x, double y, double z) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public AbstractClientPlayer getPlayer() {
        return player;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
