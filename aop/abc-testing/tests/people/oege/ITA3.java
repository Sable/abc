

aspect Aspect {

    public A.new(int a) {
	}
  
}

class A {
    int z = 3;
    public A() { }
}

public class ITA3 {

    public static void main(String[] args) {
	System.out.println(new A(4).z);
	System.out.println(new A().z);
}

}
