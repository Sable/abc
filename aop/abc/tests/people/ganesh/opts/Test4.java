public class Test4 {

}

class Test4_Test2 extends Test4 {

}

aspect Test4_Aspect perthis(this(Test4_Test2)) {
    before() : this(Test4_Test2) {
    }
    

}
