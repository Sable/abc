package benchmark;

public class Bench7 {
	public static void main(String[] args) {
		boolean b = false;
		start: {
		  System.out.println("start");
		  if (b) {
		    break start;
		  } 
		  System.out.println("break");
		  b = true;
       }
	}
}
