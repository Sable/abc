package p;

import java.util.ArrayList;

class A{
	private final class Inner extends ArrayList {
		private Inner(int p0) {
			super(p0);
		}
	}

	void g(){
		new Inner(6);
	}
}