import java.lang.*;

class OrdinaryJavaClass {
  public int x;
  public int y;

  String foo(int x) 
     { return ("The value of x +1 is " + (x + 1));
     }
}

/* an aspect with a per declaration in the header */
privileged aspect OrdinaryAspect 
  percflow ( call(void Foo.m()) ) 
  { 
    /* declare declaration */
    declare warning: call(*1.*+.new(..)): "calling a constr. ending with 1";

    /* pointcut declarations */
    pointcut notKeywords(): call(void *if*..*while*(int,boolean,*for*));

    pointcut hasSpecialIf(): if(Tracing.isEnabled());

    /* advice declaration */
    after(Point p) returning(int x): target(p) && call(int getX()) 
      {
        System.out.println("Returning int value " + x + " for p = " + p);
      }

    /* inter-type member declaration */
    int OrdinaryJavaClass.incr2(int i)
       { return(x+2);
       } 

    /* ordinary Java declarations */
    int x;

    static int incr3(int x)
      { return(x+3);
      }

    /* a nested class */
    class 
      NestedClass {
        public int after;

	public int getBefore()
	{ return(OtherClass.before);
	}
     } // end of NestedClass 
  } // end of OrdinaryAspect
