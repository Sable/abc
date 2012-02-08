import java.lang.*;

jpi int JP();
<R> jpi R JP(int b);
<R> jpi int JP(R b);
<R extends Integer,Z> jpi Z JP(R b);
<R extends Integer,Z extends Float> jpi R JP(Z b);


<R,Z> jpi H JP1(Z b); //error, return type.
<R,Z> jpi I JP1(J b); //error, return & argument types.
<R,Z> jpi M JP1(Z b); //ok, since M is a classDecl

class M{}

<R> jpi int JP2();
<R> jpi float JP2(int a);
<R> jpi R JP2(R a); //error
<L> jpi L JP2(L b); //error
<I extends Integer> jpi I JP2(I m);
<I> jpi I JP2(I o); //error