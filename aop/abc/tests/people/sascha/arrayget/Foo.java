
public class Foo {

    public static void main(String[] args) {
	String[] sa=new String[10];
	sa[5]="five";
	int i=5;
	String s=sa[i];
	System.out.println(s);
    }
}

aspect Bar {
    before(String[] array, int index) : arrayget() && target(array) && args(index) && within(Foo){
	System.out.println(
			   "arrayget: " + array + 
			   "\nindex: " + index +
			   "\nvalue: " + array[index] + 
			   "\njoinpoint: " );//+ thisJoinPoint);
    }
}