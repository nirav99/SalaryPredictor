package salaryPredictor;

import java.io.*;

public class DataCleanerDriver
{
  public static void main(String[] args)
  {
  	try
  	{
  		if(args == null || args.length < 4 || args.length > 5 || args[0].toLowerCase().contains("help"))
  		{
  			printUsage();
  			return;
  		}
  		
  		File featuresFile = new File(args[0]);
  		File salaryFile = new File(args[1]);
  		File trainingDataFile = new File(args[2]);
  		File arffDataFile = new File(args[3]);
  		boolean removeOutliers = (args.length >= 5 && args[4].equalsIgnoreCase("false")) ? false : true;
  		
  		DataCleaner dataGen = new DataCleaner(featuresFile, salaryFile, trainingDataFile, arffDataFile, removeOutliers);
  	}
  	catch(Exception e)
  	{
  		System.err.println(e.getMessage());
  		e.printStackTrace();
  	}
  }
  
  public static void printUsage()
  {
  	System.err.println("Usage :");
  	System.err.println("features_file : File having features (train_features)");
  	System.err.println("salaries_file : File having salary (train_salaries)");
  	System.err.println("temp_file : File where to write joined and cleaned data");
  	System.err.println("arff_file : Final training data file in ARFF format for use with WEKA");
  	System.err.println("remove_outliers : boolean (true/false) to determine whether to remove outliers from data [default=true]");
  }
}
