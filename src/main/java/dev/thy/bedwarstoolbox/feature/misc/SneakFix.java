package dev.thy.bedwarstoolbox.feature.misc;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import net.minecraft.client.Minecraft;

public class SneakFix extends Feature {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private boolean unavailable;

    public SneakFix() {
        super(FeatureCategory.MISC);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (unavailable || !WindowsInputMethod.isWindows()) {
            return;
        }

        try {
            if (minecraft.thePlayer == null || minecraft.theWorld == null) {
                WindowsInputMethod.restoreIme();
                return;
            }

            if (minecraft.currentScreen == null) {
                WindowsInputMethod.disableImeForGameWindow();
            } else {
                WindowsInputMethod.restoreIme();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            unavailable = true;
            WindowsInputMethod.restoreIme();
        }
    }

    private static final class WindowsInputMethod {
        private static Pointer targetWindow;
        private static Pointer oldImeContext;
        private static boolean imeDisabled;

        private WindowsInputMethod() {
        }

        private static boolean isWindows() {
            return System.getProperty("os.name", "").toLowerCase().contains("win");
        }

        private static void disableImeForGameWindow() {
            Pointer window = User32.INSTANCE.GetForegroundWindow();
            if (window == null || Pointer.nativeValue(window) == 0L) {
                return;
            }

            if (imeDisabled && targetWindow != null && Pointer.nativeValue(targetWindow) == Pointer.nativeValue(window)) {
                return;
            }

            restoreIme();

            targetWindow = window;
            oldImeContext = Imm32.INSTANCE.ImmAssociateContext(window, Pointer.NULL);
            imeDisabled = true;
        }

        private static void restoreIme() {
            if (!imeDisabled) {
                return;
            }

            if (targetWindow != null && Pointer.nativeValue(targetWindow) != 0L) {
                Imm32.INSTANCE.ImmAssociateContext(targetWindow, oldImeContext);
            }

            targetWindow = null;
            oldImeContext = null;
            imeDisabled = false;
        }
    }

    private interface User32 extends Library {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

        Pointer GetForegroundWindow();
    }

    private interface Imm32 extends Library {
        Imm32 INSTANCE = (Imm32) Native.loadLibrary("imm32", Imm32.class);

        Pointer ImmAssociateContext(Pointer hWnd, Pointer hIMC);
    }
}