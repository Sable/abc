package Access.test25;

/*
 *  Test 25: 
 *  access a field of a class from inside a static initialization block 
 *  of the same class
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    char a;
    { a = 'b'; }
}

