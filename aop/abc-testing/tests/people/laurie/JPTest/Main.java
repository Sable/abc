public class Main {

  public static Main v = new Main();

  void lottery ( String arg )
    { System.out.println("The arg in lottery is " + arg);
    }

  public static void main (String args[])
    { v.lottery("Oege");
      v.lottery("Ganesh");
    }

}
