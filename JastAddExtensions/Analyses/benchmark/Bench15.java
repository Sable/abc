package benchmark;

public class Bench15 {
	public static void main(String[] args) {
		try {
			System.out.println("TryStmt");
		} catch (Exception e) {
			System.out.println("CatchStmt");
		} finally {
			System.out.println("FinallyStmt");
		}
	}
}
