protected void recursiveStackPusher(Object element) {
		if (element instanceof Map) {
			for (Map.Entry<String, Object> entry : ((Map<String, Object>) element).entrySet()) {
				stack.push(entry.getKey());
				recursiveStackPusher(entry.getValue());
			}
		} else if (element instanceof List) {
			ListIterator<Object> listIterator = ((ArrayList<Object>) element).listIterator();
			while (listIterator.hasNext()) {
				recursiveStackPusher(listIterator.next());
			}
		} else {
			stack.push(element.toString());
		}
	}
