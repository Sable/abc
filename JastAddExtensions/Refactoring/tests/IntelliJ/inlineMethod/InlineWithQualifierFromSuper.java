class Base {
    String id;
    public String getID() {
        return id;
    }
}

class Derived extends Base {
    String getName() {
        return getID();
    }
    
    static void usage(Derived element) {
        StringBuffer buffer = new StringBuffer();
	// changed add to append in next line for compilability
        buffer.append(element./*[*/getName()/*]*/);
    }
}