
public class Foo {

    public static void main(String[] args) {
	new Foo().foo();
    }
    public void foo() {
	String[] sa=new String[10];
	sa[5]="five";
	sa[6]="six";
	int i=5;
	
	String s=sa[5];
	bar(sa[5]);
	System.out.println();
	System.out.println(s);
    }
    public void bar(String s) {
	System.out.println("bar: " + s);
    }
}

aspect Bar {

    String around(String[] array, int index) : arrayget() && target(array) && args(index) && within(Foo){
	System.out.println(
			   "arrayget: " + array + 
			   "\nindex: " + index +
			   "\nvalue: " + array[index] + 
			   "\njoinpoint: " + thisJoinPoint);
	return proceed(array, ++index);
    }

     before(String[] array, int index, Foo f) : arrayget() && target(array) && args(index) && within(Foo) && this(f){
	System.out.println(
			   "arrayget: " + array + 
			   "\nindex: " + index +
			   "\nvalue: " + array[index] + 
			   "\njoinpoint: ");// + thisJoinPoint);
       }

}