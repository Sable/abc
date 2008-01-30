package certrevsim;


/** This is a resource for gathering the statistical data from 
a system run. This class holds static data elements **/
public class Statistics{
  private static double revocationRequests;
  private static double[] requestArray;
  
  private static double deltaRevocationRequests;
  private static double[] deltaRequestArray;

  private static double[] nwLoad;

  private static double[] procLoad;

  private static double[] nwQueue;
  private static double[] procQueue;

  public static void initialize(double simulationTimespan){
    revocationRequests = deltaRevocationRequests = 0;
    requestArray = new double[(int) simulationTimespan];
    deltaRequestArray = new double[(int) simulationTimespan];
    nwLoad = new double[(int) simulationTimespan];
    procLoad = new double[(int) simulationTimespan];
    nwQueue = new double[(int) simulationTimespan];
    procQueue = new double[(int) simulationTimespan];

    for(int i = 0; i<simulationTimespan; i++){
      requestArray[i] = 0;
      deltaRequestArray[i] = 0;
      nwLoad[i] = 0;
      procLoad[i] = 0;
      nwQueue[i] = 0;
      procQueue[i]=0;
    }
  }

  public static void incrementRevocationRequests(double simulationTime){
    revocationRequests++;
    try{
      requestArray[(int) (simulationTime-1)]++;
    }catch(Exception e){System.out.println("Out of Bound: "+simulationTime);}
  }

  public static void incrementDeltaRevocationRequests(double simulationTime){
    deltaRevocationRequests++;
    try{
      deltaRequestArray[(int) (simulationTime-1)]++;
    }catch(Exception e){System.out.println("Out of Bound: "+simulationTime);}
  }

  public static void increaseNetworkLoad(double simulationTime, 
					 double revocationSize){
    try{
      nwLoad[(int) (simulationTime -1)] += revocationSize;
    }catch(Exception e){System.out.println("Out of Bound: "+simulationTime);}
  }

  public static void increaseProcessingLoad(double simulationTime,
					    double processingTime){
    try{
      procLoad[(int) (simulationTime-1)] += processingTime;
    } catch(Exception e){System.out.println("Out of Bound: "+simulationTime);}
  }

  public static double[] getNetworkLoad(){
    return nwLoad;}

  public static double[] getProcessingLoad(){
    return procLoad;}

  public static double[] getNwLoad(){
    return nwLoad;}

  public static double[] getProcLoad(){
    return procLoad;}

  public static double getMaxNwLoad(){
    return (Computer.maxInArray(nwLoad)/60);}
  
  public static double getMaxProcLoad(){
    return (Computer.maxInArray(procLoad)/60);}

  public static double getMaxNwQueue(){
    return(Computer.maxInArray(nwQueue));}
    
  public static double getMaxProcQueue(){
    return(Computer.maxInArray(procQueue));}

  public static double getRevocationRequests(){
    return revocationRequests;}

  public static double getMaxRevocationRequest(){
    return (Computer.maxInArray(requestArray)/60);}

  public static double getAvgRevocationRequest(){
    return (revocationRequests / (requestArray.length * 60));}

  public static double[] getRequestArray(){
    return requestArray;}

  public static double getDeltaRevocationRequests(){
    return deltaRevocationRequests;}

  public static double getMaxDeltaRevocationRequest(){
    return (Computer.maxInArray(deltaRequestArray)/60);}

  public static double getAvgDeltaRevocationRequest(){
    return (deltaRevocationRequests / (deltaRequestArray.length * 60));}

  public static double[] getDeltaRequestArray(){
    return deltaRequestArray;}

  public static void computeQueues(double simulationTimespan){
    nwQueue[0] = Math.max(0, nwLoad[0]/60 - Constants.SYSTEM_BANDWIDTH);
    procQueue[0] = Math.max(0, procLoad[0]/60 - Constants.SYSTEM_PROCESSOR);
    for(int i=1; i<simulationTimespan; i++){
      nwQueue[i] = Math.max(0, nwLoad[i]/60 + nwQueue[i-1] - Constants.SYSTEM_BANDWIDTH);
      procQueue[i] = Math.max(0, procLoad[i]/60 + procQueue[i-1] - Constants.SYSTEM_PROCESSOR);
    }
  }

  public static void printQueues(double simulationTimespan){
    for(int i=0; i<simulationTimespan; i++){
      System.out.print("["+nwQueue[i]+","+procQueue[i]+"]");
    }
  }
}
