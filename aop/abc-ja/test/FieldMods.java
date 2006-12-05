package test;

public class FieldMods {
    public int foo;
    public static void main(String[] args) {
	FieldMods v = new FieldMods();
	v.foo = 1;
    }
}

aspect FieldModsAspect {
    before() : set(public * *) { System.out.println("public * *"); }
    before() : set(!private * *) { System.out.println("!private * *"); }
    before() : set(public !public * *) { System.out.println("public !public * *"); }
    before() : set(public !private * *) { System.out.println("public !private * *"); }
}
