package lawOfDemeter.objectform;
import lawOfDemeter.Any;
import java.util.HashMap;
import org.aspectj.lang.*;
/**
 * @authors David H. Lorenz and Pengcheng Wu
 * @version 0.8, 12/19/02
 */
/*** THIS MUST BE LINE 9 ***/
public aspect Pertarget 
  extends ObjectSupplier
  pertarget(Any.Initialization()) {
  before(Object value): Any.Set(value) {
    add(fieldIdentity(thisJoinPointStaticPart),
      value);
  }
  public boolean contains(Object /*[*/target/*]*//**+target+**/) {
    return super.containsValue(/*[*/target/*]*//**+target+**/) ||
      Percflow.aspectOf().containsValue(/*[*/target/*]*//**+target+**/);
  }
  private String fieldIdentity(JoinPoint.StaticPart
    sp) {
    String fieldName = sp.getSignature().
      getDeclaringType().getName() + ":" +
      sp.getSignature().getName();
    if(fieldNames.containsKey(fieldName))
      fieldName=(String)fieldNames.get(fieldName);
    else
      fieldNames.put(fieldName,fieldName);
    return fieldName;
  }
  private static HashMap fieldNames =
    new HashMap();
}
