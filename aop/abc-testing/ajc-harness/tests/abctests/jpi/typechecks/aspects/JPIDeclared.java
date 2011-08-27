package aspects;

import interfaces.JPI;
import klass.klass;

aspect A{

    before JPI(){};

    before interfaces.JPI(){}; //error JPI is already declared
    
    before klass(){}; //error, klass is a class.
    
    before interfaces.klass(){};
    
    before interfaces.JP2(){}; //error jpi interfaces.JP2 doesn't exist.

}

