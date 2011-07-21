/**
 * This test checks arguments and return type between jpi and advice.
 */

import java.lang.*;

jpi Integer JP(char x, Object i);
jpi Integer JP2(char x, Object i);
jpi Integer JP3(char x, Object i);
jpi Integer JP4(char x, Object i);
jpi Integer JP5(char x, Object i);

aspect A{

    void around JP1(){} //error, JP1 doesn't exist.

    void around JP2(char l, Object k){} //wrong, return type must be the same.

    Integer around JP3(char x, Integer i){ return 1;} //wrong, arguments must have the same type. 

    Integer around JP4(char x, Object p){ return 1;} //ok

    Integer around JP5(char x){ return 1;} //wrong, must be the same quantity of args.

}
