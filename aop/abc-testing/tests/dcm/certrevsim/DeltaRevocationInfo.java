package certrevsim;

public class DeltaRevocationInfo extends RevocationInfo{

  // double thisUpdate double nextUpdate double id;
  double baseId;

  public DeltaRevocationInfo(double id, double baseId, 
			     double thisUpdate, double nextUpdate,
			     double deltaDegree,double revocationValidityPeriod){
    super(id, thisUpdate, nextUpdate, deltaDegree, revocationValidityPeriod);
    this.baseId = baseId;
  }

  public double getBaseId(){
    return baseId;
  }
}
