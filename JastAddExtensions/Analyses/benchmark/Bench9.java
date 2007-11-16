package benchmark;

public class Bench9 {
	public static void main(String[] args) {
		boolean b = true;
		while (b) {
			System.out.println("start");
			if (b) {
				return;
			} 
			System.out.println("break");
			b = false;
		}
	}
}
