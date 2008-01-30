public aspect SimplePointCuts percflow(adviceexecution()) { 

  /* some regular declarations */
/*
  public int i = 9;
  private Object foo (int i) { return (new Object()); }
  private String alwaysString() { return "STRING"; }
*/


  /* some pointcut declarations */
// following is not allowed in AspectJ,  must be a StringConstant 
// declare warning: adviceexecution() && !adviceexecution() : alwaysString() ; 
//  declare warning: adviceexecution() && !adviceexecution() : "never true"; 
//  declare warning: !(adviceexecution() || !adviceexecution()) : "never true either";
// ajc accepts this, but I don't know why, it should have something after ..
/* declare warning: within(com..) : "whatever"; */
/* the following are correctly rejected by ajc 
  declare warning: within(..) : "whatever";
  declare warning: within(..com) : "whatever";
*/

// I think it should be:
declare warning: within(com..*) : "whatever";

//pointcut get(): args(int);  

//pointcut aspect(): target(even);

// ajc doesn't like args(..,..)  - doesn't allow more than one .., compiler 
// limitation
pointcut foo () : args() || args(*) || args(..);

// can't say args(int || boolean) 
// can't say args(sun..*)
pointcut goo() : args(int,*);

// all of these are ok for ajc, including int - should not include primitive
//    types though?
// pointcut hoo() : target(even) || target(int) || target(*) || target(sun.com);
// we accept only name patterns
pointcut hoo(): target(even) || target(*) || (target(sun.get));

// all of these are not ok for ajc
// pointcut boo() : target(int || bool);
// pointcut boo() : target(sun..com);

/* this if causes a problem for ajc, should it? */
/* declare warning: cflow(adviceexecution() && if (i = 4 * 5)) : "funny"; */

/* some regular Java */
boolean even (int i) { if (i % 2 == 0) return true; else return false; }

/* some more complicated type patterns */
declare warning: within(java..* && sun..* || !*..mcgill..*) : "packages"; 

/* ajc doesn't like the following, don't know why not */
/* declare warning: withincode(!public && (private || abstract) * f() throws *) : "funny"; */

/* if you try parens around modifier pattern, then syntax error on f with ajc */ 
/* should be a constructor */
declare warning: withincode(!public a.b.c.new ()) : "funny";

/* should be a method */
declare warning: withincode(!public int sun.com..*.newb ()) : "funny2";

/* more complex */
declare warning: withincode(!public (a.b.c+ || d.e.f).new ()) : "funny3";

/* ajc likes the following, but our grammar will not accept it ... */

/* declare warning: withincode(!public a.b.c || d.e.f.new ()) : "funny3";*/

declare precedence: *.com && *.sun, foo; 

declare precedence: foo ;

declare warning: args(short,a,short) : "shortashort";

declare warning: args() : "emptyargs";

class foogoo {
    int x;
}

static class sfoogoo {
    int y;
}

/* inner aspects must be static */
/*
static aspect foomoo {
  pointcut xx () : target(*);
}
*/


}
