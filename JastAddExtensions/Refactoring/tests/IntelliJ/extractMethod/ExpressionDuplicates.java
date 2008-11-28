import java.util.*;

class C {
    {
        Object[] o = null;
        List l = /*[*/new ArrayList(Arrays.asList(o))/*]*/;

        List l1 = new ArrayList(Arrays.asList(new Object[0]));
    }
}