public class Test implements A,B {
    public int fwibble(int x)
    {
	return x+1;
    }

    public static void test() {
	new Test().fwibble(3);
	Test2 o=new Test2();
	o.fwibble(5);
    }
    

}


class Test2 extends Test {
       public int fwibble(int x) {
    return x+2;
    }
}

class Test3 extends Test {
    public int fwibble(int x) {
	return x+2;
    }
}

interface A {
    public int fwibble(int x);
}

interface B {
    public int fwibble(int x);
}
