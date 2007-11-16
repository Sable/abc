package benchmark;

public class Bench10 {
	public static void main(String[] args) {
		boolean b = true;
		while (b) {
			System.out.println("start");
			if (b) {
				throw new Error();
			} 
			System.out.println("break");
			b = false;
		}
	}
}
