package io.dimitris.simpleresource;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public class EAttributeSlot {

	protected EAttribute eAttribute;
	protected EObject eObject;

	public EAttribute getEAttribute() {
		return eAttribute;
	}

	public void setEAttribute(EAttribute eAttribute) {
		this.eAttribute = eAttribute;
	}

	public EObject getEObject() {
		return eObject;
	}

	public void setEObject(EObject eObject) {
		this.eObject = eObject;
	}

	public void newValue(String value) {

	}

	@Override
	public String toString() {
		String eObjectName = ((EClass) eObject.eClass()).getName().toString();
		return "EAttributeSlot={" + eObjectName + " -> " + eAttribute.getName() + "}";
	}
}