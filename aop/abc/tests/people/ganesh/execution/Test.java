public class Test {

   public int fact(int n) {
     if(n>1) return n*fact(n-1); else return 1;
   }

   public static void main(String[] args) {
     System.out.println(new Test().fact(5));
   }
}

aspect Aspect {
   private static boolean debug = true;
   int y=5;
   after () returning (int x): execution(int fact(int)) {
       System.out.println(y);
   }
}

