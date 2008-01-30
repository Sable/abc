//Listing 4.5 SaveEnergyAspect.java

public aspect SaveEnergyAspect {
    before() : call(void Home.exit()) {
	System.out.println("Switching off lights");
    }

    after() : call(void Home.enter()) {
	System.out.println("Switching on lights");
    }
}
