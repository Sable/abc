
public class Bit {
    private int number;
    private static int count = 0;
    private boolean state;
    
    public Bit() {
        ++count;
        number = count;
        state = false;
    }
    
    public void set() {
        state = true;
    }
    
    public void clear() {
        state = false;
    }
    
    public boolean get() {
        return state;
    }
    
    public String toString() {
        return "bit" + number + "(" + get() + ")";
    }
}