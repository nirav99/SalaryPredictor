package salaryPredictor;

import java.io.*;
import java.util.*;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * Prepares the training data file by joining the salary and the job features files.
 * It analyzes and cleans the given data and writes the training data file in WEKA arff format.
 * @author nirav99
 *
 */
public class DataCleaner
{
  private HashMap<String, Double> jobSalaryMap; // Job ID and salary map - used to join train salaries and train features files 

  // For each nominal feature, remember all salary values
  private HashMap<String, ArrayList<Double>> salaryForCompany;
  private HashMap<String, ArrayList<Double>> salaryForIndustry;
  private HashMap<String, ArrayList<Double>> salaryForDegree;
  private HashMap<String, ArrayList<Double>> salaryForMajor;
  private HashMap<String, ArrayList<Double>> salaryForJobType;
  
  private int numLinesWritten = 0; // Number of training data records written
  
  private ArrayList<Integer> numYearsExperience;
  private ArrayList<Integer> distFromMetro;
  private ArrayList<Double> salaryList; // Store salary list separately to calculate its distribution such as average and std dev
  
  private String header; // Header of the given CSV file
  
  private boolean removeOutliers = true; // If true, removes outliers from the training data.
  // Outliers are considered as data points that are more than 2 std deviation away from the mean
  
  private LinkedList<SalaryInstance> trainingDataInstances; // List of all the given data instances
  
  private double meanSalary = 0;   // Mean of the given salaries
  private double stdDevSalary = 0; // Standard deviation of the given salaries
  
  /**
   * Class constructor
   * @param jobFeaturesFile  - train_features file
   * @param salaryInfoFile - train_salary file
   * @param tempFile - a CSV file written with salaries and features joined and outliers removed
   * @param trainingDataFile - final training data file written in ARFF format
   * @throws IOException
   */
  DataCleaner(File jobFeaturesFile, File salaryInfoFile, File tempFile, File trainingDataFile, boolean removeOutliers) throws IOException
  {
  	this.removeOutliers = removeOutliers;
    jobSalaryMap = new HashMap<String, Double>();
	
  	salaryForCompany = new HashMap<String, ArrayList<Double>>();
  	salaryForIndustry = new HashMap<String, ArrayList<Double>>();
  	salaryForDegree = new HashMap<String, ArrayList<Double>>();
  	salaryForMajor = new HashMap<String, ArrayList<Double>>();
  	salaryForJobType = new HashMap<String, ArrayList<Double>>();
  	
  	numYearsExperience = new ArrayList<Integer>();
  	distFromMetro = new ArrayList<Integer>();
  	salaryList = new ArrayList<Double>();
  	
  	trainingDataInstances = new LinkedList<SalaryInstance>();
  	
  	buildJobIDSalaryMap(salaryInfoFile);
  	System.out.println("Job Salary Map Size = " + jobSalaryMap.size());
  	processAndAnalyzeData(jobFeaturesFile);
  	
  	writeTempDataFile(tempFile);
  	System.out.println("Total number of records written : " + numLinesWritten);
  	
  	writeARFFDataFile(tempFile, trainingDataFile);
  }
  
  /**
   * Reads the job features files, obtains the corresponding salary information and analyzes the data records.
   * @param jobFeaturesFile
   * @throws IOException
   */
  private void processAndAnalyzeData(File jobFeaturesFile) throws IOException
  {
  	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jobFeaturesFile), "UTF-8"));
  	
  	String line = null;
  	
  	header = reader.readLine(); // Read the first header line and discard it
  	
  	while((line = reader.readLine()) != null)
  		processDataInstance(line);
  	
  	reader.close();
  	
  	showStatsForFeatures();
  }
  
  /**
   * Writes the intermediate cleaned data in CSV format - which is then converted to arff format.
   * If removeOutliers is set to true, it discards those data records.
   * @param tempFile
   * @throws IOException
   */
  private void writeTempDataFile(File tempFile) throws IOException
  {
  	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
  	
  	writeOutputFileHeader(this.header, writer);
  	
  	if(this.removeOutliers == false)
  	{
  		for(SalaryInstance instance : trainingDataInstances)
  		{
  			writer.write(instance.toString());
  			writer.newLine();
  			numLinesWritten++;
  		}
  	}
  	else
  	{
  		double salary;
  		
  		for(SalaryInstance instance : trainingDataInstances)
  		{
  			salary = instance.salary;
  			
  			if((salary > 2 * this.stdDevSalary + meanSalary) || (salary < meanSalary - 2 * stdDevSalary))
  				continue;
  			else
  			{
  				writer.write(instance.toString());
    			writer.newLine();
    			numLinesWritten++;
  			}
  		}
  	}
  	writer.close();
  }
  
  /**
   * Helper method to write the header file for the temporary CSV file. 
   * This header is used to generate ARFF header in the finaltraining data file.
   * @param inputHeader
   * @param writer
   * @throws IOException
   */
  private void writeOutputFileHeader(String inputHeader, BufferedWriter writer) throws IOException
  {
  	String[] fields = inputHeader.split(",");
  	
  	for(int i = 2; i < fields.length; i++)
      writer.write(fields[i] + ",");
  	
  	writer.write("salary");
  	writer.newLine();
  }
  
  /**
   * Processes a single data record - features are joined with the corresponding salary.
   * Various maps are updated to calculate distribution on salary later.
   * @param line
   */
  private void processDataInstance(String line)
  {
    String[] tokens = line.split(",");
    
    Double salary = jobSalaryMap.get(tokens[0]); // Get salary from job ID
    
    if(salary == null || salary <= 0) // invalid salary value - discard this data record
    	return;
    
    String companyID = tokens[1].replaceFirst("COMP", ""); // Remove the prefix comp from company ID and make it numeric
      
    trainingDataInstances.add(new SalaryInstance(tokens[2], tokens[3], tokens[4], tokens[5], Integer.parseInt(tokens[6]), Integer.parseInt(tokens[7]), salary));
    
    rememberSalaryForCategoricalFeature(salary, companyID, salaryForCompany);
    rememberSalaryForCategoricalFeature(salary, tokens[2], salaryForJobType);
    rememberSalaryForCategoricalFeature(salary, tokens[3], salaryForDegree);
    rememberSalaryForCategoricalFeature(salary, tokens[4], salaryForMajor);
    rememberSalaryForCategoricalFeature(salary, tokens[5], salaryForIndustry);
    
    numYearsExperience.add(new Integer(tokens[6]));
    distFromMetro.add(new Integer(tokens[7]));
    salaryList.add(salary);
  }
  
  /**
   * Stores the salary value for the given categorical (nominal) feature in the corresponding map.
   * @param salary
   * @param featureName
   * @param salaryForGivenFeature
   */
  private void rememberSalaryForCategoricalFeature(Double salary, String featureName, HashMap<String, ArrayList<Double>> salaryForGivenFeature)
  {
    ArrayList<Double> salaryList = salaryForGivenFeature.get(featureName);
    
    if(salaryList == null)
    	salaryList = new ArrayList<Double>();
    
    salaryList.add(salary);
    salaryForGivenFeature.put(featureName, salaryList);
  }
  
  /**
   * Reads the salary file and builds the map of job ID and salary
   * @param salaryInfoFile
   * @throws IOException
   */
  private void buildJobIDSalaryMap(File salaryInfoFile) throws IOException
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(salaryInfoFile), "UTF-8"));
    
    String line = null;
    String[] tokens = null;
    
    Double salary;
    
    int lineCount = 0;
    
    while((line = reader.readLine()) != null)
    {
    	if(lineCount > 0)
    	{
    		tokens = line.split(",");
    		
    		try
    		{
    		  salary = new Double(tokens[1]);
    		  jobSalaryMap.put(tokens[0], salary);
    		}
    		catch(NumberFormatException nfe)
    		{
    			System.err.println(nfe.getMessage());
    			nfe.printStackTrace();
    		}
    	}
    	lineCount++;
    }
    reader.close();
  }
  
  /**
   * Calculates some analysis for the features
   */
  private void showStatsForFeatures()
  {
  	showStatsForCategoricalFeatuers();
  	showStatsForNumericFeatures();
  }
  
  /**
   * For the categorical features, calculates mean and standard deviation for salary
   */
  private void showStatsForCategoricalFeatuers()
  {
  	System.out.println("Stats for Industry :");
  	showStatsForGivenCategoricalFeature(salaryForIndustry);
  	
  	System.out.println("Stats for Degree :");
  	showStatsForGivenCategoricalFeature(salaryForDegree);
  	
  	System.out.println("Stats for Major :");
  	showStatsForGivenCategoricalFeature(salaryForMajor);
  	
  	System.out.println("Stats for JobType :");
  	showStatsForGivenCategoricalFeature(salaryForJobType);	
  }
  
  /**
   * Helper method for showStatsForCategoricalFeatuers
   * @param salaryMap
   */
  private void showStatsForGivenCategoricalFeature(HashMap<String, ArrayList<Double>> salaryMap)
  {
    for(Map.Entry<String, ArrayList<Double>> entry : salaryMap.entrySet())
    {
      System.out.println("Key : " + entry.getKey());
      calculateStats(entry.getValue());
      System.out.println("====================");
    }
  }
  
  /**
   * Analyzes the numeric features and the salary distribution
   * For salary, it calculates mean, max, min and standard deviation.
   * 
   * For other numeric features such as years of experience and distance from metro, it calculates the correlation with salary
   */
  private void showStatsForNumericFeatures()
  {
  	System.out.println("\nSalary Stats : ");
  	DistributionStats stats = calculateStats(salaryList);
  	
  	this.meanSalary = stats.average;
  	this.stdDevSalary = stats.stdDeviation;
  	
  	double[] salaryArray = getPrimitiveArrayFromDouble(salaryList);
    System.out.println("Correlation of Num. Years and Salary : " + getCorrelation(numYearsExperience, salaryArray)); 	
    System.out.println("Correlation of distance and Salary : " + getCorrelation(distFromMetro, salaryArray)); 
  }
  
  /**
   * Returns the correlation of the given numeric feature with the salary
   * @param intList
   * @return
   */
  private double getCorrelation(ArrayList<Integer> intList, double[] salaryArray)
  {
  	double[] featureArray = getPrimitiveArray(intList);
  	double corr = new PearsonsCorrelation().correlation(salaryArray, featureArray);
    return corr;
  }
  
  /**
   * Helper method to getCorrelation
   * @param intList
   * @return
   */
  private double[] getPrimitiveArray(ArrayList<Integer> intList)
  {
  	double[] intArray = new double[intList.size()];
  	
  	for(int i = 0; i < intList.size(); i++)
  		intArray[i] = intList.get(i);
  	
  	return intArray;
  }
  
  /**
   * Helper method to getCorrelation
   * @param doubleList
   * @return
   */
  private double[] getPrimitiveArrayFromDouble(ArrayList<Double> doubleList)
  {
    double[] doubleArray = new double[doubleList.size()];
    
    for(int i = 0; i < doubleList.size(); i++)
    	doubleArray[i] = doubleList.get(i);
    
    return doubleArray;
  }
  
  /**
   * For the given salary range, calculate basis parameters such as maximum value, minimum value, mean and standard deviation
   * Also gets an estimate of the data points that might be considered outliers
   * @param salaryRanges
   * @return
   */
  private DistributionStats calculateStats(ArrayList<Double> salaryRanges)
  {
    double maxSalary = Collections.max(salaryRanges);
    double minSalary = Collections.min(salaryRanges);
    
    int total2SigmaAway = 0;
    int total3SigmaAway = 0;
    
    double mean = 0;
   
    for(int i = 0; i < salaryRanges.size(); i++)
  	  mean = mean + salaryRanges.get(i);
   
    mean = 1.0 * mean / salaryRanges.size();
   
    double stddev = 0;
   
    for(int i = 0; i < salaryRanges.size(); i++)
    {
      stddev = stddev + Math.pow(salaryRanges.get(i) - mean, 2);
    }
    stddev = Math.sqrt(stddev / salaryRanges.size());
    
    double salaryValue;
    for(int i = 0; i < salaryRanges.size(); i++)
    {
      salaryValue = salaryRanges.get(i);
      
      if((salaryValue >= mean + 2 * stddev) || (salaryValue <= mean - 2 * stddev))
      	total2SigmaAway++;
      if((salaryValue >= mean + 3 * stddev) || (salaryValue <= mean - 3 * stddev))
  	    total3SigmaAway++;
      	
    }
    
    System.out.println("Max salary : " + maxSalary);
    System.out.println("Min salary : " + minSalary);
    System.out.println("Mean salary : " + mean);
    System.out.println("Std dev : " + stddev);
    System.out.println("Total 2 sigma away : " + total2SigmaAway);
    System.out.println("Total 3 sigma away : " + total3SigmaAway);
    System.out.println("Percentage data points 2 Std Dev away : " + 1.0 * total2SigmaAway / salaryRanges.size() * 100);
    System.out.println("Percentage data points 3 Std Dev away : " + 1.0 * total3SigmaAway / salaryRanges.size() * 100);
    
    DistributionStats stats = new DistributionStats(maxSalary, minSalary, mean, stddev);
    return stats;
  }
  
  /**
   * Reads the intermediate CSV file and writes the final training data file in ARFF format
   * @param sourceFile
   * @param destFile
   * @throws IOException
   */
/*  
  private void writeARFFDataFile(File sourceFile, File destFile) throws IOException
  {
    CSVLoader loader = new CSVLoader();
    loader.setSource(sourceFile);
    Instances data = loader.getDataSet();
 
    // save ARFF
    ArffSaver saver = new ArffSaver();
    saver.setInstances(data);
    saver.setFile(destFile);
    saver.setDestination(destFile);
    saver.writeBatch();
    
    System.out.println("Completed writing ARFF data file");
  }
  */
  
  private void writeARFFDataFile(File sourceFile, File destFile) throws IOException
  {
  	ARFFFileWriter arffWriter = new ARFFFileWriter();
  	arffWriter.writeFile(sourceFile, destFile);
  	
  	System.out.println("Completed writing ARFF data file");
  }
}

class DistributionStats
{
	double maxValue;
	double minValue;
	double average;
	double stdDeviation;
	
	DistributionStats(double max, double min, double avg, double stddev)
	{
		this.maxValue = max;
		this.minValue = min;
		this.average = avg;
		this.stdDeviation = stddev;
	}
}
