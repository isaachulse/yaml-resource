package io.dimitris.simpleresource.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class YamlObject extends YamlElement {
	private final ConcurrentHashMap<String, YamlElement> members = new ConcurrentHashMap<String, YamlElement>();

	public boolean hasChildren() {
		return !this.members.isEmpty();
	}

	public int childCount() {
		return members.size();
	}

	public Map<String, YamlElement> getChildren() throws Exception {
		return members;
	}

	public Entry<String, YamlElement> getChild() {
		return members.entrySet().iterator().next();
	}

	public void add(String property, YamlElement value) {
		members.put(property, value == null ? YamlNull.INSTANCE : value);
	}

	public YamlElement remove(String key) {
		return members.remove(key);
	}

	public Set<Map.Entry<String, YamlElement>> entrySet() {
		return members.entrySet();
	}

	public YamlElement get(String memberName) {
		return members.get(memberName);
	}

	public String getSingleName() {
		if (keySet().size() == 1) {
			return (keySet().toArray(new String[keySet().size()]))[0];
		}
		return "";
	}

	public Set<String> keySet() {
		return members.keySet();
	}

	public int size() {
		return members.size();
	}

	public boolean has(String memberName) {
		return members.containsKey(memberName);
	}

	@Override
	public boolean equals(Object o) {
		return (o == this) || (o instanceof YamlObject && ((YamlObject) o).members.equals(members));
	}

}