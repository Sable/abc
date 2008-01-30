public class ReturnTest {
   public int foo (int i) {
      if (i%2 == 0)
        return(0);
      else
        return(g(i));
   }

   public int g(int i)
     { while (true)
         { System.out.print(i);
           if (i==0) 
             return(i);
           i--;
          }
     } 

   public static void main(String args[])
     { ReturnTest t = new ReturnTest();
       t.foo(10);
     }
}

aspect Returning {
  after() returning: execution(* *.*(..)) 
    { System.out.println("returning");
    }
}
