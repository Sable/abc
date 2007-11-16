package benchmark;

public class Bench19 {
	public static void main(String[] args) {
		try {
			try {
				System.out.println("TryStmt");
				throw new Exception();
			} catch (Exception e) {
				System.out.println("CatchStmt");
				throw new Exception();
			} finally {
				System.out.println("FinallyStmt");
				throw new Exception();
			}
		} catch (Exception e) {
			System.out.println("CatchStmt2");
		} finally {
			System.out.println("FinallyStmt2");
		}
	}
}
