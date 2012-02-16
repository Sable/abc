/*
 * global jpi type decl, which has a pointcut_expr, is an extended version of jpi type decl.
 */
import java.lang.*;

//generic global jpi
<R> global jpi R JP(R b) : call(* *(..)) && argsinv(b);
<L extends Integer> global jpi R JP(L b) : call(* *(..)) && argsinv(b);
<H> global jpi void J(H a); //error --> parser dies
