/**
 * This test checks the return type and argument type in case of
 * inheritance between jpi.
 * Basically, inheritance add arguments to the signature of the jpi.
 */

jpi int JP(char amount); //ok
jpi int JP2(char items, int x) extends JP(); //ok
jpi int JP3(char items) extends J(); //wrong, J doesn't exist.

jpi int JP4(char items) extends JP(item);
jpi int JP5(char items) extends JP(items);

jpi char JP6(int z, char b) extends JP(b); //ok

//jpi Integer JP7(Number amount) extends JP(amount); //wrong, return type
//jpi Number JP8(Integer amount) extends JP(amount); //wrong, argument type
