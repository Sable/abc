//Listing 8.1 CachePreFetcher.java

public class CachePreFetcher {
    static void fetch() {
	System.out.println("Fetching in thread "
			   + Thread.currentThread());
    }
}
