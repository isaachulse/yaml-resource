package io.dimitris.simpleresource;

import java.io.File;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

public class ModelUtilities {

	protected EList<EObject> loadMetaModel(String fileName) {
		ResourceSet metamodelResourceSet = new ResourceSetImpl();
		metamodelResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		Resource metamodelResource = metamodelResourceSet
				.getResource(URI.createURI(new File(fileName).toURI().toString()), true);
		try {
			metamodelResource.load(null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return metamodelResource.getContents();
	}

	protected Resource getModelResource(EPackage metaModelEPackage, String fileName) {
		ResourceSet modelResourceSet = new ResourceSetImpl();
		modelResourceSet.getPackageRegistry().put(metaModelEPackage.getNsURI(), metaModelEPackage);
		modelResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("yaml", new YamlResourceFactory());
		Resource modelResource = modelResourceSet.getResource(URI.createURI(new File(fileName).toURI().toString()),
				true);
		return modelResource;
	}

	protected void printResource(EList<EObject> eObjects) {
		XMIResource resource = new XMIResourceImpl();
		resource.getContents().addAll(eObjects);

		try {
			resource.save(System.out, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
