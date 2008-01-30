package certrevsim;

/** This class holds constant variables and codes for 
the Certificate Revocation Simulator**/
public class Constants {

  /** Constant system variable: Network Bandwidth **/
  public static final double SYSTEM_BANDWIDTH = 10000000;
  /** Constant system variable: Processing capacity **/
  public static final double SYSTEM_PROCESSOR = 1000;
  /** Constant system variable: CRL request processing load **/
  public static final double CRL_PROC = 1;
  /** Constant system variable: CRL w. DP/ DElta request processing load **/
  public static final double CRL_DP_DELTA_PROC = 1;
  /** Constant system variable: OCSP request processing load **/
  public static final double OCSP_PROC = 43;
  /** Constant system variable: OCSP response size **/
  public static final double OCSP_SIZE = 1000;

  /** The priority of the Repositories on the Scheduler**/
  public static final int REPOSITORY_PRIORITY = 3;
  /** The priority of the End Entities on the Scheduler**/
  public static final int ENDENTITY_PRIORITY = 5;
  /** The priority of the End mark on the Scheduler **/
  public static final int ENDMARK_PRIORITY = 10;
  /** The normal standard deviation for the End Entity requests **/
  public static final int NORMAL_STANDARD_DEVIATION = 1;
  /** This code represents the CRL scheme **/
  public static final int SCHEME_CRL = 0;
  /** This code represents the CRL scheme w. DP and Delta**/  
  public static final int SCHEME_CRL_DP_DELTA = 3;
  /** This code represents the OCSP scheme **/  
  public static final int SCHEME_OCSP = 10;

  /** This code is a code from the EE that indicates that
    the Base Revocation Information is expired - new info
    have to be obtained **/
  public static final int EXPIRED_BASE = 0;
  /** This code indicates that the delta info is expired,
    but not the base info.**/
  public static final int EXPIRED_DELTA = 1;
  /** This code indicates that all revocation info is
    valid **/
  public static final int NOT_EXPIRED = 2;
}
