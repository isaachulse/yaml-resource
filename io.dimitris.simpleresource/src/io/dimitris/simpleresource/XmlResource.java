package io.dimitris.simpleresource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class XmlResource extends ResourceImpl {
	
	
//	public static void main(String[] args) throws Exception {
//		
//		// Load metamodel
//		ResourceSet metamodelResourceSet = new ResourceSetImpl();
//		metamodelResourceSet.getResourceFactoryRegistry().
//			getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
//		Resource metamodelResource = metamodelResourceSet.getResource(
//			URI.createURI(new File("simpledsl.ecore").toURI().toString()), true);
//		metamodelResource.load(null);
//		EPackage metamodelEPackage = (EPackage) metamodelResource.getContents().get(0);
//		
//		// Set up the model resource
//		ResourceSet modelResourceSet = new ResourceSetImpl();
//		modelResourceSet.getPackageRegistry().put(metamodelEPackage.getNsURI(), metamodelEPackage);
//		modelResourceSet.getResourceFactoryRegistry().
//			getExtensionToFactoryMap().put("*", new XmlResourceFactory());
//		Resource modelResource = modelResourceSet.getResource(
//				URI.createURI(new File("simpledsl.xml").toURI().toString()), true);
//		modelResource.load(null);
//		
//		// Print the contents of the resource
//		new ResourcePrinter().print(modelResource);
//	}
	
	protected Stack<Object> stack = new Stack<Object>();
	
	public XmlResource(URI uri) {
		super(uri);
	}

	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(inputStream);
			process(document.getRootElement());
			
		} catch (DocumentException e) {
			throw new IOException(e);
		}
	}
	
	// This code only handles containment EReferences
	// It doesn't handle EAttributes or non-containment EReferences
	protected void process(Element element) {
		
		// We assume that there is only one EPackage in the resource's resourceSet registry
		EPackage ePackage = (EPackage) getResourceSet().getPackageRegistry().values().iterator().next();
		
		
		if (stack.isEmpty()) { // If the stack is empty, it means that we are in the root element of the XML document
			// We find the EClass that matches the name of the element
			EClass eClass = (EClass) ePackage.getEClassifier(element.getName());
			// ... and create an instance of it
			EObject eObject = ePackage.getEFactoryInstance().create(eClass);
			// We put the instance in the contents of the resource as a top-level element
			getContents().add(eObject);
			// ... and we push it to the stack
			stack.push(eObject);
		}
		else if (stack.peek() instanceof EObject) { // If the top element of the stack is an EObject, this element refers to one of the EReferences of its EClass
			// We get hold of the EObject that's a the top of the stack
			EObject eObject = (EObject) stack.peek();
			// We get hold of the EReference of its EClass with that name
			EReference eReference = (EReference) eObject.eClass().getEStructuralFeature(element.getName());
			// We don't create any new EObjects at this stage; just put a "slot" containing the EObject and the EReference in the stack
			stack.push(new Slot(eObject, eReference));
		}
		else if (stack.peek() instanceof Slot) { // If the top element of the stack is a slot, the current element is an EObject that should be added to the values of that slot
			// We get hold of the slot, and the EReference and EObject it encapsulates
			Slot slot = (Slot) stack.peek();
			EReference eReference = slot.getEReference();
			EObject eObject = slot.getEObject();
			
			// Check that the EReference is a containment reference
			if (!eReference.isContainment()) throw new RuntimeException("Can only handle containment references at the moment");
			
			// We find the EClass that matches the name of our current XML element
			EClass valueEClass = (EClass) ePackage.getEClassifier(element.getName());
			// ... and create an instance of it (our new value for the slot)
			EObject valueEObject = ePackage.getEFactoryInstance().create(valueEClass);
			
			
			if (eReference.isMany()) { // If the EReference of the slot is multi-valued
				// ... we get hold of existing values
				Collection<Object> existingValues = (Collection<Object>) eObject.eGet(eReference);
				// ... and add our new value to them
				existingValues.add(valueEObject);
			}
			else { // If the EReference of the slot is single-valued
				// ... we assign the value of the reference to our new value 
				eObject.eSet(eReference, valueEObject);
			}
			
			// Finally, we push the new object to the stack
			stack.push(valueEObject);
		}
		
		// We process recursively all the children of the current XML element
		for (Element child : element.elements()) {
			process(child);
		}
		
		// We pop the top object of the stack
		if (!stack.isEmpty()) stack.pop();
		
	}
	
}
