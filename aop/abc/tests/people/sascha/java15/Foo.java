

class Container<E> {
    
    public void set(E val) { 
	this.val=val;
	// fails: Aspect.pc2(this); 
	Aspect.pc3(this);
    }
    public Container<int[]> makeone() { return null; }
  public void set(Container<? extends E> val) { set(val.get()); }
  
  public E get() { return val; }
    private E val;
}

public class Foo {
 public static void main(String[] args) {
   Container<String> c=new Container<String>();
   c.set("hello"); Aspect.pc1(c); 

   String s=c.get(); Aspect.pc4(c.get(), c); Aspect.pc5(c); Aspect.pc6(c); Aspect.pc7(c);

   Number n="test";
 }
}

/*
aspect Aspect {
*/
class Aspect {
    // pointcut pc1(): call(* Container<String>.set(..)) {}
    static public void pc1(Container<String> c) {  }

    // pointcut pc2(): execution(* Container<String>.set(..)) {}
    static public void pc2(Container<String> c) {  }

    // pointcut pc3(): execution(* Container<?>.set(..)) {}
    static public void pc3(Container<?> c) {  }

    // pointcut pc4(): call(String Container<?>.get(..)) {}
    static public void pc4(String s, Container<?> c) {  }

    // <T extends Container> pointcut pc5(): call(* T.get(..)) {}
    static public <T extends Container> void pc5(T c) {  }

    
    // <E, T extends Container<E>> pointcut pc6(): call(* T.get(..)) {}
    static public <E, T extends Container<E>> void pc6(T c) { }
   
     // <E extends String, T extends Container<E>> pointcut pc7(): call(* T.get(..)) {}
    static public <E extends String, T extends Container<E>> void pc7(T c) { }
   
}

interface Param<T> {}

class Test {
    <T> void foo(Container<? super Test> l) {
	
    }
    //  <T> void boo(Container<T<Test>> c) {} // error: found type parameter, required class
    
    <T> void boo(T[] arg) {}

    <T> void boo(Container<T[]> arg) {}

    <T> void boo(Container<T>[] arg) {}
    
    // <T> void boo(Container<T extends Test> arg) {} illegal

   <T> void boo2(Container<Param<T>[]> arg) {}
   <T> void boo3(Container<Param<T[]>[]> arg) {}
}