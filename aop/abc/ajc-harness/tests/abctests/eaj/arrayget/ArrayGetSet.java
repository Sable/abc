public class ArrayGetSet {
    public static int counter = 0;
    public static int indices = 0;
    public static String tmp = "";
    public static void main(String[] args) {
	String[] foo = new String[2];
	foo[0] = "o1"; foo[1] = "o2";
	foo[0] = foo[1] + foo[1];
	foo[0] = foo[0];
	if(counter != 18 || indices != 7 || !tmp.equals("o1o1o2o2o2o2o2o2o2o2o2o2o2o2o2o2")) {
	    throw new RuntimeException("ArrayGet/Set misbehaved. Sorry for the convoluted test case.");
	}
    }
}

aspect A {
    pointcut array(Object targ, int arg) : target(targ) && args(arg) && !within(A) && !target(java.lang.StringBuffer);

    before(Object o1, int i) : array(o1, i) {
	ArrayGetSet.counter++;
	ArrayGetSet.indices += i;
    }

    after(Object o1, int i) returning (Object o2) : array(o1, i) {
	ArrayGetSet.counter++;
	ArrayGetSet.indices += i;
	ArrayGetSet.tmp += (String)o2;
	}

    after(Object o1, int i) returning ( Object o2) : arrayset() && target(o1) && args(i) && !target(java.lang.StringBuffer) {
	ArrayGetSet.counter++;
	ArrayGetSet.indices += i;
	ArrayGetSet.tmp += (String)o2;
    }
}
