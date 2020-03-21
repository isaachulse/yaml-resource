package io.dimitris.simpleresource.utils;

import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import io.dimitris.simpleresource.model.YamlArray;
import io.dimitris.simpleresource.model.YamlElement;
import io.dimitris.simpleresource.model.YamlObject;
import io.dimitris.simpleresource.model.YamlPrimitive;

public class ResourcePrinter {

	public static void print(Resource resource) {
		for (EObject content : resource.getContents()) {
			print(content, 0);
		}
	}

	public static void print(EObject eObject, int indent) {
		String prefix = "";
		for (int i = 0; i < indent; i++)
			prefix += "  ";
		System.out.println(prefix + eObject);
		for (EObject content : eObject.eContents()) {
			print(content, indent + 1);
		}
	}

	public static void printYaml(YamlElement yamlElement) {

		if (yamlElement.isYamlObject()) {
			Set<Entry<String, YamlElement>> ens = ((YamlObject) yamlElement).entrySet();
			if (ens != null) {
				for (Entry<String, YamlElement> en : ens) {
					System.out.println(en.getKey() + " : ");
					printYaml(en.getValue());
				}
			}

		} else if (yamlElement.isYamlArray()) {
			YamlArray yamlArray = yamlElement.getAsYamlArray();
			for (YamlElement ye : yamlArray) {
				printYaml(ye);
			}
		}

		else if (yamlElement.isYamlNull()) {
			System.out.println("null");
		} else if (yamlElement.isYamlPrimitive()) {
			System.out.println(((YamlPrimitive) yamlElement).getAsString());
		}

	}

}
