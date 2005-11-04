public class Fact5 {
    public static final void main(String[] args) {
        System.out.println("5! is "+fact(5));
    }
    public static int fact(int n) {
        if(n <= 0) return 1;
        return n*fact(n-1);
    }
}

aspect Depth5 {
    pointcut fact(): execution(int fact(int));

    before(boolean i): fact() && cflowdepth(i, fact()) {
        System.out.println( "before fact; depth is "+i );
    }
    after(Object i): fact() && cflowdepth(i, fact()) {
        System.out.println( "after fact; depth is "+i );
    }
    before(long i): fact() && cflowdepth(i, fact()) {
    }
    before(short i): fact() && cflowdepth(i, fact()) {
    }
    before(double i): fact() && cflowdepth(i, fact()) {
    }
}
