public class Test {


}

class Base {

}
 
abstract aspect AbstractAspect extends Base perthis(this(Test)) {

}

aspect Aspect1 extends AbstractAspect  {

}

aspect Aspect2 extends AbstractAspect issingleton() {

}

aspect Aspect3 extends AbstractAspect pertarget(this(Test)) {

}