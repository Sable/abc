public class Foo {
    String getComponent(Integer i) { return null; }
    Integer myI;

    public void usage() {
        if (myI != null)
            /*[*/method(myI)/*]*/;
    }

    void method(Integer i) {
        System.out.println(getComponent(myI) + getComponent(i));
    }

}