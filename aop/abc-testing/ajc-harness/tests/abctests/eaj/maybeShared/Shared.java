import org.aspectj.testing.Tester;

public class Shared {

	static int i;
	
	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread() {
			public void run() {
				int x = 0;
				i = x;
			}
		};
		t1.start();
		Thread t2 = new Thread() {
			public void run() {
				if(i>5) {
					System.out.println();
				}
			}
		};		
		t2.start();
		t1.join();
		t2.join();
    	Tester.check(Logger.counter==1,"expected 1 matches but saw "+Logger.counter);
	}
	
}

aspect Logger {
	
	static volatile int counter = 0;
	
	after(): set(* *) && !within(Logger) && maybeShared() {
		counter++;
	}
	
}
