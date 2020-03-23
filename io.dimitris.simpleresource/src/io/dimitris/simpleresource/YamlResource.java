package io.dimitris.simpleresource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.yaml.snakeyaml.Yaml;

import io.dimitris.simpleresource.utils.ResourcePrinter;
import io.dimitris.simpleresource.utils.Utilities;
import io.dimitris.simpleresource.YamlModelDiscoverer;
import io.dimitris.simpleresource.model.YamlElement;
import io.dimitris.simpleresource.model.YamlObject;

public class YamlResource extends ResourceImpl {

	protected YamlElement currentElement = null;

	public static void main(String[] args) throws Exception {
			
		// load metamodel
		ResourceSet metamodelResourceSet = new ResourceSetImpl();
		metamodelResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		Resource metamodelResource = metamodelResourceSet
				.getResource(URI.createURI(new File("simpledsl.ecore").toURI().toString()), true);
		metamodelResource.load(null);
		EPackage metamodelEPackage = (EPackage) metamodelResource.getContents().get(0);

		// print metamodelResource
		System.out.println("metamodelResource:");
		ResourcePrinter.print(metamodelResource);
		System.out.println();

		// set up the model resource
		ResourceSet modelResourceSet = new ResourceSetImpl();
		modelResourceSet.getPackageRegistry().put(metamodelEPackage.getNsURI(), metamodelEPackage);
		modelResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("yaml", new YamlResourceFactory());
		Resource modelResource = modelResourceSet
				.getResource(URI.createURI(new File("simpledsl.yaml").toURI().toString()), true);

		// load modelResource
		modelResource.load(null);

		// print modelResource
		System.out.println("modelResource:");
		ResourcePrinter.print(modelResource);
	}

	// persistent object stack
	private Stack<Object> stack = new Stack<Object>();

	public YamlResource(URI uri) {
		super(uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.emf.ecore.resource.impl.ResourceImpl#doLoad(java.io.InputStream,
	 * java.util.Map)
	 */
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

	/**
	 * Implementation of doLoad() method.
	 * 
	 * @param inputStream
	 * @param options
	 * @throws Exception
	 */
	public void doLoadImpl(InputStream inputStream, Map<?, ?> options) throws Exception {

		// reset a few things
		getContents().clear();
		stack.clear();

		// new yaml parser
		Yaml yaml = new Yaml();
		Object document = yaml.load(inputStream);
		System.out.println("Parsed YAML Map is: " + document);

		// wrap parsed yaml with custom YamlElement, YamlObject, YamlNull, YamlArray and
		// YamlPrimitive classes, also, print a few things out
		YamlObject yamlElement = (YamlObject) YamlModelDiscoverer.wrapYamlObject(document);
		System.out.println("Start YAML Element is:");
		ResourcePrinter.printYaml(yamlElement);
		System.out.println("\n------------\n");

		// process the wrapped yaml
		process(yamlElement);
	}

	/**
	 * 
	 * Processes a YamlElement to modify ePackage with features from document
	 * 
	 * @param yamlElement
	 * @throws Exception
	 */
	protected void process(YamlElement yamlElement) throws Exception {

		// this code only handles containment eReferences, does not yet support
		// EAttributes or non-containment eReferences

		// print a few things out to console
		System.out.println("START ->");
		System.out.println("Start YAML Element is:");
		ResourcePrinter.printYaml(yamlElement);
		System.out.println();

		// get first resource in ePackage (should only be one) @TODO -> make this nicer?
		EPackage ePackage = (EPackage) getResourceSet().getPackageRegistry().values().iterator().next();

		// if this is the first entry into process() loop
		if (stack.isEmpty()) {

			// currently only support YamlObject as root element, other support later @TODO
			if (yamlElement == null || !(yamlElement instanceof YamlObject)) {
				throw new NonConformingException("Root object is null, or unsuported type");

			} else {

				YamlObject yamlObject = (YamlObject) yamlElement;

				// get name of first object
				String id = yamlObject.getIdentifier();
				EClass eClass = (EClass) ePackage.getEClassifier(id);

				// if eclass actually exists
				if (eClass != null) {

					// instantiate EClass
					EObject eObject = ePackage.getEFactoryInstance().create(eClass);
					getContents().add(eObject);

					// add EObject to stack
					stack.push(eObject);

					// remove top layer (process child element)
					yamlElement = yamlObject.get(id);

				} else
					throw new NonConformingException("No eClass named " + id);
			}

		} else if (stack.peek() instanceof EObject) {

			// this element refers to one of the eReferences of its eClass

			// currently only supports YamlObject @TODO
			if (!(yamlElement instanceof YamlObject)) {
				throw new NonConformingException("yamlElement is not a YamlObject");

			} else {

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
					}

				} else {
					throw new NonConformingException("eObject has no eReferences");
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

			// get name of eReference
			String key = slot.eReference.getName();

			// gey yamlObject for eReference name
			YamlObject eReferenceMatch = (YamlObject) yamlObject.get(key);

			// find the eClass that matches the name of our current element
			EClass valueEClass = (EClass) ePackage.getEClassifier(eReferenceMatch.getSingleName());

			// create an instance of it (our new value for the slot)
			EObject valueEObject = ePackage.getEFactoryInstance().create(valueEClass);

			if (eReference.isMany()) {

				// if eReference is multi-valued, get hold of existing values and add our new
				// value to them
				Collection<Object> existingValues = (Collection<Object>) eObject.eGet(eReference);
				existingValues.add(valueEObject);

			} else {

				// if eReference is single-valued, assign eReference to our new value
				eObject.eSet(eReference, valueEObject);

			}

			// push the new object to the stack
			stack.push(valueEObject);

		} else if (stack.peek() instanceof EAttributeSlot) {

			// need to handle EAttributes here
		}

		// print a few things
		System.out.println("END -> Stack is " + Utilities.formatStack(stack));
		System.out.println();

//		// process child elements
//		if (yamlElement instanceof YamlObject && ((YamlObject) yamlElement).hasChildren()) {
//			Map<String, YamlElement> children = ((YamlObject) yamlElement).getChildren();
//
//			for (Entry<String, YamlElement> child : children.entrySet()) {
//				process(child.getValue());
//			}
//		}

		// pop the top object of the stack at the end
		if (!stack.isEmpty())
			System.out.println("Popped stack is: " + stack.pop());
	}

}
