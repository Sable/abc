//Listing 8.3 Test.java: exercising the simulated expensive operations

public class Test {
    public static void main(String[] args) {
	CachePreFetcher.fetch();
	ProjectSaver.backupSave();
    }
}
