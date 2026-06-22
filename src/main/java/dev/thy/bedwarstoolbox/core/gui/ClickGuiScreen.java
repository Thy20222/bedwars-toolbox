package dev.thy.bedwarstoolbox.core.gui;

import dev.thy.bedwarstoolbox.core.gui.component.FeaturePanel;
import dev.thy.bedwarstoolbox.core.gui.component.GuiComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends GuiScreen {
    private static final double SCALE_STEP = 0.05D;
    private static final double SCROLL_STEP = 12.0D;

    private final GuiManager guiManager;
    private final GuiScreen parentScreen;
    private final List<GuiComponent> components = new ArrayList<>();

    public ClickGuiScreen(GuiManager guiManager) {
        this(null, guiManager);
    }

    public ClickGuiScreen(GuiScreen parentScreen, GuiManager guiManager) {
        this.parentScreen = parentScreen;
        this.guiManager = guiManager;
    }

    @Override
    public void initGui() {
        components.clear();
        components.add(new FeaturePanel(0, 0, 240, guiManager.getFeatureManager()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        double scale = guiManager.getClickGuiScale();
        int transformedMouseX = toLocalX(mouseX);
        int transformedMouseY = toLocalY(mouseY);

        GlStateManager.pushMatrix();
        GlStateManager.translate(guiManager.getClickGuiX(), guiManager.getClickGuiY(), 0.0D);
        GlStateManager.scale(scale, scale, 1.0D);

        for (GuiComponent component : components) {
            component.render(mc, transformedMouseX, transformedMouseY, partialTicks);
        }

        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (GuiComponent component : components) {
            component.mouseClicked(toLocalX(mouseX), toLocalY(mouseY), mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (GuiComponent component : components) {
            component.mouseReleased(toLocalX(mouseX), toLocalY(mouseY), state);
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int direction = wheel > 0 ? 1 : -1;
            if (isCtrlDown()) {
                guiManager.setClickGuiScale(guiManager.getClickGuiScale() + direction * SCALE_STEP);
            } else if (isShiftDown()) {
                guiManager.setClickGuiX(guiManager.getClickGuiX() - direction * SCROLL_STEP);
            } else {
                guiManager.setClickGuiY(guiManager.getClickGuiY() + direction * SCROLL_STEP);
            }
        }

        super.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE && parentScreen != null) {
            mc.displayGuiScreen(parentScreen);
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private int toLocalX(int mouseX) {
        return (int) Math.round((mouseX - guiManager.getClickGuiX()) / guiManager.getClickGuiScale());
    }

    private int toLocalY(int mouseY) {
        return (int) Math.round((mouseY - guiManager.getClickGuiY()) / guiManager.getClickGuiScale());
    }

    private boolean isCtrlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    private boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }
}
