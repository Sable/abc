interface EntityLocalHome extends javax.ejb.EJBLocalHome {
  EntityLocal findByPrimaryKey(Integer i) throws javax.ejb.FinderException;
  void <caret>newBusiness();
}

public abstract class EntityEJB implements javax.ejb.EntityBean {
  public EntityEJB() {}

  public abstract EntityLocal2 getCmrField();
  public abstract void setCmrField(EntityLocal2 e);
  public abstract String getCmpField();
  public abstract void setCmpField(String s);

  public void ejbHomeNewBusiness() {
  }
}
