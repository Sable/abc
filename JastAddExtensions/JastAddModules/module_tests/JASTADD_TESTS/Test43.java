package test;

public aspect Test43 {
 public ast A;

 syn lazy byte A.byteValue() = 0;
 syn lazy short A.shortValue() = 1;
 syn lazy int A.intValue() = 2;
 syn lazy char A.charValue() = 'a';
 syn lazy long A.longValue() = 3L;
 syn lazy float A.floatValue() = 0.5f;
 syn lazy double A.doubleValue() = 0.6d;
 syn lazy boolean A.booleanValue() = true;
 syn lazy String A.stringValue() = "str";

 
 public static void main(String[] args) {
   System.out.println("Boxing/Unboxing used when caching attribute values");
   A a = new A();
   System.out.println(a.byteValue());
   System.out.println(a.byteValue());
   System.out.println(a.shortValue());
   System.out.println(a.shortValue());
   System.out.println(a.intValue());
   System.out.println(a.intValue());
   System.out.println(a.charValue());
   System.out.println(a.charValue());
   System.out.println(a.longValue());
   System.out.println(a.longValue());
   System.out.println(a.floatValue());
   System.out.println(a.floatValue());
   System.out.println(a.doubleValue());
   System.out.println(a.doubleValue());
   System.out.println(a.booleanValue());
   System.out.println(a.booleanValue());
   System.out.println(a.stringValue());
   System.out.println(a.stringValue());
 }

}
