package test;

public class FieldPatterns {
    int field1, field2;
    String string1, string2;
    public static void main(String[] args) {
	FieldPatterns v = new FieldPatterns();
	System.out.println("Doing: set field1");
	v.field1 = 1;
	System.out.println("Doing: get field1, set field2");
	v.field2 = v.field1 + 1;
	System.out.println("Doing: set string1");
	v.string1 = "";
	System.out.println("Doing: get string1, set string2");
	v.string2 = v.string1;
    }
}

aspect FieldPatternsAspect {
    before() : get(* field1) { System.out.println("get(* field1)"); }
    before() : set(* FieldPatterns.field2) 
	{System.out.println("set(* FieldPatterns.field2)");}

    before() : get(* *1) { System.out.println("get(* *1)");}
    before() : set(String *) { System.out.println("get(String *)");}
}
