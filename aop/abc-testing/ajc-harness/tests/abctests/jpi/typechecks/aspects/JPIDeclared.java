package aspects;

import interfaces.JPI;
import klass.klass;

aspect A{

    before JPI(){};

    before interfaces.JPI(){};
    
    before klass(){}; //error jpi JP2 doesn't exist.
    
    before interfaces.klass(){};
    
    before interfaces.JP2(){}; //error jpi interfaces.JP2 doesn't exist.

}

