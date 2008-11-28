class A{
    public static Object test(Object a) {
	// added initializer to make it compile
        boolean value = false;

        if (a == null){
            /*[*/value = true;/*]*/
        }
        else{
        }

        return Boolean.valueOf(value);
    }
}
