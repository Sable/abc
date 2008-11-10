package test;

public aspect Test82 {

  syn int ASTNode.test() = 1;
  syn int List.test() = 2;
  syn int Opt.test() = 3;

  rewrite List {
    to List this;
  }

  rewrite Opt {
    to Opt this;
  }

  public static void main(String[] args) {
    System.out.println(new ASTNode().test());
    System.out.println(new List().test());
    System.out.println(new Opt().test());
    System.out.println("ASTNode: " + new ASTNode().mayHaveRewrite());
    System.out.println("List: " + new List().mayHaveRewrite());
    System.out.println("Opt: " + new Opt().mayHaveRewrite());
  }

}

