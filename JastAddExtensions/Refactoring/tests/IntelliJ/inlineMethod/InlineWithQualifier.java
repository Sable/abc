class Element {
    String id;

    /*[*/String getName() {
        return getID();
    }/*]*/
    String getID() {
        return id;
    }

    public String method(Element element) {
        return getName() + element.getName();
    }

    public String staticMethod(Element element) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(element.getName());
        return buffer.toString();
    }
    static Element toXML(Element element){
	// added semicolon for compilability
        X el= new X("El");
        el.setAttribute("attr", element.getName());
        return el;
    }
}

class Usage {
    public String staticMethod(Element element) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(element.getName());
        return buffer.toString();
    }
}

// added following definition for compilability
class X extends Element { 
    X(String s) { } 
    void setAttribute(String s, String v) { } 
}