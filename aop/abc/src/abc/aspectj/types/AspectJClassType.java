
package arc.aspectj.types;

import java.util.List;
import polyglot.types.ClassType;

public interface AspectJClassType extends ClassType {

   public List pointcuts();
   public List advices();
   
}
