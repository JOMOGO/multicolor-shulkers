package com.multicolorshulkers.client;

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class KeyComboEntry extends TooltipListEntry<KeyCombo> {
	private final KeyCombo defaultValue;
	private KeyCombo value;
	private final Consumer<KeyCombo> saveConsumer;
	private final ButtonWidget buttonWidget;
	private final ButtonWidget resetButton;
	private boolean listening = false;
	private InputUtil.Key firstKey = null;

	public KeyComboEntry(Text fieldName, KeyCombo value, KeyCombo defaultValue, Consumer<KeyCombo> saveConsumer, Text tooltipText) {
		super(fieldName, () -> Optional.ofNullable(tooltipText == null ? null : new Text[]{tooltipText}));
		this.value = value;
		this.defaultValue = defaultValue;
		this.saveConsumer = saveConsumer;

		this.buttonWidget = ButtonWidget.builder(getDisplayText(), button -> {
			listening = true;
			firstKey = null;
		}).dimensions(0, 0, 150, 20).build();

		this.resetButton = ButtonWidget.builder(Text.literal("Reset"), button -> {
			this.value = defaultValue;
			listening = false;
			firstKey = null;
		}).dimensions(0, 0, 40, 20).build();
	}

	private Text getDisplayText() {
		if (listening) {
			if (firstKey == null) {
				return Text.literal("> Press key 1 <");
			} else {
				return Text.literal("> " + getKeyName(firstKey) + " + different key <");
			}
		}
		return Text.literal(value.getDisplayName());
	}

	private String getKeyName(InputUtil.Key key) {
		String name = key.getLocalizedText().getString();
		// Clean up some names
		if (name.startsWith("Button ")) {
			return name.replace("Button ", "Mouse ");
		}
		return name;
	}

	@Override
	public KeyCombo getValue() {
		return value;
	}

	@Override
	public Optional<KeyCombo> getDefaultValue() {
		return Optional.of(defaultValue);
	}

	@Override
	public void save() {
		if (saveConsumer != null) {
			saveConsumer.accept(value);
		}
	}

	@Override
	public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
		super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);

		// Draw field name
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, getFieldName(), x, y + 6, 0xFFFFFF);

		// Position and render button
		buttonWidget.setX(x + entryWidth - 150 - 2 - 40 - 4);
		buttonWidget.setY(y);
		buttonWidget.setMessage(getDisplayText());
		buttonWidget.render(context, mouseX, mouseY, delta);

		// Position and render reset button
		resetButton.setX(x + entryWidth - 40 - 2);
		resetButton.setY(y);
		resetButton.active = !value.equals(defaultValue);
		resetButton.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (listening && keyCode != GLFW.GLFW_KEY_ESCAPE) {
			InputUtil.Key key = InputUtil.Type.KEYSYM.createFromCode(keyCode);
			handleKeyInput(key);
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ESCAPE && listening) {
			listening = false;
			firstKey = null;
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (listening) {
			InputUtil.Key key = InputUtil.Type.MOUSE.createFromCode(button);
			handleKeyInput(key);
			return true;
		}

		if (buttonWidget.isMouseOver(mouseX, mouseY)) {
			buttonWidget.onPress();
			return true;
		}
		if (resetButton.isMouseOver(mouseX, mouseY) && resetButton.active) {
			resetButton.onPress();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void handleKeyInput(InputUtil.Key key) {
		if (firstKey == null) {
			firstKey = key;
		} else {
			// Check if same key pressed twice
			if (firstKey.equals(key)) {
				// Reject - same key can't be used twice, keep listening for different key
				return;
			}
			// Second key pressed - save combo
			value = new KeyCombo(firstKey, key);
			listening = false;
			firstKey = null;
		}
	}

	@Override
	public List<? extends Element> children() {
		return List.of(buttonWidget, resetButton);
	}

	@Override
	public List<? extends Selectable> narratables() {
		return List.of(buttonWidget, resetButton);
	}
}
