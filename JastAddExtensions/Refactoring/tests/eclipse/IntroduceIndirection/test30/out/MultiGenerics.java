package p;

public class MultiGenerics<E> {

	{
		MultiGenerics<Integer> intVec = new MultiGenerics<Integer>();
		/*///MultiGenerics.*/bar(intVec, 42);
		/*///MultiGenerics.*/bar(intVec, new Integer(42));
		MultiGenerics<String> sVec = new MultiGenerics<String>();
		/*///MultiGenerics.*/bar(sVec, "X");
	}

	/**
	 * @param <E>
	 * @param multiGenerics
	 * @param e
	 */
	public static <E> void bar(MultiGenerics<E> multiGenerics, E e) {
		multiGenerics.addElement(e);
	}

	private void addElement(E e) {
	}

}
