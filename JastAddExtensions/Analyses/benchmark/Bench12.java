package benchmark;

public class Bench12 {
	public Bench12() {
		super();
		int x;
	}
	public static void main(String[] args) {
		boolean b = true;
		synchronized (Bench12.class) {
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
}
