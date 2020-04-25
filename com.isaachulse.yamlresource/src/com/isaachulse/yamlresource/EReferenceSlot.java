package com.isaachulse.yamlresource;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public class EReferenceSlot {

	public EReferenceSlot(EObject eObject, EReference eReference) {
		super();
		this.eObject = eObject;
		this.eReference = eReference;
	}

	protected EObject eObject;
	protected EReference eReference;

	public EObject getEObject() {
		return eObject;
	}

	public void setEObject(EObject eObject) {
		this.eObject = eObject;
	}

	public EReference getEReference() {
		return eReference;
	}

	public void setEReference(EReference eReference) {
		this.eReference = eReference;
	}

	@Override
	public String toString() {
		String eObjectName = ((EClass) eObject.eClass()).getName().toString();
		String eReferenceName = eReference.getName();
		return "EReferenceSlot={" + eObjectName + " -> " + eReferenceName + "}";
	}

}
