package p;

class A{
}
class B extends p.///
                Inner{
	B(){
		super(new A());
	}
}