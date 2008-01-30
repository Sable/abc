public class Hello {
    static int y;
    public static void main(String [] args){
        System.out.println("Hello World");
        y = 3;
        Hello h = new Hello();
        h.go();
    }

    public void go(){
        System.out.println("hi");
    }
    
}
