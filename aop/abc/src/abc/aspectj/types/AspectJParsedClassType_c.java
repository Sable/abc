
package arc.aspectj.types;

import java.util.LinkedList;
import java.util.List;

import polyglot.frontend.Source;
import polyglot.types.*;
import polyglot.types.Package;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.TypedList;

import polyglot.ext.jl.types.ParsedClassType_c;

public class AspectJParsedClassType_c 
       extends ParsedClassType_c 
       implements AspectJParsedClassType {

   protected List advices;
   protected List pointcuts;

   protected AspectJParsedClassType_c() {
	   super();
   }

   public AspectJParsedClassType_c(TypeSystem ts, 
                                   LazyClassInitializer init, 
							       Source fromSource) {
	   super(ts, init, fromSource);
   }
  
  
   protected boolean initialized() {
		   return super.initialized() &&
		          this.advices != null &&
				  this.pointcuts != null;
   }
	   
   public void addPointcut(PointcutInstance pci) {
	  pointcuts().add(pci);
   }
	 
   public void addAdvice(AdviceInstance ai) {
      advices().add(ai);
   }
   
   public List pointcuts() {
		   if (pointcuts == null) {
			   pointcuts = new TypedList(new LinkedList(), PointcutInstance.class, false);
			   // ((AspectJLazyClassInitializer)init).initPointcuts(this);
			   // freeInit();
		   }
		   return pointcuts;
   }
   
   public List advices() {
			  if (advices == null) {
				  advices = new TypedList(new LinkedList(), AdviceInstance.class, false);
				  // ((AspectJLazyClassInitializer)init).initAdvices(this);
				  // freeInit();
			  }
			  return advices;
   }
   
   
	   
 
   	 

}
