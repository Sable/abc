//Listing 2.1 MessageCommunicator.java

public class MessageCommunicator {
    public static void deliver(String message) {
	System.out.println(message);
    }

    public static void deliver(String person, String message) {
	System.out.print(person + ", " + message);
    }
}

