// RenameVariable/test9/in/A.java p A f g
package p;
class A{
	public A f;
	public int k;
	void m(){
		{
			int g;
		}
		f.k=0;
	}
}