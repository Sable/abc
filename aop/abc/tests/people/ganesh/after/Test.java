public class Test {

    public class Exception1 extends Exception {
    }

    public class Exception2 extends Exception {
    }


    public class Exception3 extends Exception {
    }

    public float foo(int x) throws Exception1,Exception2 {
	if(x==1) throw new Exception1();
	if(x==2) throw new Exception2();
	return x>3 ? 0.0F : 1.0F;
    }

    public void foo() {
    }

    public static void main(String[] args) {
	try { new Test().foo(5);
	} catch(Exception1 e) {
	} catch(Exception2 e) {
	}
    }


}
