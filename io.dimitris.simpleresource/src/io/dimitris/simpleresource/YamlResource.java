package io.dimitris.simpleresource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
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
		tempMap.put("Mailbox", "mailbox");

		// load metamodel
		ResourceSet metamodelResourceSet = new ResourceSetImpl();
		metamodelResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		Resource metamodelResource = metamodelResourceSet
				.getResource(URI.createURI(new File("messaging.ecore").toURI().toString()), true);
		metamodelResource.load(null);
		EPackage metamodelEPackage = (EPackage) metamodelResource.getContents().get(0);

//		XMIResource t = new XMIResourceImpl();
//		t.getContents().addAll(metamodelResource.getContents());
//		t.save(System.out, null);

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

		ArrayList nicelyOrderedList = new ArrayList<YamlElement>();
		
//		YamlObject system = (YamlObject) yamlElement;
//		YamlElement user = ((YamlObject) System.get("System"));
//		YamlElement mailBox = ((YamlArray)User.getAsYamlObject().get("User")).get(0);

		
//		nicelyOrderedList.add(system);
//		nicelyOrderedList.add(user);
//		nicelyOrderedList.add(MailBox);
		

		iterateRecursively((Map<String, Object>) document);
		
		System.out.println(stack);
		
		for(Object thing: nicelyOrderedList.toArray()) {
			process((YamlElement) thing);
		}
		
//		traverseYaml(yamlElement);
		
	}

	protected void process(YamlElement element) throws Exception {

	
		
		EPackage ePackage = (EPackage) getResourceSet().getPackageRegistry().values().iterator().next();

		if (stack.isEmpty()) { // first entry into loop

			processFirstTag(element, ePackage);


		} else if (stack.peek() instanceof EObject) {

			EObject parent = (EObject) stack.peek();
			System.out.println(parent);
			System.out.println(element.getIdentifier());

			if (false) { // no attrs, only text

				// else if no attrs

			} else { // got attributes

				String id = element.getIdentifier();

				EClass valueEClass = (EClass) ePackage.getEClassifier(id);

				EObject valueEObject = ePackage.getEFactoryInstance().create(valueEClass);

				String tempId = (String) tempMap.get(id);
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

	protected void setAttributeValues(EObject valueEObject, YamlElement yamlElement) {
		// TODO
	}
	
	// TESTING GROUND DOWN HERE, PROCEED WITH CAUTION
	
	protected void iterateRecursively(Map<String, Object> map) throws ParseException {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (value instanceof Map) {
				stack.push(key);
				iterateRecursively((Map<String, Object>) value);
			} else if (value instanceof ArrayList) {
				stack.push(key);
				iterateArrayList((ArrayList<Object>) value);
			} else if (value instanceof String) {
				stack.push(key);
			} else {
				throw new IllegalArgumentException(String.valueOf(value));
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void iterateArrayList(ArrayList<Object> arrayList) throws ParseException {
		ListIterator<Object> listIterator = arrayList.listIterator();

		while (listIterator.hasNext()) {
			Object current = listIterator.next();
			if (current instanceof Map) {
				
				Map<String, Object> value = ((Map<String, Object>) current).
				iterateRecursively((Map<String, Object>) current);
			} else if (current instanceof ArrayList) {
				stack.push(key);
				iterateArrayList((ArrayList<Object>) value);
			} else {
				throw new IllegalArgumentException(String.valueOf(current));
			}
		}
	}
}
