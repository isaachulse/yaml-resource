package io.dimitris.simpleresource;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;

public class Main {

	public static void main(String[] args) throws Exception {

		String directory = "model/";

		String name = "messaging";
//		String name = "simpledsl";

		String metaModelName = directory + name + ".ecore";
		String modelName = directory + name + ".yaml";

		ModelUtilities modelUtilities = new ModelUtilities();
		EList<EObject> eObjects = modelUtilities.loadMetaModel(metaModelName);

		EPackage metaModel = (EPackage) eObjects.get(0);

		modelUtilities.printResource(eObjects);

		Resource modelResource = modelUtilities.getModelResource(metaModel, modelName);

		modelResource.load(null);

		modelUtilities.printResource(modelResource.getContents());

	}

}
