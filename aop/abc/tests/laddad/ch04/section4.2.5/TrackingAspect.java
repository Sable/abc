//Listing 4.13 TrackingAspect.java

public aspect TrackingAspect {
    private String TestPrecedence._id = "TrackingAspect:id";

    private void TestPrecedence.printId() {
	System.out.println(
	   "<TrackingAspect:performTracking id=" + _id + "/>");
    }
}

