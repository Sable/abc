// ExtractMethod/test14/in/A.java A m 3 3 n

class A {
  class MyExn extends Throwable { }
  void m(int k) throws Throwable {
    class MyExn extends Throwable { }
    int i = k+1;
    i = 2;
    for(int j=0;j<i;++j) {
      if(j==4)
	throw new MyExn();
      ++i;
    }
    int j = ++i;
  }
}