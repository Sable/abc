/*
 * global jpi type decl, which has a pointcut_expr, is an extended version of jpi type decl.
 */
import java.lang.*;

//global jpi
global jpi Integer JP() : call(* *(..));
global jpi void JP(int a) : call(* *(..)) && argsinv(a);
global jpi Float J(); //error --> parser dies