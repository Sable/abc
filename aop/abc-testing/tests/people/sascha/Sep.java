
public class Sep {
    public static void main(String[] args) {
	System.out.println("sep: " +  System.getProperty("path.separator") );
	System.out.println("sep: " +  System.getProperty("file.separator") );
    }
}

aspect Aspect {
    pointcut pc() : !args(String);
}