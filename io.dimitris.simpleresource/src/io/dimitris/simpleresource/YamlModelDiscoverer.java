package io.dimitris.simpleresource;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import io.dimitris.simpleresource.model.YamlArray;
import io.dimitris.simpleresource.model.YamlElement;
import io.dimitris.simpleresource.model.YamlNull;
import io.dimitris.simpleresource.model.YamlObject;
import io.dimitris.simpleresource.model.YamlPrimitive;

class YamlModelDiscoverer {

	// Maps YAML elements to values
	static YamlElement wrapYamlObject(Object o) {

		// Null transformed to YamlNull
		if (o == null)
			return YamlNull.INSTANCE;

		// Collection transformed to YamlArray
		if (o instanceof Collection) {
			YamlArray yamlArray = new YamlArray();

			for (Object child : (Collection<?>) o) {
				yamlArray.add(wrapYamlObject(child));
			}

			return yamlArray;
		}

		// Array transformed to YamlArray
		if (o.getClass().isArray()) {
			YamlArray yamlArray = new YamlArray();

			for (int i = 0; i < Array.getLength(yamlArray); i++) {
				yamlArray.add(wrapYamlObject(Array.get(yamlArray, i)));
			}

			return yamlArray;
		}

		// Map transformed to YamlObject
		if (o instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) o;
			YamlObject yamlObject = new YamlObject();

			for (final Map.Entry<?, ?> entry : map.entrySet()) {
				final String name = String.valueOf(entry.getKey());
				final Object value = entry.getValue();
				yamlObject.add(name, wrapYamlObject(value));
			}

			return yamlObject;
		}

		// Everything else transformed to YamlPrimitive
		if (o instanceof String) {
			return new YamlPrimitive((String) o);
		} else if (o instanceof Number) {
			return new YamlPrimitive((Number) o);
		} else if (o instanceof Character) {
			return new YamlPrimitive((Character) o);
		} else if (o instanceof Boolean) {
			return new YamlPrimitive((Boolean) o);
		} else {
			// Default to String if we can't find anything else
			return new YamlPrimitive(String.valueOf(o));
		}

	}
}
