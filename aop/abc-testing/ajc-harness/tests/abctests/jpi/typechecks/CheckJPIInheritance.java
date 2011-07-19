/**
 * This test checks the return type and argument type in case of
 * inheritance between jpi.
 * Basically, inheritance add arguments to the signature of the jpi.
 */

import java.lang.*;
import java.io.*;

jpi Number JP(Number amount); //ok
jpi Number JP2(char items, int x) extends JP(); //ok
jpi Number JP3(char items) extends J(); //wrong, J doesn't exist.

jpi Number JP4(Number items) extends JP(item); //wrong, item doesn't exist.
jpi Number JP5(Number items) extends JP(items); //ok
jpi Number JP6(int z, Number b) extends JP(b); //ok

jpi Integer JP7(Number amount) extends JP(amount); //wrong, return type
jpi Number JP8(Integer amount) extends JP(amount); //wrong, argument type

jpi Number JPRoot(Integer x) throws Exception; //ok
jpi Number JP9(Number m, String z, Integer l) throws Exception extends JPRoot(l); //ok
jpi Number JP10(Integer x) throws IOException, Exception extends JPRoot(x); //ok
jpi Number JP11(Integer x) throws IOException extends JPRoot(x); //wrong

jpi Number JPRoot2(); //ok
jpi Number JP12(Integer z) extends JPRoot2(z); //wrong 
jpi Number JP13() throws IOException extends JPRoot2(); //ok

