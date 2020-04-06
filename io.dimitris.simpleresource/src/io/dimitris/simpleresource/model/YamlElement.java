package io.dimitris.simpleresource.model;

import java.lang.reflect.Array;
import java.util.ArrayList;

public abstract class YamlElement {

	public boolean hasAttributes() {
		//TODO
		return false;
	}
	
	public boolean isYamlArray() {
		return this instanceof YamlArray;
	}

	public boolean isYamlObject() {
		return this instanceof YamlObject;
	}

	public boolean isYamlPrimitive() {
		return this instanceof YamlPrimitive;
	}

	public boolean isYamlNull() {
		return this instanceof YamlNull;
	}

	public YamlObject getAsYamlObject() {
		if (isYamlObject()) {
			return (YamlObject) this;
		}
		throw new IllegalStateException("Not a YAML Object: " + this);
	}

	public YamlArray getAsYamlArray() {
		if (isYamlArray()) {
			return (YamlArray) this;
		}
		throw new IllegalStateException("Not a YAML Array: " + this);
	}

	public YamlPrimitive getAsYamlPrimitive() {
		if (isYamlPrimitive()) {
			return (YamlPrimitive) this;
		}
		throw new IllegalStateException("Not a YAML Primitive: " + this);
	}

	public YamlNull getAsYamlNull() {
		if (isYamlNull()) {
			return (YamlNull) this;
		}
		throw new IllegalStateException("Not a YAML Null: " + this);
	}

	public boolean getAsBoolean() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}
	
	public String getIdentifier() {
		return getAsYamlObject().getSingleName();
	}
	
	public boolean hasChildren() {
		// TODO
		return true;
	}
	
	public ArrayList<YamlElement> getChildren() {
		// TODO
		return new ArrayList<YamlElement>();
	}

}