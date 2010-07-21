package p;

public class Position {
	private int x;
	private int y;
	private int z;
	//constructors added
	  public Position(int x, int y, int z) {
		    super();
		    this.setX(x);
		    this.setY(y);
		    this.setZ(z);
		  }
		  public Position() {
		    super();
		  }
	public int getX() {
		return x;
	}
	public int /*///void*/ setX(int x) {
		return ///
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public int /*///void*/ setY(int y) {
		return ///
		this.y = y;
	}
	public int getZ() {
		return z;
	}
	public int /*///void*/ setZ(int z) {
		return ///
		this.z = z;
	}
}