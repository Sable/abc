public class Test2 {
  public static void main(String args[])
    { System.out.println("Hi");
    }
}

aspect Aspect2 {
  // shouldn't one have to say,  java..* instead of java.. in the following??
  // Note that the first advice (incorrect one) passes the front-end, but does
  // not match,  whereas the second one does match.   I believe the front-end
  // should reject the first one.
  before () : call( * java..(..)) && within(Test2) 
    { System.out.println(
	"Incorrect One: Before a call of method in a java package ");
    }

  before () : call( * java..*(..)) && within(Test2) 
    { System.out.println(
	"Correct One: Before a call of method in a java package ");
    }
}
