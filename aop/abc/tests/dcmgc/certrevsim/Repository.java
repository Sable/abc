package certrevsim;
import jsim.event.*;

/** This class implements a Revocation Information 
 Repository entity **/
public class Repository extends Entity{
  
  private RevocationInfo revocationInfo;
  private DeltaRevocationInfo deltaRevocationInfo;

  //public Repository(double createTime){
  //  super(createTime);
  //  revocationInfo = new RevocationInfo(0, createTime, createTime);
  //}
  
  public Repository(double createTime, int deltaDegree){
    super(createTime);
    revocationInfo = new RevocationInfo(0, createTime, createTime, deltaDegree, 0);
    
    if(deltaDegree > 0){
      deltaRevocationInfo = new DeltaRevocationInfo(0, 0, createTime, 
						  createTime, deltaDegree, 0);
    }
  }

  public void updateRevocationInformation(double revocationValidityPeriod,
					  int deltaDegree){
    double id = revocationInfo.getId();
    double thisUpdate = revocationInfo.getThisUpdate();
    double nextUpdate = revocationInfo.getNextUpdate();

    id++;
    thisUpdate = nextUpdate;
    nextUpdate = thisUpdate + revocationValidityPeriod;

    revocationInfo = new RevocationInfo(id, thisUpdate, nextUpdate, 
					deltaDegree, revocationValidityPeriod);    
    
    /// TEST OUTPUT
    //System.out.print("[NEW:"+revocationInfo.getThisUpdate()+","+revocationInfo.getNextUpdate()
    //	     +","+revocationInfo.getFirstDeltaUpdate()+"]");
  }

  public void updateDeltaRevocationInformation(double revocationValidityPeriod,
					       int deltaDegree){
     double id = deltaRevocationInfo.getId();
     double baseId = revocationInfo.getId();
     double thisUpdate = deltaRevocationInfo.getThisUpdate();
     double nextUpdate = deltaRevocationInfo.getNextUpdate();
     
     id++;
     thisUpdate = nextUpdate;
     nextUpdate = thisUpdate + (revocationValidityPeriod/deltaDegree);

     deltaRevocationInfo = new DeltaRevocationInfo(id, baseId, 
						   thisUpdate, nextUpdate,
						   deltaDegree, revocationValidityPeriod);
     /// TEST OUTPUT
     //System.out.print("[NEW:"+deltaRevocationInfo.getThisUpdate()+","+
     //	     deltaRevocationInfo.getNextUpdate()
     //+","+deltaRevocationInfo.getFirstDeltaUpdate()+"]");
  
  }

  /** This is a function for obtaining the current revocation info. **/
  public RevocationInfo requestRevocationInfo(){return revocationInfo;}

  public DeltaRevocationInfo requestDeltaRevocationInfo(){
    return deltaRevocationInfo;}
}
