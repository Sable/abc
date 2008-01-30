// declare parents making a reference to an otherwise unmentioned class

public aspect DeclareParentsRef {
    declare parents: A extends java.util.HashSet;
}

class A {}