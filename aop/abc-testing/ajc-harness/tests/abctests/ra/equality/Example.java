import org.aspectj.testing.Tester;

public class Example {
    static Bit b1 = new Bit();
    static Bit b2 = new Bit();
    static Bit b3 = new Bit();
    
    public static void main(String[] args) {

        Equality.associate(b1,b2);
        Equality.associate(b2,b3);
        
        b1.set();
        Tester.check(b1.get() && b2.get() && b3.get(),"all three bits must be set");
            
        b2.clear();
        Tester.check(!b1.get() && !b2.get() && !b3.get(),"all three bits must not be set");
        
        b3.set();
        Tester.check(b1.get() && b2.get() && b3.get(),"all three bits must be set");
    }
    
}