public class Test  {

   int y;

   public Test(int x) {
     if(x<3) y=x; else y=x+1;      
   }

   public Test() {
     this(4);
   }
}
