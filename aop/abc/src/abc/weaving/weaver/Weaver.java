package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;

public class Weaver {

    public void weave() {
        // TODO: add intertype declaration stuff

        // Generate methods inside aspects
        AspectCodeGen ag = new AspectCodeGen();
        for( Iterator asIt = GlobalAspectInfo.v().getAspects().iterator(); asIt.hasNext(); ) {
            final Aspect as = (Aspect) asIt.next();
            ag.fillInAspect(as.getInstanceClass().getSootClass());
        }

        // Weave in code to call advice for each pointcut
        PointcutCodeGen pg = new PointcutCodeGen();
        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            pg.weaveInAspects(cl.getSootClass());
        }
    }
}
