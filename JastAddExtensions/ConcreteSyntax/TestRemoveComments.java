public class TestRemoveComments {
  public static void main(String[] args) {
    test("");
    test("hello");
    test(" // hello");
    test(" // hello\n");
    test(" // hello\naa");
    test(" // he//llo\naa");
    test(" // /*he/*//llo\naa");
    test(" /* aa");
    test(" /* a*/a");
    test(" /*\na*/a");
    test(" /*\n/*a*/a");
    test(" /*\n//a*/a");
    test(" /*a*");
    test(" abc/");
    test(" abc//");
    test(" /abc");
    test(" /**a*bc*/");

  }
  public static void test(String s) {
    System.out.println("'" + s + "'");
    String t = AST.ASTNode.removeComments(s);
    System.out.println("'" + t + "'");
    if(t.length() != s.length())
      System.out.println("ERROR: String length does not match");
    System.out.println("=============");
  }
}
