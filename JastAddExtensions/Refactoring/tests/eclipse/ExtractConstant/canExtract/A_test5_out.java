//11, 20 -> 11, 26   AllowLoadtime == true
package p;

class R {
	static int rG() {
		return 2;
	}

	static class S extends R {
		int f(){
			int d= CONSTANT;
			return d;
		}
		private static final int CONSTANT= R.rG();
	}
}