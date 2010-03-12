package p5;
class MoreInner {

	/** Comment */
	private final A.Inner inner;

	{
		this.inner.someField++;
	}

	/**
	 * @param inner
	 */
	MoreInner(A.Inner inner) {
		this.inner= inner;
	}
}