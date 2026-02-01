package com.multicolorshulkers.client;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import com.multicolorshulkers.MultiColorShulkers;

public class ClientConfigHelper {

    // Transient cached combos
    private static KeyCombo cachedTopCombo;
    private static KeyCombo cachedBottomCombo;

    // Default combos
    public static final KeyCombo DEFAULT_TOP_COMBO = new KeyCombo(
            InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_LEFT_SHIFT),
            InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
    );
    public static final KeyCombo DEFAULT_BOTTOM_COMBO = new KeyCombo(
            InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_LEFT_CONTROL),
            InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
    );

    public static KeyCombo getTopCombo(ModConfig config) {
        if (cachedTopCombo == null) {
            try {
                InputUtil.Key k1 = InputUtil.fromTranslationKey(config.topKey1);
                InputUtil.Key k2 = InputUtil.fromTranslationKey(config.topKey2);
                cachedTopCombo = new KeyCombo(k1, k2);
            } catch (Exception e) {
                MultiColorShulkers.LOGGER.warn("Invalid top combo, using default");
                cachedTopCombo = DEFAULT_TOP_COMBO;
            }
        }
        return cachedTopCombo;
    }

    public static void setTopCombo(ModConfig config, KeyCombo combo) {
        cachedTopCombo = combo;
        config.topKey1 = combo.getKey1().getTranslationKey();
        config.topKey2 = combo.getKey2().getTranslationKey();
    }

    public static KeyCombo getBottomCombo(ModConfig config) {
        if (cachedBottomCombo == null) {
            try {
                InputUtil.Key k1 = InputUtil.fromTranslationKey(config.bottomKey1);
                InputUtil.Key k2 = InputUtil.fromTranslationKey(config.bottomKey2);
                cachedBottomCombo = new KeyCombo(k1, k2);
            } catch (Exception e) {
                MultiColorShulkers.LOGGER.warn("Invalid bottom combo, using default");
                cachedBottomCombo = DEFAULT_BOTTOM_COMBO;
            }
        }
        return cachedBottomCombo;
    }

    public static void setBottomCombo(ModConfig config, KeyCombo combo) {
        cachedBottomCombo = combo;
        config.bottomKey1 = combo.getKey1().getTranslationKey();
        config.bottomKey2 = combo.getKey2().getTranslationKey();
    }
    
    public static void resetCache() {
        cachedTopCombo = null;
        cachedBottomCombo = null;
    }
}
