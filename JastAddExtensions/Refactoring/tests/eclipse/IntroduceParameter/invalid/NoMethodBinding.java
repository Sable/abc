//selection: 8, 11, 8, 12
package invalid;

class NoMethodBinding {
	void method() { }
	
	void method() {
		int x = 3; //<-- introduce 3 as a parameter
	}
}