package benchmark;

public class Bench13 {
	static {
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
