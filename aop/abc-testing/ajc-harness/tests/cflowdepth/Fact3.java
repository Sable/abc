public class Fact3 {
    public static final void main(String[] args) {
        System.out.println("5! is "+fact(5));
    }
    public static int fact(int n) {
        if(n <= 0) return 1;
        return n*fact(n-1);
    }
}

aspect Depth3 {
    pointcut fact(): execution(int fact(int));

    before(int i): fact() && cflowbelowdepth(i, fact()) {
        System.out.println( "before fact; depth is "+i );
    }
    after(int i): fact() && cflowbelowdepth(i, fact()) {
        System.out.println( "after fact; depth is "+i );
    }
}
