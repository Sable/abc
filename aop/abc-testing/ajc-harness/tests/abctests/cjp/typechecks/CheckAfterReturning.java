import java.util.*;

public aspect CheckAfterReturning {
	
	joinpoint String JP();

	joinpoint List JP2();

	after JP() returning(int i) {} //error: return types not compatible
	
	after JP() returning(Object o) {} //ok

	after JP2() returning(Map m) {} //ok: there may be some List cast-convertible to Map
}