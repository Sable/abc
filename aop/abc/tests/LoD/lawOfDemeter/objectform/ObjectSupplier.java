package lawOfDemeter.objectform;
import java.util.*;

/**
 * @authors David H. Lorenz and Pengcheng Wu
 * @version 0.5, 12/18/02
 */

/*** THIS MUST BE LINE 9 ***/
abstract class ObjectSupplier {
  protected boolean containsValue(Object supplier){
    return targets.containsValue(supplier);
  }
  protected void add(Object key,Object value){
    targets.put(key,value);
  }
  protected void addValue(Object supplier) {
    add(supplier,supplier);
  }
  protected void addAll(Object[] suppliers) {
    for(int i=0; i< suppliers.length; i++)
      addValue(suppliers[i]);
  }
  private IdentityHashMap targets =
    new IdentityHashMap();
}
