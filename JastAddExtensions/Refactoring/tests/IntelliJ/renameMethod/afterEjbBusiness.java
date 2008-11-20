import javax.ejb.*;

abstract interface EntityLocal extends EJBLocalObject {
  void <caret>newBusiness();
}

public abstract class EntityEJB implements EntityBean {
  public EntityEJB() {}

  public abstract EntityLocal2 getCmrField();
  public abstract void setCmrField(EntityLocal2 e);
  public abstract String getCmpField();
  public abstract void setCmpField(String s);

  public void newBusiness() {
  }
}
