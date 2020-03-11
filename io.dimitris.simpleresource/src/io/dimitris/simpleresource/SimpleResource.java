package io.dimitris.simpleresource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class SimpleResource extends ResourceImpl {
	
	public static void main(String[] args) throws Exception {
		
		// Load metamodel
		ResourceSet metamodelResourceSet = new ResourceSetImpl();
		metamodelResourceSet.getResourceFactoryRegistry().
			getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource metamodelResource = metamodelResourceSet.getResource(
			URI.createURI(new File("simpledsl.ecore").toURI().toString()), true);
		metamodelResource.load(null);
		EPackage metamodelEPackage = (EPackage) metamodelResource.getContents().get(0);
		
		// Set up the model resource
		ResourceSet modelResourceSet = new ResourceSetImpl();
		modelResourceSet.getPackageRegistry().put(metamodelEPackage.getNsURI(), metamodelEPackage);
		modelResourceSet.getResourceFactoryRegistry().
			getExtensionToFactoryMap().put("*", new SimpleResourceFactory());
		Resource modelResource = modelResourceSet.getResource(
				URI.createURI(new File("simpledsl.txt").toURI().toString()), true);
		modelResource.load(null);
		
		// Print the contents of the resource
		new ResourcePrinter().print(modelResource);
	}
	
	public SimpleResource(URI uri) {
		super(uri);
	}
	
	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = bufferedReader.readLine();
		while (line != null) {
			
			EPackage ePackage = (EPackage) getResourceSet().getPackageRegistry().values().iterator().next();
			EClass eClass = (EClass) ePackage.getEClassifier(line);
			
			EObject modelElement = ePackage.getEFactoryInstance().create(eClass);
			getContents().add(modelElement);
			
			line = bufferedReader.readLine();
		}
		
		
		
	}
	
}
