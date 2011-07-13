/**
 * This test checks the return type and argument type in case of
 * inheritance between jpi.
 * Basically, inheritance add arguments to the signature of the jpi.
 */

jpi Number JP(Number amount); //ok
jpi Number JP2(Number items, int x) extends JP(items); //ok
jpi Number JP3(int z, Number b) extends JP(b); //ok

jpi Integer JP4(Number amount) extends JP(amount); //wrong, return type
jpi Number JP5(Integer amount) extends JP(amount); //wrong, argument type
