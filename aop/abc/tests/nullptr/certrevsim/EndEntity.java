package certrevsim;

import jsim.event.*;

/** This class implements an end entity **/
public class EndEntity extends Entity{
  
  private RevocationInfo[] revocationInfo;
  private DeltaRevocationInfo[] deltaRevocationInfo;
  
  public EndEntity(double createTime, double validityPeriod, int numberOfDp, int deltaDegree){
    super(createTime);
    revocationInfo = new RevocationInfo[numberOfDp];
    deltaRevocationInfo = new DeltaRevocationInfo[numberOfDp];
    for(int i = 0; i<numberOfDp; i++){
      revocationInfo[i] = new RevocationInfo(0, createTime, createTime,
					     deltaDegree, validityPeriod);
    }
    for(int i = 0; i<numberOfDp; i++){
      deltaRevocationInfo[i] = new DeltaRevocationInfo(0, 0, createTime, createTime, 
						       deltaDegree, validityPeriod);
    }
  }

  public int checkRevocationInformation(double currentTime, int dpNumber){
    if(currentTime>revocationInfo[dpNumber].getNextUpdate()){
      return(Constants.EXPIRED_BASE);
    }
    else if((currentTime>revocationInfo[dpNumber].getFirstDeltaUpdate())
	    && (currentTime>deltaRevocationInfo[dpNumber].getNextUpdate())){
      return(Constants.EXPIRED_DELTA);
    }
    else{
      //// TEST OUTPUT
      //System.out.print("["+currentTime+","+
      //		 revocationInfo[dpNumber].getFirstDeltaUpdate()+","+
      //		 deltaRevocationInfo[dpNumber].getNextUpdate()+"]");
      return Constants.NOT_EXPIRED;}
  }
  
  public void update(RevocationInfo revocationInfo, int dpNumber){
    this.revocationInfo[dpNumber] = revocationInfo;
  }

  public void updateDelta(DeltaRevocationInfo deltaRevocationInfo, int dpNumber){
    this.deltaRevocationInfo[dpNumber] = deltaRevocationInfo;  
  }

}


