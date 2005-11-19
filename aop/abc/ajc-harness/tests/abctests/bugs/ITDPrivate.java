import java.awt.*;

public class ITDPrivate {
    private Rectangle r;

    public static void main(String[] args) {
    }
}

privileged aspect Aspect {
    public void ITDPrivate.writex(int x) {
        r.x = x; //Exception when processing this
    }
}
