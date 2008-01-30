// Listing 3.2 RemoteClient.java

public class RemoteClient {
    public static void main(String[] args) throws Exception {
	int retVal = RemoteService.getReply();
	System.out.println("Reply is " + retVal);
    }
}
