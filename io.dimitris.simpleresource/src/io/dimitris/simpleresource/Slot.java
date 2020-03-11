package io.dimitris.simpleresource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public class Slot {
	
	public Slot(EObject eObject, EReference eReference) {
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
	
}
