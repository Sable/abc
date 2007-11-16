package benchmark;

public class Bench11 {
	public static void main(String[] args) {
		boolean b = true;
		try {
		while (b) {
			System.out.println("start");
			if (b) {
				throw new Exception();
			} 
			System.out.println("break");
			b = false;
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
