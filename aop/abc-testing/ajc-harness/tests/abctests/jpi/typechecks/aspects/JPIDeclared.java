package aspects;

import interfaces.JPI;

aspect A{

    before JPI(){};

    before interfaces.JPI(){};
    
    before JP2(){}; //error jpi JP2 doesn't exist.
    
    before interfaces.JP2(){}; //error jpi interfaces.JP2 doesn't exist.


}