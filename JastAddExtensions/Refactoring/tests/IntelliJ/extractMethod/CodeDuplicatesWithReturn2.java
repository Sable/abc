class C {
    String method(Object o) {
        System.out.println(o);
        /*[*/Integer i = new Integer(o.hashCode());
        return i.toString();/*]*/
    }

    {
        Integer j = new Integer(Boolean.TRUE.hashCode());
        String k = j.toString();
    }
}