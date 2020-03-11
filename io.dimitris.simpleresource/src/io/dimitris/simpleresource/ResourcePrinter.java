package io.dimitris.simpleresource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public class ResourcePrinter {
	
	public void print(Resource resource) {
		for (EObject content : resource.getContents()) {
			print(content, 0);
		}
	}
	
	public void print(EObject eObject, int indent) {
		String prefix = "";
		for (int i=0; i<indent; i++) prefix += "  ";
		System.out.println(prefix + eObject);
		for (EObject content : eObject.eContents()) {
			print(content, indent + 1);
		}
	}
	
}
