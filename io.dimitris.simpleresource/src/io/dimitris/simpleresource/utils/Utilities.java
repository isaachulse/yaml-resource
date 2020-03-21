package io.dimitris.simpleresource.utils;

import java.util.ArrayList;
import java.util.Stack;

import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;

import io.dimitris.simpleresource.EReferenceSlot;

public class Utilities {

	public static <T> T checkNotNull(T obj) {
		if (obj == null) {
			throw new NullPointerException();
		}
		return obj;
	}
	
	public static String formatStack(Stack<Object> stack) {
		ArrayList<String> stringArray = new ArrayList();

		stack.forEach(item -> {
			if (item instanceof EReferenceSlot) {
				stringArray.add(item.toString());
			} else if (item instanceof DynamicEObjectImpl) {
				String eObjectName = (((DynamicEObjectImpl) item).eClass()).getName().toString();
				stringArray.add("EObject={" + eObjectName + "}");
			}
		});
		return stringArray.toString();
	}
}
