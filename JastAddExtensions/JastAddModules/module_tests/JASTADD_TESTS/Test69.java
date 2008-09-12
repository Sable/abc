package test;

public aspect Test69 {
  ast X;
  ast Y ::= <ID:String>;
  syn nta Y X.myY(String name) {
    return new Y(name);
  }
  public static void main(String[] args) {
    // Create a non-terminal attribute with an argument
    System.out.println(new X().myY("hello").getID());
  }
}
