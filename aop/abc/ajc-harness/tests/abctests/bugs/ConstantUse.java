import org.aspectj.testing.Tester;

public class ConstantUse {
    public static void main(String[] args) {
        switch(3) {
        case ConstantInit.x:
            Tester.event("ok");
        }
        Tester.expectEvent("ok");
        Tester.checkAllEvents();
    }
}