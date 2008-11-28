class A{
    public static Object test(Object a) {
	// added initializer to make it compile
        boolean value = false;

        if (a == null){
            value = newMethod();
        }
        else{
        }

        return Boolean.valueOf(value);
    }

    private static boolean newMethod() {
        boolean value;
        value = true;
        return value;
    }
}
