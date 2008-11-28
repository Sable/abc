class C {
    String method(Object o) {
        System.out.println(o);
        /*[*/Integer i = new Integer(o.hashCode());
        return i.toString();/*]*/
    }

    {
        String k;
        Integer j = new Integer(Boolean.TRUE.hashCode());
        k = j.toString();
    }
}