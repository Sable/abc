//Listing 8.24 DesiredCharacteristicsAspect.java: the second version

public aspect DesiredCharacteristicsAspect {
    Object around() : MyClass1.desiredCharacteristicJoinPoints()
	|| MyClass2.desiredCharacteristicJoinPoints() {
	// advice code

	return new Object();
    }
}

