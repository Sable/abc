package certrevsim;

public class RevocationInfo{

  protected double thisUpdate;
  protected double nextUpdate;
  protected double id;
  protected double firstDeltaUpdate;

  //public RevocationInfo(double id, double thisUpdate, double nextUpdate){
  //  this.id = id;
  //  this.thisUpdate = thisUpdate;
  //  this.nextUpdate = nextUpdate;
  //}

  public RevocationInfo(double id, double thisUpdate, double nextUpdate,
			double deltaDegree,double revocationValidityPeriod){
    // this(id, thisUpdate, nextUpdate);
    this.id = id;
    this.thisUpdate = thisUpdate;
    this.nextUpdate = nextUpdate;
    if(deltaDegree > 0){
      this.firstDeltaUpdate = thisUpdate+(revocationValidityPeriod / deltaDegree);
    }
    else{
      this.firstDeltaUpdate = nextUpdate; // I.e. no delta at all
    }
  }
  
  public double getFirstDeltaUpdate(){
    try{
      return firstDeltaUpdate;
    }catch(Exception e){
      return nextUpdate;
    }
  }

  public double getId(){
    return id;
  }

  public double getThisUpdate(){
    return thisUpdate;
  }

  public double getNextUpdate(){
    return nextUpdate;
  }

}
