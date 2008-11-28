class A {
    int f(int n) throws Exception {
        int i = 0;
        while (i < n) {
	    // from
            i++;
            if (i == 23) {
                n += 42;
                throw new Exception("" + n);
            }
	    // to
        }
        return n;
    }
}