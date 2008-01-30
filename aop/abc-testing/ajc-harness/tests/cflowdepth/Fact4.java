public class Fact4 {
    public static final void main(String[] args) {
        System.out.println("5! is "+fact(5));
    }
    public static int fact(int n) {
        if(n <= 0) return 1;
        return n*fact(n-1);
    }
}

aspect Depth4 {
    pointcut fact(int j): execution(int fact(int)) && args(j);

    before(int i, int j, int k): fact(k) && cflowbelowdepth(i, fact(j)) {
        System.out.println( "before fact; depth is "+i );
    }
    after(int i, int j, int k): fact(k) && cflowbelowdepth(i, fact(j)) {
        System.out.println( "after fact; depth is "+i );
    }
}
