package abc.weaving.matching;

import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.*;

public class BodyAdviceApplication extends AdviceApplication {
    
    public BodyAdviceApplication(AdviceDecl advice,
				 ConditionPointcutHandler cph) {
	super(advice,cph);
    }
}
    
				      
