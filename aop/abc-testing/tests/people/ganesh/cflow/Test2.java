public class Test2 {

    static int x=3;

    public static void main(String[] args) {
	x=4;
    }
}

aspect Test2Aspect {

    pointcut setx() : set(int Test2.x);

    before() : setx() {
    }

    before() : cflow(setx()) && adviceexecution() {
    }

}
