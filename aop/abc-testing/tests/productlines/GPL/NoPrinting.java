package GPL;

/* add methods to call empty printing */

public aspect NoPrinting {
   public static boolean MyLog.dumpgraph = false;

   public static void MyLog.print(String s) 
      { }

   public static void MyLog.println(String s) 
      { }

   public static void MyLog.println() 
      { }
}
