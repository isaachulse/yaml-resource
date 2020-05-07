// Initialize implementation of empty ResourceSet
ResourceSet metamodelResourceSet = new ResourceSetImpl();

// Utilizes EMF's package based namespace to modify ResourceSet
metamodelResourceSet.getPackageRegistry()
  .put(EcorePackage.eINSTANCE.getNsURI(),
    EcorePackage.eINSTANCE);

metamodelResourceSet.getResourceFactoryRegistry()
  .getExtensionToFactoryMap()
  .put("ecore", new XMIResourceFactoryImpl());

// Loads specific ecore file
Resource metamodelResource =
  metamodelResourceSet.createResource(
    URI.createFileURI(new File("file_to_load.ecore")
    .getAbsolutePath())
  );

metamodelResource.load(null);
