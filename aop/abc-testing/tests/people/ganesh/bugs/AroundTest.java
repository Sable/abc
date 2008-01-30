public class AroundTest {
    void v() {}
    byte b() { return 0; }
    char c() { return 0; }
    short s() { return 0; }
    int i() { return 0; }
    long l() { return 0; }
    float f() { return 0; }
    double d() { return 0; }

    Object o() { return null; }
    String str() { return null; }
    Number n() { return null; }
    Integer ig() { return null; }
    
}

aspect AroundAspect {
    pointcut v(): execution(void v());
    pointcut b(): execution(byte b());
    pointcut c(): execution(char c());
    pointcut s(): execution(short s());
    pointcut i(): execution(int i());
    pointcut l(): execution(long l());
    pointcut f(): execution(float f());
    pointcut d(): execution(double d());

    pointcut o(): execution(Object o());
    pointcut str(): execution(String str());
    pointcut n(): execution(Number n());
    pointcut ig(): execution(Integer ig());

    // void around return type implies void return type of joinpoint
    void around(): v() { proceed(); }

    // v() || b() || c() || s() || i() || l() || f() || d() || o() || str() || n() || ig()

    char around(): c() 
      { return proceed(); }

}
