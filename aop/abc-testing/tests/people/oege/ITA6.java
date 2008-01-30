

aspect Aspect {

    int Foo.x = three();

}

interface Foo {
    int three();
}

class A implements Foo 
{
    public int three() {
	return 3;
    }
}

public class ITA6 {
    
    public static void main(String[] args) {
	Foo a = new A();
	System.out.println("a.x="+a.x);
	a.x = 5;
	System.out.println("a.x="+a.x);
    }
}
