//Listing 8.2 ProjectSaver.java

public class ProjectSaver {
    static void backupSave() {
	System.out.println("Saving backup copy in thread "
			   + Thread.currentThread());
    }
}
