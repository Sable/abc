package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;

public class Weaver {

    public void weave() {
        // Generate intertype methods and fields
        IntertypeGenerator ig = new IntertypeGenerator();
        for( Iterator imdIt = GlobalAspectInfo.v().getIntertypeMethodDecls().iterator(); imdIt.hasNext(); ) {
            final IntertypeMethodDecl imd = (IntertypeMethodDecl) imdIt.next();
            ig.addMethod( imd );
        }
        for( Iterator ifdIt = GlobalAspectInfo.v().getIntertypeFieldDecls().iterator(); ifdIt.hasNext(); ) {
            final IntertypeFieldDecl ifd = (IntertypeFieldDecl) ifdIt.next();
            ig.addField( ifd );
        }

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
