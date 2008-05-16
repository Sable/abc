package test;

public class Example {
  static class JavaType { 
    JavaType(Example e) {
    }
  }

  public static void main(String[] args) {
    System.out.println("Hello");
    Example t = new Example();
    JavaType b = `A(t);
    Example c = `C();
    %match(b) {
      A(c) -> {
        System.out.println(c); 
        System.out.println(`c); 
      }
    }
  }

  %typeterm TomType { implement { JavaType } }
  %typeterm TomType2 { implement { Example } }

  static void A() { }

  %op TomType A(name : TomType2) {
    make(x) { new JavaType(x) }
  }
  %op TomType2 C() {
    make() { new Example() }

  }

}
