public class ConstructedClass {
    public static ConstructedClass PEC_ONE = new ConstructedClass("param");

    ConstructedClass(int field) {
    }

    /*[*/public ConstructedClass(String keyword) {
	this(keyword.length());
    }/*]*/
}