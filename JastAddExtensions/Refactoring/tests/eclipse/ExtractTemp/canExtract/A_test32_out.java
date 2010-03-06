package p;
class A{
    void f(int i){
        int temp= (i= 1);   /// Eclipse removes the parentheses
        int y= temp + 1;
    }
}