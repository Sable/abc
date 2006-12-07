package test;

public class FieldsPrivate {
    private int foo = 0;
    public static void main(String[] args) {
	new FieldsPrivate().foo++;
    }
}

aspect FieldsPrivateAspect {
    before() : (get(* *) || set(* *)) && !within(FieldsPrivateAspect) {
	System.out.println("Matched 'get(* *)||set(* *)' at " +
			   thisJoinPoint);
    }
}
