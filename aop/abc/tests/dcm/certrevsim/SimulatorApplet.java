package certrevsim;

import java.applet.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.lang.Integer;
import java.lang.String;
import java.text.ParseException;

public class SimulatorApplet extends Applet implements ActionListener, ItemListener{

  // GUI Panels
  private Panel generalInputPanel;
  private Panel schemeInputPanel;
  private Panel commandPanel;
  private Panel graphPanel;
  private Panel resultPanel;

  // Text Fields and Labels for the generalInputPanel
  private TextField timespanField;
  private TextField sizeField;
  private TextField averageValidationFrequencyField;
  private TextField revocationRateField;
  private Label timespanLabel;
  private Label sizeLabel;
  private Label averageValidationFrequencyLabel;
  private Label revocationRateLabel;
 
  // Text Fields and Labels for the CRL schemeInputPanel
  private Label crlSchemeLabel;
  private TextField revInfoValidityPeriodField;
  private Label revInfoValidityPeriodLabel;

  // Text Fields and Labels for the CRL_DP_DELTA schemeInputPanel
  private Label crlDpDeltaSchemeLabel;
  //private TextField revInfoValidityPeriodField;
  //private Label revInfoValidityPeriodLabel;
  private TextField numberOfDpField;
  private Label numberOfDpLabel;
  private TextField deltaDegreeField;
  private Label deltaDegreeLabel;

  // Text Fields and Labels for the OCSP schemeInputPanel
  private Label ocspSchemeLabel;
 
  // Fields for the commandPanel
  private Label statusDescriptionLabel;
  private Label statusLabel;
  private Label schemeChoiceLabel;
  private Choice schemeChoice;
  private Button okButton;

  // Fields for the graph Panel
  // NONE ...

  // Text Fields for the resultPanel
  private Label header1;
  private Label header2;
  private Label header3;
  private Label header4;
  private Label header5;
  private Label header6;
  private Label header7;
  private Label header8;
  private TextArea resultsArea;

  // The General Simulation Variables
  private int timeSpan;
  private int size;
  private int averageValidationFrequency;
  private int revocationRate;
 
  // The CRL specific simulation variables
  private int revInfoValidityPeriod;

  // The CRL_DP_DELTA specific simulation variables
  private int numberOfDp;
  private int deltaDegree;

  private GridBagConstraints c;
  private String currentScheme = "CRL";
  private double[] currentGraphArray = {};
  private double[] currentDeltaGraphArray ={};

  public void init() {

    this.setBackground(Color.white);
    this.setLayout(new GridBagLayout());
    
    c = new GridBagConstraints();
    c.fill=GridBagConstraints.BOTH;
    c.insets = new Insets(1,1,1,1);
    
    // Setting up the 5 panels
    c.gridx=0; c.gridwidth=1; c.gridheight=1; c.weightx=1; c.weighty=0;
    
    generalInputPanel = new Panel();
    makeGeneralInputPanel();
    c.gridy=0; 
    this.add(generalInputPanel,c);

    schemeInputPanel = new Panel();

    if(currentScheme == "CRL"){
      makeCrlInputPanel();   // CRL is default
    }
    else if(currentScheme == "CRL_DP_DELTA"){
      makeCrlDpDeltaInputPanel();
    }
    else if(currentScheme == "OCSP"){
      makeOcspInputPanel();
    }

    c.gridy=1; 
    this.add(schemeInputPanel,c);

    commandPanel = new Panel();
    makeCommandPanel();
    c.gridy=2; 
    this.add(commandPanel,c);

    c.weighty=1;

    graphPanel = new Panel();
    makeGraphPanel();
    c.gridy=3; 
    this.add(graphPanel,c);

    c.weighty=0;

    resultPanel = new Panel();
    makeResultPanel();
    c.gridy=4;
    this.add(resultPanel,c);

    okStatus();
  }

  public void makeGeneralInputPanel(){
    generalInputPanel.setBackground(Color.lightGray);
    generalInputPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill=GridBagConstraints.BOTH;
    c.insets = new Insets(5,5,5,5);

    c.weightx=c.weighty=0;c.gridwidth=1; c.gridheight=1;
    
    c.gridx=0; c.gridy=0; 
    timespanLabel = new Label("Simulation Timespan (min)");
    generalInputPanel.add(timespanLabel,c);
   
    c.gridx=1; c.gridy=0; c.weighty = 0.5;
    timespanField = new TextField(5);
    generalInputPanel.add(timespanField,c);
    
    c.gridx=2; c.gridy=0; c.weighty =0;
    sizeLabel = new Label("System Size (EndEntities)");
    generalInputPanel.add(sizeLabel,c);
       
    c.gridx=3; c.gridy=0; c.weighty = 0.5;
    sizeField = new TextField(5);
    generalInputPanel.add(sizeField,c);
    
    c.gridx=0; c.gridy=1; c.weighty =0;
    averageValidationFrequencyLabel = new Label("Avg. Validations per day");
    generalInputPanel.add(averageValidationFrequencyLabel,c);

    c.gridx=1;c.weighty = 0.5;
    averageValidationFrequencyField = new TextField(5);
    generalInputPanel.add(averageValidationFrequencyField,c);
    
    c.gridx=2; c.gridy=1; c.weighty =0;
    revocationRateLabel = new Label("Revocation Rate");
    generalInputPanel.add(revocationRateLabel,c);

    c.gridx=3;c.weighty = 0.5;
    revocationRateField = new TextField(5);
    generalInputPanel.add(revocationRateField,c);
  }
 
  public void makeCrlInputPanel(){
    schemeInputPanel.setBackground(Color.lightGray);
    schemeInputPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill=GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5,5,5,5);

    c.weightx=c.weighty=0;c.gridwidth=1; c.gridheight=1;
    
    c.gridx=0; c.gridy=0;
    
    crlSchemeLabel= new Label("Scheme: CRL");
    schemeInputPanel.add(crlSchemeLabel,c);
    
    c.gridx=1; c.gridy=1;
    revInfoValidityPeriodLabel=new Label("Revocation Information Validity (min)");
    schemeInputPanel.add(revInfoValidityPeriodLabel,c);
    
    c.gridx=2;
    revInfoValidityPeriodField = new TextField(5);
    schemeInputPanel.add(revInfoValidityPeriodField,c);
  }
  
  public void makeCrlDpDeltaInputPanel(){
    schemeInputPanel.setBackground(Color.lightGray);
    schemeInputPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill=GridBagConstraints.NONE;
    c.insets = new Insets(5,5,5,5);

    c.weightx=c.weighty=0;c.gridwidth=1; c.gridheight=1;

    c.gridx=0; c.gridy=0;
    
    crlDpDeltaSchemeLabel= new Label("Scheme: Delta CRL with DP");
    schemeInputPanel.add(crlDpDeltaSchemeLabel,c);

    c.gridx=2; c.gridy=0;
    revInfoValidityPeriodLabel=new Label("Rev. Info. Validity (min)");
    schemeInputPanel.add(revInfoValidityPeriodLabel,c);
    
    c.gridx=3;
    revInfoValidityPeriodField = new TextField(5);
    schemeInputPanel.add(revInfoValidityPeriodField,c);

    c.gridx=0; c.gridy=1;
    numberOfDpLabel = new Label("Number of DPs");
    schemeInputPanel.add(numberOfDpLabel,c);
    
    c.gridx=1;
    numberOfDpField = new TextField(5);
    schemeInputPanel.add(numberOfDpField,c);
    
    c.gridx=2;
    deltaDegreeLabel = new Label("Delta CRLs per period");
    schemeInputPanel.add(deltaDegreeLabel,c);
    
    c.gridx=3;
    deltaDegreeField = new TextField(5);
    schemeInputPanel.add(deltaDegreeField,c);
    
  }


  public void makeOcspInputPanel(){
    schemeInputPanel.setBackground(Color.lightGray);
    schemeInputPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill=GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5,5,5,5);

    c.weightx=c.weighty=0;c.gridwidth=1; c.gridheight=1;

    c.gridx=0; c.gridy=0;
    
    ocspSchemeLabel= new Label("Scheme: OCSP");
    schemeInputPanel.add(ocspSchemeLabel,c);

    
  }

  public void makeCommandPanel(){
    commandPanel.setBackground(Color.lightGray);
    commandPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill=GridBagConstraints.NONE;
    c.insets = new Insets(5,5,5,5);

    c.weightx=c.weighty=0;c.gridwidth=1; c.gridheight=1;

    c.gridx = 0; c.gridy = 0;
    statusDescriptionLabel = new Label("Status: ");
    commandPanel.add(statusDescriptionLabel,c);

    c.gridx = 1; c.gridwidth = 2; c.weightx=1;
    statusLabel = new Label("OK");
    commandPanel.add(statusLabel,c);
    
    c.gridx = 2; c.gridy = 1; c.gridwidth = 1; c.weightx = 0;
    schemeChoiceLabel = new Label("Select Scheme:");
    commandPanel.add(schemeChoiceLabel,c);

    c.gridx = 3; c.gridy = 1; c.gridwidth = 1; c.weightx = 0;
    schemeChoice = new Choice();
    schemeChoice.add("CRL");
    schemeChoice.add("CRL_DP_DELTA");
    schemeChoice.add("OCSP");
    schemeChoice.addItemListener(this);
    commandPanel.add(schemeChoice,c);

    c.gridx = 4; c.gridy = 1; c.gridwidth = 1; c.weightx = 0;
    okButton = new Button("Simulate");
    okButton.setActionCommand("ok");
    okButton.addActionListener(this);
    commandPanel.add(okButton,c);

  }

  public void makeGraphPanel(){
    graphPanel.setBackground(Color.lightGray);    
  }

  public void makeResultPanel(){
    resultPanel.setBackground(Color.lightGray);
    resultPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill=GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(1,1,1,1);

    c.weightx=c.weighty=0;c.gridwidth=1; c.gridheight=1;

    c.gridx = 0; c.gridy = 0;
    
    header1 = new Label("MAX_RQ");
    c.gridx=0; c.gridy=0; c.gridwidth=1; c.gridheight=1;
    c.weightx=1; c.weighty=0;
    resultPanel.add(header1, c);

    header2 = new Label("MAX_D_RQ");
    c.gridx=1; c.gridy=0; c.gridwidth=1; c.gridheight=1;
    c.weightx=1; c.weighty=0;
    resultPanel.add(header2, c);

    header3 = new Label("MAX_NW");
    c.gridx=2; c.gridy=0; c.gridwidth=1; c.gridheight=1;
    c.weightx=1; c.weighty=0;
    resultPanel.add(header3, c);

    header4 = new Label("MAX_PROC");
    c.gridx=3; c.gridy=0; c.gridwidth=1; c.gridheight=1;
    c.weightx=1; c.weighty=0;
    resultPanel.add(header4, c);


    header5 = new Label("MAX_DELAY");
    c.gridx=4; c.gridy=0; c.gridwidth=1; c.gridheight=1;
    c.weightx=1; c.weighty=0;
    resultPanel.add(header5, c);

    
    resultsArea = new TextArea();
    resultsArea.setEditable(false);
    c.gridx=0; c.gridy=1; c.gridwidth=5; c.gridheight=5;
    c.weightx=1; c.weighty=1;
    resultPanel.add(resultsArea,c);  
  }

  public void itemStateChanged(ItemEvent event){

    if(event.getItem() == "CRL"){
      status("CRL selected");
      currentScheme = "CRL";
      schemeInputPanel.removeAll();
      makeCrlInputPanel();
      validate();
      repaint();
    }
    else if(event.getItem() == "CRL_DP_DELTA"){
      status("CRL_DP_DELTA selected");
      currentScheme = "CRL_DP_DELTA";
      schemeInputPanel.removeAll();
      makeCrlDpDeltaInputPanel();
      validate();
      repaint();
    }
    else if(event.getItem() == "OCSP"){
      status("OCSP selected");
      currentScheme = "OCSP";
      schemeInputPanel.removeAll();
      makeOcspInputPanel();
      validate();
      repaint();
      
    }
    else{}

  }

  public void actionPerformed(ActionEvent event) {
    if(event.getActionCommand() == "ok") {

      status("Parsing input");

      try{
	parseGeneralInput();
      }catch(Exception e){
	errorStatus("Invalid input fields");
	return;
	
      }

      Simulator theSimulator;

      if(schemeChoice.getSelectedItem() == "CRL"){
	try{
	  parseCrlInput();
	}catch(Exception e){
	  errorStatus("Invalid CRL specific field");
	  return;
	}
	
	try{
	status("Instantiating CRL simulation");
	theSimulator = new Simulator(timeSpan, size, averageValidationFrequency, revocationRate, revInfoValidityPeriod);
	}catch(Exception e){
	  errorStatus("Error initializing the sim.");
	  System.out.println(e);
	  return;
	}
      }

      else if(schemeChoice.getSelectedItem() == "CRL_DP_DELTA"){
      	try{
	  parseCrlDpDeltaInput();
	}catch(Exception e){
	  errorStatus("Invalid CRL_DP_DELTA specific field");
	  return;
	}
	
	try{
	  status("Instantiating CRL_DP_DELTA simulation");
	theSimulator = new Simulator(timeSpan, size, averageValidationFrequency, revocationRate, revInfoValidityPeriod, numberOfDp, deltaDegree);
	}catch(Exception e){
	  errorStatus("Error initializing the sim.");
	  return;
	}
      }
      else if(schemeChoice.getSelectedItem() == "OCSP"){
	try{
	  parseOcspInput();
	}catch(Exception e){
	  errorStatus("Invalid OCSP specific field");
	  return;
	}
	
	try{
	  status("Instantiating OCSP simulation");
	  theSimulator = new Simulator(timeSpan, size, averageValidationFrequency, revocationRate);
	}catch(Exception e){
	  errorStatus("Error initializing the sim.");
	  return;
	}
      }
      else{
	errorStatus("Error: No scheme selected");
	return;
      }

      try{
	status("Initiating Simulator, please Wait");
	theSimulator.initialize();
      }catch(Exception e){
	errorStatus("Error initializing the Simulator");
	return;
      }

      try{
	status("Running Simulator");
	theSimulator.run();
      }catch(Exception e){
	errorStatus("Error running Simulator");
	return;
      }
      
      resultsArea.append(theSimulator.createReportLine()+"\n");
      currentGraphArray = theSimulator.getRequestArray();
      currentDeltaGraphArray = theSimulator.getDeltaRequestArray();
      graphPanel.setBackground(Color.lightGray);
      plotGraph(currentGraphArray, Color.black);
      if (currentDeltaGraphArray.length != 0){
	plotGraph(currentDeltaGraphArray, Color.blue);
      }
      //plotTest(null, null);
      
      theSimulator = null;
      okStatus();
    }
    else {
    }
    
  }

  public void parseGeneralInput() throws ParseException{
    timeSpan = Integer.parseInt(timespanField.getText());
    size = Integer.parseInt(sizeField.getText()); 
    averageValidationFrequency = Integer.parseInt(averageValidationFrequencyField.getText());
    revocationRate = Integer.parseInt(revocationRateField.getText());
    if(revocationRate>100 || revocationRate<0){
      throw(new ParseException("Invalid percentage",0));
    }
  }

  public void parseCrlInput() throws ParseException{
    revInfoValidityPeriod = Integer.parseInt(revInfoValidityPeriodField.getText());
  }

  public void parseCrlDpDeltaInput() throws ParseException{
    revInfoValidityPeriod = Integer.parseInt(revInfoValidityPeriodField.getText());
    numberOfDp = Integer.parseInt(numberOfDpField.getText());
    deltaDegree = Integer.parseInt(deltaDegreeField.getText());
  }

  public void parseOcspInput() throws ParseException{}

  public void errorStatus(String message){
    statusLabel.setForeground(Color.red);
    statusLabel.setText(message);
    this.validate();
    this.repaint();
  }

  public void okStatus(){
    statusLabel.setForeground(Color.blue);
    statusLabel.setText("OK");
    this.validate();
    this.repaint();
  }

  public void status(String message){
    statusLabel.setForeground(Color.blue);
    statusLabel.setText(message);
    this.validate();
    this.repaint();
  }

  public void paint(Graphics g){
    if (currentGraphArray.length != 0){
      plotGraph(currentGraphArray, Color.black);
    }
    if (currentDeltaGraphArray.length != 0){
      plotGraph(currentDeltaGraphArray, Color.red);
    }
  }

 
  public void plotGraph(double[] yArray, Color color){

    Dimension dimension = graphPanel.getSize();
    Graphics g = graphPanel.getGraphics();
    g.setColor(color);
    
    int width = dimension.width;
    int height = dimension.height;

    int x = yArray.length;
    double y = max(yArray);

    double normXFactor = ((double) width)/((double) x);
    double normYFactor = ((double) height)/y;
    
    int[] newXArray = new int[x];
    for(int i = 0; i<x; i++){
	newXArray[i] = (int) Math.round(i*normXFactor);
    }
    
    int[] newYArray = new int[x];
    for(int i = 0; i<x; i++){
      newYArray[i] = (int) Math.round(height - yArray[i]*normYFactor);
    }

    for(int i = 0; i<(x-1); i++){
      g.drawLine(newXArray[i],newYArray[i],newXArray[i+1],newYArray[i+1]);
    }
    this.validate();
    this.repaint();
  }
  
  public double max(double[] array){
      double max = 0;
      int x = array.length;
      for(int i = 0; i<x; i++){
	if(array[i] > max){max = array[i];}
      }
      return(max);
  }  
}
