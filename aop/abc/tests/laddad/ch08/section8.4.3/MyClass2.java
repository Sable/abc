//Listing 8.27 MyClass2.java: participating in the collaboration

public class MyClass2 {
    // MyClass2's implementation
    private void desiredCharacteristicMethod1() {
    }

    private void desiredCharacteristicMethod2() {
    }

    public static aspect DesiredCharacteristicParticipant
	extends AbstractDesiredCharacteristicAspect {
	public pointcut desiredCharacteristicJoinPoints() :
	    call(* MyClass2.desiredCharacteristicMethod1())
	    || call(* MyClass2.desiredCharacteristicMethod2())
	    /* || ... */;
    }
}
