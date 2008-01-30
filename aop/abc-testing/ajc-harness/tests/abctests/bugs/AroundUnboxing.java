import org.aspectj.testing.Tester;

// Just want this class to load without verification errors
public class AroundUnboxing {
	
    public static void first(float amount){
	first(new Float(0.00001).floatValue());
    }

   public static void main(String[] args) { }
}

aspect Foo { 
    Object around() : call(* first(..)) {
	return proceed(); 
    }		
}

