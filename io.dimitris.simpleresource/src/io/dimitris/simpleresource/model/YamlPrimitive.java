package io.dimitris.simpleresource.model;

import io.dimitris.simpleresource.utils.Utilities;

public final class YamlPrimitive extends YamlElement {

	private final Object value;

	public YamlPrimitive(Boolean bool) {
		value = Utilities.checkNotNull(bool);
	}

	public YamlPrimitive(Number number) {
		value = Utilities.checkNotNull(number);
	}

	public YamlPrimitive(String string) {
		value = Utilities.checkNotNull(string);
	}

	public YamlPrimitive(Character c) {
		value = Utilities.checkNotNull(c).toString();
	}

	public boolean isBoolean() {
		return value instanceof Boolean;
	}

	public boolean isNumber() {
		return value instanceof Number;
	}

	public boolean isString() {
		return value instanceof String;
	}

	public Object getValue() {

		if (value instanceof String) {
			return getAsString();
		} else if (value instanceof Boolean) {
			return getAsBoolean();
		} else if (value instanceof Number) {
			return getAsNumber();
		}
		return (String) value.toString();
	}

	public boolean getAsBoolean() {
		if (isBoolean()) {
			return ((Boolean) value).booleanValue();
		}
		return Boolean.parseBoolean(getAsString());
	}

	public Number getAsNumber() {
		return value instanceof String ? Integer.parseInt(getAsString()) : (Number) value;
	}

	public String getAsString() {
		if (isNumber()) {
			return getAsNumber().toString();
		} else if (isBoolean()) {
			return ((Boolean) value).toString();
		} else {
			return (String) value;
		}
	}

}