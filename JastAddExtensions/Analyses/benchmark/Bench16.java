package benchmark;

public class Bench16 {
	public static void main(String[] args) {
		try {
			System.out.println("TryStmt");
			throw new Exception();
		} catch (Exception e) {
			System.out.println("CatchStmt");
		} finally {
			System.out.println("FinallyStmt");
		}
	}
}
