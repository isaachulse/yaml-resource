package io.dimitris.simpleresource.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class YamlArray extends YamlElement implements Iterable<YamlElement> {
	public final List<YamlElement> elements; // TODO set back to private

	public YamlArray() {
		elements = new ArrayList<YamlElement>();
	}

	public void add(YamlElement element) {
		if (element == null) {
			element = YamlNull.INSTANCE;
		}
		elements.add(element);
	}

	public int size() {
		return elements.size();
	}

	@Override
	public Iterator<YamlElement> iterator() {
		return elements.iterator();
	}

	public YamlElement get(int i) {
		return elements.get(i);
	}
	
	@Override
	public String getIdentifier() {
		return elements.get(0).getIdentifier();
	}
}