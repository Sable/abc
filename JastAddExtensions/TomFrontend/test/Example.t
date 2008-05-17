package test;

public class Example {

  static abstract class JavaType {
    JavaType2 getName() { throw new UnsupportedOperationException(); }
  }
  
  static abstract class JavaType2 {}

  static class A extends JavaType {
    
    JavaType2 name;

    A(JavaType2 name) {
    this.name = name;
    }

    JavaType2 getName() {
      return name;
    }
  
  }

  static class B extends JavaType {
    B() {}
  }

  static class C extends JavaType2 {
    C() {}
  }

  public static void main(String[] args) {
    System.out.println("Hello");
    JavaType2 c = `C();
    JavaType a = `A(c);
    JavaType x = `B();
    %match(a) {
      A(x) -> {
         JavaType tmp1 = x;
         JavaType2 tmp2 = `x;
      }
    }
  }

  %typeterm TomType { 
    implement { JavaType }
    is_sort(t) { (t instanceof JavaType) }
    equals(t1,t2) { t1.equals(t2) }
  }

  %typeterm TomType2 { 
    implement { JavaType2 } 
    is_sort(t) { (t instanceof JavaType) }
    equals(t1,t2) { t1.equals(t2) }
  }

  %op TomType A(name : TomType2) {
    is_fsym(t) { t instanceof A }
    get_slot(name,t) { t.getName() }
    make(x) { new A(x) }
  }
  // for make: order of parameters, same number of parameters, return type is op type
  // is fsym: one parameter of type Object, return type is boolean
  // get_slot_n: one parameter of type op type, return type is same as lookup for the same name

  %op TomType B() {
    make() { new B() }
    is_fsym(t) { t instanceof B }
  }

  %op TomType2 C() {
    make() { new C() }
    is_fsym(t) { t instanceof C }
  }

}
