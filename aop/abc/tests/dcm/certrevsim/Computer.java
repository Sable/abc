package certrevsim;

import java.lang.Math;
import java.util.Random;

/** This is a resource for computing the attributes
of a certificate revocation simulation run **/
public class Computer {
  
  public static Random theRandom = new Random(13);

  public static double maxInArray(double[] array){
    double maxValue = 0;
    for (int i = 0; i < array.length; i++){
      maxValue = Math.max(maxValue, array[i]);
    }
    return maxValue;
  }
 
  public static double computeValidationAttempts(double simulationTimespan, 
					    double validationFrequency){
    double averageAttempts = validationFrequency * (simulationTimespan / ((double) (24 * 60)));
    double attempts = Math.round(averageAttempts * 2 * theRandom.nextDouble());     
    return attempts;
  }
  
  public static double computeRandomTime(double simulationTimespan){
    return Math.ceil(simulationTimespan * theRandom.nextDouble());
  }
  
  public static int selectDp(int numberOfDp){
    return ((int) Math.floor((numberOfDp) * theRandom.nextDouble()));
  }
  
  public static double computeCrlSize(double systemSize, double revocationRate, int numberOfDp){
    return (8 * (128 + 51 + (9 *(systemSize*revocationRate/100) / numberOfDp) ));};
  
  public static double computeDeltaCrlSize(double systemSize, 
					   double revocationRate,
					   double revocationValidityPeriod,
					   int numberOfDp){
    // It is assumed that each certificate is valid a year
    return (8 * (128 + 51 + (((9 * systemSize*revocationRate*revocationValidityPeriod*60)/
      (365 * 24 * 60 * 60)) / (2 * numberOfDp))));};
  
  public static double computeOcspSize(){return Constants.OCSP_SIZE;};
 
  public static double computeMaxDelay(double systemSize, double revocationRate, double revocationValidityPeriod, int numberOfDp){
    double revocationSize;
    double processingTime;
    double maxNwDelay;
    double maxProcDelay;

    if (revocationValidityPeriod <=1){ //OCSP
      revocationSize = computeOcspSize();
      processingTime = Constants.OCSP_PROC;
    }
    else{ // CRL (assume that the Max delay is never a Delta CRL)
      revocationSize = computeCrlSize(systemSize, revocationRate, numberOfDp);
      processingTime = Constants.CRL_PROC;
    }

    maxNwDelay = 2 * Statistics.getMaxNwQueue() + revocationSize/Constants.SYSTEM_BANDWIDTH;
    maxProcDelay = Statistics.getMaxProcQueue() + processingTime;
    return(maxNwDelay*1000 + maxProcDelay);
  }
  
}




