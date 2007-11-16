package benchmark;

public class Bench14 {
	public Bench14() {
		boolean b = true;
		while (b) {
			System.out.println("start");
			if (b) {
				break;
			} 
			System.out.println("break");
			b = false;
		}
	}
}
