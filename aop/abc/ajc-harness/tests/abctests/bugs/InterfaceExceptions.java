public class InterfaceExceptions implements I3 {
    public Object clone() {
        try {
           return super.clone();
        } catch(CloneNotSupportedException e) {
           throw new RuntimeException(e);
        }
    }

    public static Object bar() {
        I3 i3 = new InterfaceExceptions();
        return i3.clone();
    }

}

interface I1 {
    Object clone();
}
interface I2 {
    Object clone() throws CloneNotSupportedException;
}

interface I3 extends I1, I2 {
}
