package io.dimitris.simpleresource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.yaml.snakeyaml.Yaml;

import io.dimitris.simpleresource.utils.ResourcePrinter;
import io.dimitris.simpleresource.utils.Utilities;
import io.dimitris.simpleresource.YamlModelDiscoverer;
import io.dimitris.simpleresource.model.YamlArray;
import io.dimitris.simpleresource.model.YamlElement;
import io.dimitris.simpleresource.model.YamlObject;
import io.dimitris.simpleresource.model.YamlPrimitive;

public class YamlResource extends ResourceImpl {

	protected static HashMap tempMap = new HashMap<String, String>(); 
	
	protected YamlElement currentElement = null;

	public static void main(String[] args) throws Exception {
		
		tempMap.put("User", "users");

		// load metamodel
		ResourceSet metamodelResourceSet = new ResourceSetImpl();
		metamodelResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		Resource metamodelResource = metamodelResourceSet
				.getResource(URI.createURI(new File("messaging.ecore").toURI().toString()), true);
		metamodelResource.load(null);
		EPackage metamodelEPackage = (EPackage) metamodelResource.getContents().get(0);

		XMIResource t = new XMIResourceImpl();
		t.getContents().addAll(metamodelResource.getContents());
		t.save(System.out, null);
		
		// set up the model resource
		ResourceSet modelResourceSet = new ResourceSetImpl();
		modelResourceSet.getPackageRegistry().put(metamodelEPackage.getNsURI(), metamodelEPackage);
		modelResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("yaml", new YamlResourceFactory());
		Resource modelResource = modelResourceSet
				.getResource(URI.createURI(new File("messaging.yaml").toURI().toString()), true);

		// load modelResource
		modelResource.load(null);

		System.out.println();

		XMIResource r = new XMIResourceImpl();
		r.getContents().addAll(modelResource.getContents());
		r.save(System.out, null);
	}

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

		Yaml yaml = new Yaml();
		Object document = yaml.load(inputStream);

		YamlElement yamlElement = YamlModelDiscoverer.wrapYamlObject(document);

		process(yamlElement);
	}

	protected void process(YamlElement yamlElement) throws Exception {

		EPackage ePackage = (EPackage) getResourceSet().getPackageRegistry().values().iterator().next();

		if (stack.isEmpty()) { // first entry into loop

			processFirstTag(yamlElement, ePackage);
			
			YamlElement child = ((YamlObject) yamlElement).getChild().getValue();
			process(child);

		} else if (stack.peek() instanceof EObject) {

			EObject parent = (EObject) stack.peek();
			System.out.println(parent);
			System.out.println(yamlElement.getIdentifier());

			if (false) { // no attrs, only text

			// else if no attrs

			} else { // got attributes
				
				String id = yamlElement.getIdentifier();
				
				EClass valueEClass = (EClass) ePackage.getEClassifier(id);
				
				EObject valueEObject = ePackage.getEFactoryInstance().create(valueEClass);
				
				String tempId = (String) tempMap.get(id);
				EReference eReference = (EReference) parent.eClass().getEStructuralFeature(tempId);

				
				if (eReference.isMany()) { // multi-valued
				
					Collection<Object> existingValues = (Collection<Object>) parent.eGet(eReference);

					existingValues.add(valueEObject);
					
					
				} else { // single-valued
					
				}
			}
			

		} else if (stack.peek() instanceof EReferenceSlot) {

			// if single val

			// else (multi val)

		}

		else if (stack.peek() == null && !yamlElement.hasAttributes()) { // if element has no attributes and only text
																			// (orphan

			// find parent's EClass attribute

			if (yamlElement instanceof YamlObject) {

				// get hold of the eObject thats at the top of the stack
				EObject eObject = (EObject) stack.peek();

				// get the name of all eReferences linking to this source
				if (((YamlObject) yamlElement).hasChildren()) {
					Map<String, YamlElement> children = ((YamlObject) yamlElement).getChildren();

					// for each child eReference in children
					for (Entry<String, YamlElement> child : children.entrySet()) {

						// get name of eReference and value linked to it
						String key = child.getKey();

						// get hold of the eReference of its eClass with that name
						EReference eReference = (EReference) eObject.eClass().getEStructuralFeature(key);

						// put a EReferenceSlot containing the eObject and eReference on the stack
						stack.push(new EReferenceSlot(eObject, eReference));

						process(child.getValue());
					}
				}

				else if (yamlElement instanceof YamlArray) {
					System.out.println("array");

				} else if (yamlElement instanceof YamlPrimitive) {

				} else {
					throw new NonConformingException("not object, array or primitive?");
				}

			}

		} else if (stack.peek() instanceof EReferenceSlot) {

			// current element is an eObject that should be added to the values of slot

			// get hold of the slot, and the eReference and eObject it encapsulates
			EReferenceSlot slot = (EReferenceSlot) stack.peek();
			EReference eReference = slot.getEReference();
			EObject eObject = slot.getEObject();

			// check that the eReference is a containment reference @TODO -> support
			// non-containment
			if (!eReference.isContainment() || !(yamlElement instanceof YamlObject))
				throw new RuntimeException("can only handle containment references and YamlObjects at the moment");

			YamlObject yamlObject = (YamlObject) yamlElement;

			// get name of element
			String key = yamlObject.getIdentifier();

			// get yamlObject for eReference name
//			YamlObject eReferenceMatch = (YamlObject) yamlObject.get(key);

			if (eReference.isMany()) {
				YamlArray yamlArray = (YamlArray) yamlObject.get(key);

				for (YamlElement yamlThing : yamlArray) {
					EClass valueEClass = (EClass) ePackage.getEClassifier(key);

					// create an instance of it (our new value for the slot)
					EObject valueEObject = ePackage.getEFactoryInstance().create(valueEClass);

				}

				// if eReference is multi-valued, get hold of existing values and add our new
				// value to them
				Collection<Object> existingValues = (Collection<Object>) eObject.eGet(eReference);
//				existingValues.add(valueEObject);
//				existingValues.addAll(c);

			} else {

				// find the eClass that matches the name of our current element
				EClass valueEClass = (EClass) ePackage.getEClassifier(key);

				// create an instance of it (our new value for the slot)
				EObject valueEObject = ePackage.getEFactoryInstance().create(valueEClass);

				// if eReference is single-valued, assign eReference to our new value
				eObject.eSet(eReference, valueEObject);

			}

			// push the new object to the stack
//			stack.push(valueEObject);
//			process(yamlElement);

		} else if (stack.peek() instanceof EAttributeSlot) {

			// need to handle EAttributes here
		}

//		// process child elements
//		if (yamlElement instanceof YamlObject && ((YamlObject) yamlElement).hasChildren()) {
//			Map<String, YamlElement> children = ((YamlObject) yamlElement).getChildren();
//
//			for (Entry<String, YamlElement> child : children.entrySet()) {
//				process(child.getValue());
//			}
//		}

//		process(yamlElement);
		// pop the top object of the stack at the end
		if (!stack.isEmpty())
			System.out.println("Popped stack is: " + stack.pop());
	}

	protected void processFirstTag(YamlElement yamlElement, EPackage ePackage) throws NonConformingException {

		// currently only support YamlObject as root element, other support later @TODO
		if (yamlElement == null || !(yamlElement instanceof YamlObject)) {
			throw new NonConformingException("Root object is null, or unsuported type");

		} else {

			YamlObject yamlObject = (YamlObject) yamlElement;

			// get name of first object
			String id = yamlObject.getIdentifier();
			EClass eClass = (EClass) ePackage.getEClassifier(id);

			if (eClass != null) { // if eClass actually exists

				EObject eObject = ePackage.getEFactoryInstance().create(eClass); // instantiate EClass
				getContents().add(eObject); // add to model

				stack.push(eObject); // add to stack

			} else
				throw new NonConformingException("No eClass named " + id);
		}
	}
}
