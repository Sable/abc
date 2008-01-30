//Listing 8.22 MyClass1.java

public class MyClass1 {
    // MyClass1's implementation
    private void desiredCharacteristicMethod1() {
    }

    private void desiredCharacteristicMethod2() {
    }

    public pointcut desiredCharacteristicJoinPoints() :
	call(* MyClass1.desiredCharacteristicMethod1())
	|| call(* MyClass1.desiredCharacteristicMethod2())
	/* || ... */;
}
