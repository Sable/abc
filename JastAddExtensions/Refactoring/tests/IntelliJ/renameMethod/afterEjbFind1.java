import javax.ejb.*;

interface EntityLocalHomeBP extends javax.ejb.EJBLocalHome {
  EntityLocal <caret>findByXXX(Integer i) throws javax.ejb.FinderException;
}

public abstract class EntityEJBBP implements javax.ejb.EntityBean {
  public EntityEJBBP() {}


  public Integer ejbFindByXXX(Integer i) throws FinderException {
      return null;
  }
}
