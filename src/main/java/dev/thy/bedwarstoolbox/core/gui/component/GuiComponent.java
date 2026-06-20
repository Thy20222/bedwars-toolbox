package dev.thy.bedwarstoolbox.core.gui.component;

import net.minecraft.client.Minecraft;

public abstract class GuiComponent {
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public GuiComponent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(Minecraft minecraft, int mouseX, int mouseY, float partialTicks);

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    protected boolean isMouseInside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
