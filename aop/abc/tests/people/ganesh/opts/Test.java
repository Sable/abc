public class Test {

}

class Test_Test2 extends Test {

}
class Test_Test3 extends Test {

}

aspect Test_Aspect {
    before() : this(Test_Test2) {
	System.out.println(thisJoinPoint.getArgs());
    }
    before() : this(Test_Test3) {
	System.out.println(thisJoinPoint.getArgs());
    }
    

}
