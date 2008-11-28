class TryFinally {
    int method() {
        String s = "abcd";
     
        /*[*/StringBuffer buffer = new StringBuffer();
        try {
            buffer.append(s);
            return buffer.length();
        } finally {
	    // following line changed to make it compile
            buffer.append("");
        }/*]*/
    }
}