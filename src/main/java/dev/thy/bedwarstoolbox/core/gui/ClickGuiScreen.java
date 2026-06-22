package dev.thy.bedwarstoolbox.core.gui;

import dev.thy.bedwarstoolbox.core.gui.component.GlobalSettingsPanel;
import dev.thy.bedwarstoolbox.core.gui.component.FeaturePanel;
import dev.thy.bedwarstoolbox.core.gui.component.GuiComponent;
import dev.thy.bedwarstoolbox.core.gui.font.TrueTypeFontRenderer;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends GuiScreen {
    private static final double SCALE_STEP = 0.05D;
    private static final double SCROLL_STEP = 12.0D;
    private static final int PAGE_SELECTOR_Y = 8;
    private static final int PAGE_BUTTON_SIZE = 18;
    private static final int PAGE_LABEL_WIDTH = 128;
    private static final int PAGE_SELECTOR_GAP = 4;
    private static Page lastActivePage = Page.CLICK_GUI;

    private final GuiManager guiManager;
    private final GuiScreen parentScreen;
    private final List<GuiComponent> components = new ArrayList<>();
    private Page activePage = lastActivePage;

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
        if (activePage == Page.CLICK_GUI) {
            components.add(new FeaturePanel(0, 34, 260, guiManager));
        } else if (activePage == Page.GLOBAL_SETTINGS) {
            components.add(new GlobalSettingsPanel(0, 34, 280, guiManager));
        }
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

        renderPageSelector();
        for (GuiComponent component : components) {
            component.render(mc, transformedMouseX, transformedMouseY, partialTicks);
        }

        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 || mouseButton == 1) {
            playClickSound();
        }

        if (handlePageSelectorClick(toLocalX(mouseX), toLocalY(mouseY), mouseButton)) {
            return;
        }

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

    private void renderPageSelector() {
        TrueTypeFontRenderer font = guiManager.getFontRenderer();
        int selectorWidth = PAGE_BUTTON_SIZE * 2 + PAGE_LABEL_WIDTH + PAGE_SELECTOR_GAP * 2;
        int startX = toLocalX(width / 2) - selectorWidth / 2;
        int selectorY = toLocalY(PAGE_SELECTOR_Y);
        int leftX = startX;
        int labelX = leftX + PAGE_BUTTON_SIZE + PAGE_SELECTOR_GAP;
        int rightX = labelX + PAGE_LABEL_WIDTH + PAGE_SELECTOR_GAP;
        int enabledArrowColor = guiManager.getAccentColor();
        int disabledArrowColor = 0xFF55575D;
        int labelColor = guiManager.getHeaderColor();
        String label = "<" + activePage.displayName + ">";

        drawRect(leftX, selectorY, leftX + PAGE_BUTTON_SIZE, selectorY + PAGE_BUTTON_SIZE, canMovePage(-1) ? enabledArrowColor : disabledArrowColor);
        drawRect(labelX, selectorY, labelX + PAGE_LABEL_WIDTH, selectorY + PAGE_BUTTON_SIZE, labelColor);
        drawRect(rightX, selectorY, rightX + PAGE_BUTTON_SIZE, selectorY + PAGE_BUTTON_SIZE, canMovePage(1) ? enabledArrowColor : disabledArrowColor);

        font.drawString("<", leftX + 6, selectorY + 3, 0xFFFFFFFF);
        font.drawString(label, labelX + (PAGE_LABEL_WIDTH - font.getStringWidth(label)) / 2, selectorY + 3, 0xFFFFFFFF);
        font.drawString(">", rightX + 6, selectorY + 3, 0xFFFFFFFF);
    }

    private boolean handlePageSelectorClick(int mouseX, int mouseY, int mouseButton) {
        int selectorY = toLocalY(PAGE_SELECTOR_Y);
        if (mouseButton != 0 || mouseY < selectorY || mouseY >= selectorY + PAGE_BUTTON_SIZE) {
            return false;
        }

        int selectorWidth = PAGE_BUTTON_SIZE * 2 + PAGE_LABEL_WIDTH + PAGE_SELECTOR_GAP * 2;
        int startX = toLocalX(width / 2) - selectorWidth / 2;
        int leftX = startX;
        int labelX = leftX + PAGE_BUTTON_SIZE + PAGE_SELECTOR_GAP;
        int rightX = labelX + PAGE_LABEL_WIDTH + PAGE_SELECTOR_GAP;

        if (mouseX >= leftX && mouseX < leftX + PAGE_BUTTON_SIZE) {
            movePage(-1);
            return true;
        }
        if (mouseX >= rightX && mouseX < rightX + PAGE_BUTTON_SIZE) {
            movePage(1);
            return true;
        }

        return mouseX >= labelX && mouseX < labelX + PAGE_LABEL_WIDTH;
    }

    private boolean canMovePage(int direction) {
        int index = activePage.ordinal() + direction;
        return index >= 0 && index < Page.values().length;
    }

    private void movePage(int direction) {
        if (!canMovePage(direction)) {
            return;
        }

        activePage = Page.values()[activePage.ordinal() + direction];
        lastActivePage = activePage;
        initGui();
    }

    private void playClickSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    private enum Page {
        CLICK_GUI("ClickGui"),
        GLOBAL_SETTINGS("Global Settings");

        private final String displayName;

        Page(String displayName) {
            this.displayName = displayName;
        }
    }
}
