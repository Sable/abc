public class NewTest {
  int a = 2;
  int b = 3;
  int c = 4;
  static int forceclinit = 13;

  public NewTest(int a, int b)
    { // TODO: bug in Java2Jimple, loses this try catch block 
       try 
        { this.a = a;
          this.b = b;
	}
      catch (Exception e)
        { this.a = 1;
	}
    }
}


