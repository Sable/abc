public class SJP {

   int x;
   SJP() {
      this(5);
   }

   SJP(int y) {
     x=y;
   }

   int foo() {
     return x;
   }
  
   int bar() {
     try {
       throw new RuntimeException("");
     } catch(RuntimeException e) {
       return foo();
     }
   }

   public static void main(String[] args) {
      new SJP().bar();
   }
}

aspect Asp1 {
   before() : !within(Asp1) && !within(Asp2) {
     System.out.println(thisJoinPointStaticPart);
   }
}

aspect Asp2 {
   static boolean doneit=false;
   before() : within(Asp1) && adviceexecution() && if(!doneit)  {
     doneit=true;
     System.out.println(thisJoinPointStaticPart);
   }
}
