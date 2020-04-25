package com.isaachulse.yamlresource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Stack;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.yaml.snakeyaml.Yaml;

public class YamlResource extends ResourceImpl {

	// persistent object stack
	private Stack<Object> stack = new Stack<Object>();

	public YamlResource(URI uri) {
		super(uri);
	}

	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {

		try {
			doLoadImpl(inputStream, options);
		} catch (IOException ioException) {
			ioException.printStackTrace();
			throw ioException;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	public void doLoadImpl(InputStream inputStream, Map<?, ?> options) throws Exception {

		// reset a few things
		getContents().clear();
		stack.clear();

		// process the yaml
		Object document = new Yaml().load(inputStream);

		System.out.println("Document is: " + document);

		recursiveProcessor(document);

		while (!processQueue.isEmpty()) {
			process(processQueue.poll());
		}

	}

	protected void process(Entry<String, Object> element) throws Exception {

		String key = element.getKey();
		Object value = element.getValue(); // may be null if this entry is an attribute

		System.out.println("processing with key: " + key + " and value: " + value);

		EPackage ePackage = (EPackage) getResourceSet().getPackageRegistry().values().iterator().next();

		if (stack.isEmpty()) { // first entry into loop

			System.out.println("first element");

			processFirstTag(element, ePackage);

		} else if (stack.peek() instanceof EObject) {

			EObject parent = (EObject) stack.peek();

			if (value == null) { // no attrs, only text

				System.out.println("this element is an attribute");

			} else if (!(value instanceof List)) { // else if no attrs

				System.out.println("this element is maybe a containment slot");

				try {
					EReference eReference = (EReference) parent.eClass().getEStructuralFeature(key);
					stack.push(new EReferenceSlot(parent, eReference));

				} catch (ClassCastException cce) {
					EAttribute eAttribute = (EAttribute) parent.eClass().getEStructuralFeature(key);
					System.out.println("got a eattr: " + key + "eattr is: " + eAttribute);

					setAttributeValue(parent, element);
				}

			} else { // got attributes

				System.out.println("this element is a model element (parent model)");

				EClass valueEClass = (EClass) ePackage.getEClassifier(key);

				EObject valueEObject = ePackage.getEFactoryInstance().create(valueEClass);

				EReference eReference = (EReference) parent.eClass().getEStructuralFeature(key);

				setAttributeValue(valueEObject, element);
				if (eReference.isMany()) { // multi-valued

					Collection<Object> existingValues = (Collection<Object>) parent.eGet(eReference);

					existingValues.add(valueEObject);

				} else { // single-valued

					parent.eSet(eReference, valueEObject);
				}

				stack.push(valueEObject);
			}

		} else if (stack.peek() instanceof EReferenceSlot) {

			System.out.println("model element (parent containment)");

			EReferenceSlot slot = (EReferenceSlot) stack.peek();

			EClass valueEClass = (EClass) ePackage.getEClassifier(key);

			EObject valueEObject = ePackage.getEFactoryInstance().create(valueEClass);

			setAttributeValue(valueEObject, element);

			if (slot.getEReference().isMany()) { // multi-valued
				Collection<Object> existingValues = (Collection<Object>) slot.getEObject().eGet(slot.getEReference());

				existingValues.add(valueEObject);
			} else { // single-valued
				slot.getEObject().eSet(slot.getEReference(), valueEObject);

			}

			stack.push(valueEObject);
		}

		System.out.println("Stack is: " + stack + "\n");

	}

	protected void processFirstTag(Entry<String, Object> element, EPackage ePackage) throws Exception {

		// currently only support YamlObject as root element, other support later @TODO
		if (element.getValue() == null || !(element.getValue() instanceof Map)) {
			throw new Exception("Root object is null, or unsuported type");

		} else {

			// get name of first object
			EClass eClass = (EClass) ePackage.getEClassifier(element.getKey());

			if (eClass != null) { // if eClass actually exists

				EObject eObject = ePackage.getEFactoryInstance().create(eClass); // instantiate EClass
				getContents().add(eObject); // add to model

				stack.push(eObject); // add to stack

			} else
				throw new Exception("No eClass named " + element.getKey());
		}
	}

	protected void setAttributeValue(EObject eObject, Entry<String, Object> element) {

		String key = element.getKey();
		Object value = element.getValue();
		EClass eClass = eObject.eClass();

		List<EStructuralFeature> eStructuralFeatures = new ArrayList<>();

		for (EStructuralFeature sf : eClass.getEAllStructuralFeatures()) {
			if (sf.isChangeable() && (sf instanceof EAttribute
					|| ((sf instanceof EReference) && !((EReference) sf).isContainment()))) {
				eStructuralFeatures.add(sf);

				if (sf.getName().equals(key)) {
					eObject.eSet(sf, value);
				}

			}
		}
	}

	protected Queue<Entry<String, Object>> processQueue = new LinkedList<Entry<String, Object>>();

	protected void recursiveProcessor(Object element) throws Exception {

		if (element instanceof Map) {
			for (Map.Entry<String, Object> entry : ((Map<String, Object>) element).entrySet()) {
				processQueue.add(entry);
				recursiveProcessor(entry.getValue());
			}

		} else if (element instanceof List) {
			ListIterator<Object> listIterator = ((ArrayList<Object>) element).listIterator();
			while (listIterator.hasNext()) {
				recursiveProcessor(listIterator.next());
			}

		} else {
			System.out.println("doing nothigns");
			// do nothing, currently
//			process(new AbstractMap.SimpleEntry<String, Object>(element.toString(), null));
		}
	}
}
