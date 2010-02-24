package p;

class A{
}
class I2 extends p.///
		 Inner{
	I2(){
		super(new A());
	}
	I2(int i){
		super(new A(), i);
	}
}