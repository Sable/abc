public class NewTest {
  int a;
  int b;

  public NewTest(int a, int b)
    { a = a;
      b = b;
    }

  public static void main(String args[])
   { int x = 4;
     int y = 5; 
     NewTest t = new NewTest(x+1,y+2);
   }
}


