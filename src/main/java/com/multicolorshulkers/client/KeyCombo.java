package com.multicolorshulkers.client;

import net.minecraft.client.util.InputUtil;

import java.util.Objects;

/**
 * Represents a combination of two keys/mouse buttons.
 */
public class KeyCombo {
	private final InputUtil.Key key1;
	private final InputUtil.Key key2;

	public KeyCombo(InputUtil.Key key1, InputUtil.Key key2) {
		this.key1 = key1;
		this.key2 = key2;
	}

	public InputUtil.Key getKey1() {
		return key1;
	}

	public InputUtil.Key getKey2() {
		return key2;
	}

	public String getDisplayName() {
		return getKeyName(key1) + " + " + getKeyName(key2);
	}

	private String getKeyName(InputUtil.Key key) {
		String name = key.getLocalizedText().getString();
		if (name.startsWith("Button ")) {
			return name.replace("Button ", "Mouse ");
		}
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		KeyCombo keyCombo = (KeyCombo) o;
		return Objects.equals(key1, keyCombo.key1) && Objects.equals(key2, keyCombo.key2);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key1, key2);
	}
}
