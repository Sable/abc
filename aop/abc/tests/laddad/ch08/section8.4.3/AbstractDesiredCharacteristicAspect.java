//Listing 8.25 AbstractDesiredCharacteristicAspect.java: the base

abstract aspect AbstractDesiredCharacteristicAspect {
    public abstract pointcut desiredCharacteristicJoinPoints();
    // Example uses around(), but before() and after() work as well
    Object around() : desiredCharacteristicJoinPoints() {
	// advice code
	return new Object();
    }
}


