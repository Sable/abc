//Listing 8.21 DesiredCharacteristicsAspect.java: the first version

public aspect DesiredCharacteristicsAspect {
    Object around() : call(* MyClass1.desiredCharacteristicMethod1())
	|| call(* MyClass1.desiredCharacteristicMethod2())
	|| call(* MyClass2.desiredCharacteristicMethod1())
	|| call(* MyClass2.desiredCharacteristicMethod2())
	/* || ... */ {
	// advice code

	return new Object();
    }
}
