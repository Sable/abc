public class A {
    public int prevX; //just to set external get/set
    
    public int fib(int x) {
        int test = this.prevX; //test for internal get
        this.prevX = x; //test for internal set
        
        System.out.println("fib(" + x + ")");
        if (x <= 2) {
            return 1;
        }
        return fib(x-1) + fib(x-2);
    }
}