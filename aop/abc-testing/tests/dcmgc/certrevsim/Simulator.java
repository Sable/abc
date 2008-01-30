package certrevsim;

import jsim.event.Scheduler;
import jsim.event.Event;
import jsim.event.Entity;
import java.text.NumberFormat;

/** This class is the main simulator frameworkd **/
public class Simulator{

  private double simulationTimespan;
  private double systemSize;
  private double validationFrequency;
  private double revocationRate;
  private double revocationValidityPeriod = 1;
  private int numberOfDp = 1;
  private int deltaDegree = 1;
  
  private Scheduler theScheduler;
  private Repository[] theRepositories;
  private EndEntity[] theEndEntities;
  
  private double simulationStartTime;
  
  /** This is the default constructor **/
  public Simulator(double simulationTimespan, double systemSize,
		   double validationFrequency, double revocationRate){
    this.simulationTimespan = simulationTimespan;
    this.systemSize = systemSize;
    this.validationFrequency = validationFrequency;
    this.revocationRate = revocationRate;
  }

  /** This constructor supports validation frequency, and 
    consequently periodic schemes like CRL **/
  public Simulator(double simulationTimespan, double systemSize,
		   double validationFrequency, double revocationRate,
		   double revocationValidityPeriod){
    this(simulationTimespan, systemSize, validationFrequency, revocationRate);
    this.revocationValidityPeriod = revocationValidityPeriod;
  }
  
  /** This constructor supports the use of DPs and Delta CRLs **/
  public Simulator(double simulationTimespan, double systemSize,
		   double validationFrequency, double revocationRate,
		   double revocationValidityPeriod, int numberOfDp, int deltaDegree){
    this(simulationTimespan, systemSize, validationFrequency, 
	      revocationRate, revocationValidityPeriod);
    if(numberOfDp>1){this.numberOfDp = numberOfDp;}
    else{numberOfDp = 1;}
    this.deltaDegree = deltaDegree;
  }
  
  /** This function calculates and initializes
    all the simulation variables before distributing
    the events **/
  public void initialize(){
    Statistics.initialize(simulationTimespan);

    theScheduler = new Scheduler();
    simulationStartTime = theScheduler.currentTime();
    
    try{// Initializing Repositories
      theRepositories = new Repository[numberOfDp];
      for(int i = 0; i<numberOfDp; i++){
	theRepositories[i] = new Repository(simulationStartTime,
					    deltaDegree);
      }
    }catch(Exception e){
      System.out.println("Error initializing Repositories");
      System.exit(1);
    }

    try{
      // Initializing End Entities
      theEndEntities = new EndEntity[(int) systemSize];
      for(int i = 0; i<systemSize; i++){
	theEndEntities[i] = new EndEntity(simulationStartTime, revocationValidityPeriod,
					  numberOfDp, deltaDegree);
      }
    }catch(Exception e){
      System.out.println("Error initializing End Entities, #DP: "+numberOfDp);
      System.exit(1);
    }

    // !!!! JSIM ISSUE !!!!
    // Do the repositories First, as the scheduler often hangs
    // if they are not done first. I have no idea why.
    distributeRepositoryEvents();
    distributeEndEntityEvents();
    setSimulationEndEvent();
  }

  /** This function distributes the Repository events **/
  public void distributeRepositoryEvents(){
    if(revocationValidityPeriod <= 1){
      // Don't bother to update them - there is no caching
      // and the EE will always request new information
      // This increases the performance of the simulation
      // in the case of the OCSP scheme
    }
    else{
      for(int i = 0; i<numberOfDp; i++){
	for(int j = 1; j<=simulationTimespan; j+=revocationValidityPeriod){
	  theScheduler.schedule(new RepositoryUpdateEvent(theRepositories[i]),
				j, Constants.REPOSITORY_PRIORITY);
	}
      }

      if(deltaDegree > 1){ // I.e. we have a Delta CRL Scheme, and we have
	// to distribute delta CRL updates
	for(int i = 0; i<numberOfDp; i++){
	  for(int j = 1; j<=simulationTimespan; 
	      j+=(revocationValidityPeriod/deltaDegree)){
	    theScheduler.schedule(new RepositoryDeltaUpdateEvent(theRepositories[i]),
				  j, Constants.REPOSITORY_PRIORITY);
	  }
	}
      }
    }
  }

  /** This function distributes the End Entity events **/
  public void distributeEndEntityEvents(){
    for(int i = 0; i<theEndEntities.length; i++){
      double attempts = Computer.computeValidationAttempts(simulationTimespan, 
							     validationFrequency);
      for(int j = 0; j<attempts; j++){
	double randomTime = Computer.computeRandomTime(simulationTimespan);
	theScheduler.schedule(new EndEntityVerifyCertEvent(theEndEntities[i]),
			      randomTime, 
			      Constants.ENDENTITY_PRIORITY);
      }
    }
  }

  /** This function sets an End Mark to ensure thath the 
    full simulationTimeline will be simulated **/
  public void setSimulationEndEvent(){
    theScheduler.schedule(new SimulationEndEvent(), 
			  simulationTimespan, Constants.ENDMARK_PRIORITY);
  }

  public void run(){
    theScheduler.startSim();
    Statistics.computeQueues(simulationTimespan);
  }

  public String createReportLine(){
    double maxRequest = Statistics.getMaxRevocationRequest();
    double maxDeltaRequest = Statistics.getMaxDeltaRevocationRequest();
    double maxNwLoad = Statistics.getMaxNwLoad();
    double maxProcLoad = Statistics.getMaxProcLoad();
    double maxDelay = Computer.computeMaxDelay(systemSize, revocationRate, revocationValidityPeriod, numberOfDp);
    
    

    NumberFormat numberFormatter = NumberFormat.getInstance();
    numberFormatter.setMaximumFractionDigits(2);
    
    return(numberFormatter.format(maxRequest)+"rq/s\t\t"+
	   numberFormatter.format(maxDeltaRequest)+"rq/s\t\t"+
	   numberFormatter.format(maxNwLoad)+"b/s\t\t"+
	   numberFormatter.format(maxProcLoad)+"un/s\t\t"+
	   numberFormatter.format(maxDelay)+"ms");
    
  }

  public void printReportLine(){
    double maxRequest = Statistics.getMaxRevocationRequest();
    double maxDeltaRequest = Statistics.getMaxDeltaRevocationRequest();
    double maxNwLoad = Statistics.getMaxNwLoad();
    double maxProcLoad = Statistics.getMaxProcLoad();
    double maxDelay = Computer.computeMaxDelay(systemSize, revocationRate, revocationValidityPeriod, numberOfDp);
    
    

    NumberFormat numberFormatter = NumberFormat.getInstance();
    numberFormatter.setMaximumFractionDigits(2);
    
    System.out.println(numberFormatter.format(maxRequest)+"rq/s;"+
	   numberFormatter.format(maxDeltaRequest)+"rq/s;"+
	   numberFormatter.format(maxNwLoad)+"b/s;"+
	   numberFormatter.format(maxProcLoad)+"un/s;"+
	   numberFormatter.format(maxDelay)+"ms;");
    
  }

  public double[] getRequestArray(){
    return Statistics.getRequestArray();}

  public double[] getDeltaRequestArray(){
    return Statistics.getDeltaRequestArray();
  }

  // ********************* NESTED CLASSES ************************ //
  
  /** This is an event that triggers a repository update **/
  public class RepositoryUpdateEvent extends Event{
    public RepositoryUpdateEvent(Repository repository){
      super(repository);
    }
    public void occur(){
      ((Repository) entity).updateRevocationInformation(revocationValidityPeriod,
							deltaDegree);
    }
  }

  /** This is an event that triggers a repository update 
    of the Delta revocation information **/
  public class RepositoryDeltaUpdateEvent extends Event{
    public RepositoryDeltaUpdateEvent(Repository repository){
      super(repository);
    }
    public void occur(){
      ((Repository) entity).updateDeltaRevocationInformation(revocationValidityPeriod,
							deltaDegree);
    }
  }

  /** This is an event that triggers an end entity certificate
    validation **/
  public class EndEntityVerifyCertEvent extends Event{
    public EndEntityVerifyCertEvent(EndEntity endEntity){super(endEntity);}
    public void occur(){
      int dpNumber = Computer.selectDp(numberOfDp); // The DP in question
      
      int status = ((EndEntity) entity).checkRevocationInformation(theScheduler.currentTime(), dpNumber);
      
      if(status == Constants.EXPIRED_BASE){ // Takes precedence
	
	if(revocationValidityPeriod <= 1){ //OCSP
	  Statistics.incrementRevocationRequests(theScheduler.currentTime()-simulationStartTime);
	  Statistics.increaseNetworkLoad(theScheduler.currentTime()-simulationStartTime,
					 Computer.computeOcspSize());
	  Statistics.increaseProcessingLoad(theScheduler.currentTime()-simulationStartTime,
					    Constants.OCSP_PROC);
	}
	else{ //CRL
	  Statistics.incrementRevocationRequests(theScheduler.currentTime()-simulationStartTime);
	  Statistics.increaseNetworkLoad(theScheduler.currentTime()-simulationStartTime,
					 Computer.computeCrlSize(systemSize, revocationRate, numberOfDp));
	  Statistics.increaseProcessingLoad(theScheduler.currentTime()-simulationStartTime,
					    Constants.CRL_PROC);
	  
	}

	RevocationInfo newInfo = theRepositories[dpNumber].requestRevocationInfo();
	((EndEntity) entity).update(newInfo, dpNumber);
	this.checkDelta(dpNumber); // Deals with the possibility that a Delta CRL is needed in addition
      }
      else if(status == Constants.EXPIRED_DELTA){

	Statistics.incrementDeltaRevocationRequests(theScheduler.currentTime()-simulationStartTime);
	Statistics.increaseNetworkLoad(theScheduler.currentTime()-simulationStartTime,
				       Computer.computeDeltaCrlSize(systemSize, revocationRate, 
								    revocationValidityPeriod, numberOfDp));
	Statistics.increaseProcessingLoad(theScheduler.currentTime()-simulationStartTime,
				       Constants.CRL_PROC);

	DeltaRevocationInfo newInfo = theRepositories[dpNumber].requestDeltaRevocationInfo();
	((EndEntity) entity).updateDelta(newInfo, dpNumber);
      }
      else{} // Status == Constants.NOT_EXPIRED i.e. information is OK
    }

    public void checkDelta(int dpNumber){
      int status = ((EndEntity) entity).checkRevocationInformation(theScheduler.currentTime(), dpNumber);
      if(status == Constants.EXPIRED_DELTA){

	Statistics.incrementDeltaRevocationRequests(theScheduler.currentTime()-simulationStartTime);
	Statistics.increaseNetworkLoad(theScheduler.currentTime()-simulationStartTime,
				       Computer.computeDeltaCrlSize(systemSize, revocationRate, 
								    revocationValidityPeriod, numberOfDp));
	Statistics.increaseProcessingLoad(theScheduler.currentTime()-simulationStartTime,
				       Constants.CRL_PROC);

	DeltaRevocationInfo newInfo = theRepositories[dpNumber].requestDeltaRevocationInfo();
	((EndEntity) entity).updateDelta(newInfo, dpNumber);
      }
      else{}
    }
  }
  
  /** This is supposed to be the last event scheduled **/
  public class SimulationEndEvent extends Event{
    public SimulationEndEvent(){super(new Entity(0));}
    public void occur(){}
  }
  
  /** The main ... **/
  public static void main(String[] args){
    double timeSpan, size, valRate, revRate, validityPeriod;
    int numberOfDp, deltaPeriods;

    try{
      timeSpan = Integer.parseInt(args[0]);
      size = Integer.parseInt(args[1]);
      valRate = Integer.parseInt(args[2]);
      revRate = Integer.parseInt(args[3]);
      validityPeriod= Integer.parseInt(args[4]);
      numberOfDp= Integer.parseInt(args[5]);
      deltaPeriods= Integer.parseInt(args[6]);
      Simulator simulator = new Simulator(timeSpan, size, valRate, revRate, 
					validityPeriod, numberOfDp, deltaPeriods);
      simulator.initialize();
      simulator.run();    
      simulator.printReportLine();
    }catch(Exception e){
      System.out.println("You must provide at least 7 parameters: ");
      System.out.println("Timespan in minutes, Size in number of End Entities, Validation rate as validations per day and revocation rate as percentage revocations per year");
      System.out.println("validity period, number of DPs, deltaCRLs is set to 1 if 'inactive'");
      System.exit(1);
    }
 
  }
}
