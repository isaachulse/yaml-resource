package io.dimitris.simpleresource;

import org.mockito.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
class YamlResourceTest {

	@BeforeEach
	void setUp() throws Exception {

	}
//
//	@AfterEach
//	void tearDown() throws Exception {
//		
//	}
//
//	@Test
//	void testDoLoadInputStreamMapOfQQ() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testDoLoadImpl() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testProcess() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testProcessFirstTag() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testSetAttributeValue() {
//		fail("Not yet implemented");
//	}

	@Test
	void testRecursiveProcessorIterateList() throws Exception {

		Map<String, Object> mapElem1 = new HashMap<String, Object>();
		Map<String, Object> mapElem2 = new HashMap<String, Object>();

		List<Object> list1 = new ArrayList<Object>();
		List<Object> list2 = new ArrayList<Object>();
		List<Object> list3 = new ArrayList<Object>();

		mapElem1.put("elem1", "elem1Value");
		mapElem2.put("elem2", "elem2Value");

		list1.add(mapElem1);
		list2.add(mapElem2);
		list3.add(list1);
		list3.add(list2);

		YamlResource yamlResource = new YamlResource(null);
		yamlResource.recursiveProcessor(list3);

		Queue queue = yamlResource.processQueue;

		assertEquals(queue.size(), 2);
		assertEquals("elem1Value", ((Entry<String, Object>) queue.poll()).getValue());
		assertEquals("elem2", ((Entry<String, Object>) queue.poll()).getKey());

	}

	@Test
	void testRecursiveProcessorIterateMap() throws Exception {

		Map<String, Object> mapElem1 = new HashMap<String, Object>();
		Map<String, Object> mapElem2 = new HashMap<String, Object>();
		Map<String, Object> mapElem3 = new HashMap<String, Object>();

		mapElem1.put("elem1", "elem1Value");
		mapElem2.put("elem2", mapElem1);
		mapElem3.put("elem3", mapElem2);

		YamlResource yamlResource = new YamlResource(null);

		yamlResource.recursiveProcessor(mapElem3);

		Queue queue = yamlResource.processQueue;

		assertEquals(3, queue.size());

		queue.remove();
		queue.remove();

		assertEquals(((Entry<String, Object>) queue.poll()).getValue(), "elem1Value");
		assertEquals(0, queue.size());

	}

	@Test
	void testRecursiveProcessorChildElementsIgnored() throws Exception {
		Map<String, Object> mapElem1 = new HashMap<String, Object>();
		Map<String, Object> mapElem2 = new HashMap<String, Object>();
		Map<String, Object> mapElem3 = new HashMap<String, Object>();

		mapElem1.put("elem1", null);
		mapElem2.put("elem2", mapElem1);
		mapElem3.put("elem3", mapElem2);

		YamlResource yamlResource = new YamlResource(null);

		yamlResource.recursiveProcessor(mapElem3);

		Queue queue = yamlResource.processQueue;

		System.out.println(queue);
		assertEquals(3, queue.size());

		queue.remove();
		queue.remove();

		assertEquals("elem1Value", ((Entry<String, Object>) queue.poll()).getValue());
		assertEquals(queue.size(), 0);
	}

}
