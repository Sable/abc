package test;

public aspect Test1 {

	syn lazy int Test1.i() = 5;
	
  public static void main(String[] args) {
    System.out.println("Hello aspect");
  }
}
