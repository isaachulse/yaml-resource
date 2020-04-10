package io.dimitris.simpleresource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import org.yaml.snakeyaml.Yaml;

public class YamlResource extends ResourceImpl {

	protected static HashMap tempMap = new HashMap<String, String>();

	public static void main(String[] args) throws Exception {

		tempMap.put("User", "users");
		tempMap.put("Mailbox", "mailbox");
		
		// load metamodel
		ResourceSet metamodelResourceSet = new ResourceSetImpl();
		metamodelResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		Resource metamodelResource = metamodelResourceSet
				.getResource(URI.createURI(new File("model/simpledsl.ecore").toURI().toString()), true);
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
				.getResource(URI.createURI(new File("model/simpledsl.yaml").toURI().toString()), true);

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

		recursiveStackPusher(new Yaml().load(inputStream));

	}

	protected void process(Entry<String, Object> element) throws Exception {

		String key = element.getKey();
		Object value = element.getValue(); // may be null if this entry is an attribute

		System.out.println("processing with key: " + key + " and value: " + value);

		EPackage ePackage = (EPackage) getResourceSet().getPackageRegistry().values().iterator().next();

		if (stack.isEmpty()) { // first entry into loop

			processFirstTag(element, ePackage);

		} else if (stack.peek() instanceof EObject) {

			EObject parent = (EObject) stack.peek();

			if (false) { // no attrs, only text

				// else if no attrs

			} else { // got attributes
				
				String tempId = (String) tempMap.get(key);

				EClass valueEClass = (EClass) ePackage.getEClassifier(tempId); 

				System.out.println("now here, eclass is: " + valueEClass);
				EObject valueEObject = ePackage.getEFactoryInstance().create(valueEClass);

				EReference eReference = (EReference) parent.eClass().getEStructuralFeature(tempId);

				setAttributeValues(valueEObject, element);
				if (eReference.isMany()) { // multi-valued

					Collection<Object> existingValues = (Collection<Object>) parent.eGet(eReference);

					existingValues.add(valueEObject);

				} else { // single-valued
					// TODO
				}

				stack.push(valueEObject);
			}

		} else if (stack.peek() instanceof EReferenceSlot) {

			// if single val

			// else (multi val)

		}

		System.out.println("Stack is: " + stack);

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

	protected void setAttributeValues(EObject valueEObject, Entry<String, Object> element) {
		// TODO
	}

	// TESTING GROUND DOWN HERE, PROCEED WITH CAUTION

	protected void recursiveStackPusher(Object element) throws Exception {

		if (element instanceof Map) {
			for (Map.Entry<String, Object> entry : ((Map<String, Object>) element).entrySet()) {
				System.out.println("iterating with key: " + entry.getKey() + " and value: " + entry.getValue());

				process(entry);
				recursiveStackPusher(entry.getValue());
			}
		} else if (element instanceof List) {
			ListIterator<Object> listIterator = ((ArrayList<Object>) element).listIterator();
			while (listIterator.hasNext()) {
				recursiveStackPusher(listIterator.next());
			}
		} else {
			process((Entry<String, Object>) new AbstractMap.SimpleEntry<String, Object>(element.toString(), null));
		}
	}
}
