package com.isaachulse.yamlresource;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.stream.IntStream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;

public class Main {

	public static void main(String[] args) throws Exception {

//		int[] arrData = { 1, 2, 5, 10, 20, 50, 100, 200, 500 };
//
		ArrayList<Long> timeTaken = new ArrayList();
		ArrayList<Long> space = new ArrayList();
//
//		for (int i = 0; i < arrData.length; i++) {
//			largeEmfGenerator(arrData[i]);
//
//			largeYamlGenerator(arrData[i]);
		
			
		
			System.gc();
			long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			long startTime = System.nanoTime();

			String directory = "";

//		String name = "messaging";
//		String name = "test100";
//		String name = "simpledsl";
			
			String name = "test_" + args[0];

			String metaModelName = directory + name + ".ecore";
			String modelName = directory + name + ".yaml";

			ModelUtilities modelUtilities = new ModelUtilities();
			EList<EObject> eObjects = modelUtilities.loadMetaModel(metaModelName);

			EPackage metaModel = (EPackage) eObjects.get(0);

//		System.out.println(modelUtilities.formatResource(eObjects));

			Resource modelResource = modelUtilities.getModelResource(metaModel, modelName);

			modelResource.load(null);

//		System.out.println(modelUtilities.formatResource(modelResource.getContents()));
//
//		System.out.println("stack is:");

			long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			long endTime = System.nanoTime();
//			space.add();
//			timeTaken.add();

//		}
		
		System.out.println();
		System.out.println(args[0]);
		System.out.println(usedMemoryAfter - usedMemoryBefore);
		System.out.println(endTime - startTime);

	}

	public static void largeEmfGenerator(int maxNum) throws FileNotFoundException {

		PrintStream fileStream = new PrintStream("test_" + maxNum + ".emf");
		System.setOut(fileStream);

		System.out.println("@namespace(uri=\"test\", prefix=\"\")");
		System.out.println("package test;");
		System.out.println();

		IntStream.range(0, maxNum).forEachOrdered(n -> {

			System.out.println("class Test" + n + " {");
			System.out.println("\tval Test" + (n + 1) + "[*]" + " test" + (n + 1) + ";");
			System.out.println("}");
			System.out.println();
		});

		System.out.println("class Test" + (maxNum) + " {");
		System.out.println("attr String name;");
		System.out.println("}");
		System.out.println();
	}

	public static void largeYamlGenerator(int maxNum) throws FileNotFoundException {

		PrintStream fileStream = new PrintStream("test_" + maxNum + ".yaml");
		System.setOut(fileStream);

		IntStream.range(0, maxNum).forEachOrdered(n -> {

			String indent = "";
			StringBuilder stringBuilder = new StringBuilder();

			IntStream.range(0, n).forEachOrdered(p -> {
				stringBuilder.append("    ");
			});

			indent = stringBuilder.toString();

			System.out.println(indent + "Test" + n + ":");
			System.out.println(indent + "  " + "test" + (n + 1) + ":");

		});

		String indent = "";
		StringBuilder stringBuilder = new StringBuilder();

		IntStream.range(0, maxNum).forEachOrdered(p -> {
			stringBuilder.append("    ");
		});

		indent = stringBuilder.toString();

		System.out.println(indent + "Test" + maxNum + ":");
		System.out.println(indent + "  " + "name: testing");
	}

}
