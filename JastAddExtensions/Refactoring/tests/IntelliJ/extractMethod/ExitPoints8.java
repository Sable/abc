import java.util.ArrayList;

class C {
    public Object m() {
        /*[*/for (Object o : new ArrayList<Object>()) {
            if (o != null) {
                return o;
            }
        }/*]*/
        return null;
    }
}