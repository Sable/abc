//Listing 8.26 MyClass1.java: participating in the collaboration

public class MyClass1 {
    // MyClass1's implementation
    private void desiredCharacteristicMethod1() {
    }

    private void desiredCharacteristicMethod2() {
    }

    public static aspect DesiredCharacteristicParticipant
	extends AbstractDesiredCharacteristicAspect {
	public pointcut desiredCharacteristicJoinPoints() :
	    call(* MyClass1.desiredCharacteristicMethod1())
	    || call(* MyClass1.desiredCharacteristicMethod2())
	    /* || ... */;
    }
}
