package benchmark;

public class Bench8 {
	public static void main(String[] args) {
		boolean b = true;
		while (b) {
			System.out.println("start");
			if (b) {
				continue;
			} 
			System.out.println("break");
			b = false;
		}
	}
}
