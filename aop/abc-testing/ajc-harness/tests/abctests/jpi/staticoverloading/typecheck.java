jpi void JP(int a); //error
jpi void JP(float b); //error
jpi int JP(float z); //error, same signature
jpi float JP(int l) throws Exception; //error, same signature


jpi void JP1(int b);
jpi int JP1(float c);
jpi float JP1(boolean d) throws Exception;

