// multiple output values: one for modelling control flow + output value
class K {
    int f(Object o) {
        /*[*/if (o == null) return 0;
        o = new Object();/*]*/
        Object oo = o;

        return 1;
    }
}
