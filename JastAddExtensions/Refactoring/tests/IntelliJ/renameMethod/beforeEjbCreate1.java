import javax.ejb.*;

interface EntityLocalHome extends javax.ejb.EJBLocalHome {
  EntityLocal findByPrimaryKey(Integer i) throws javax.ejb.FinderException;
  EntityLocal <caret>create() throws CreateException;    
}

public abstract class EntityEJB implements javax.ejb.EntityBean {
  public EntityEJB() {}

  public abstract EntityLocal2 getCmrField();
  public abstract void setCmrField(EntityLocal2 e);
  public abstract String getCmpField();
  public abstract void setCmpField(String s);

  public Integer ejbCreate() throws CreateException {
      return null;
  }    
}
