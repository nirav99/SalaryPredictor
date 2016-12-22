package salaryPredictor;

import java.io.*;

/**
 * Reads the test data file in CSV format and converts it to ARFF format for prediction by WEKA algorithms
 * @author nirav99
 *
 */
public class TestFileCreator
{
	
  public TestFileCreator(File inputFile, File outputFile) throws IOException
  {
  	writeDataToOutputFile(inputFile, outputFile);
  }
  
  private void writeDataToOutputFile(File inputFile, File outputFile) throws IOException
  {
  	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
  	String line = reader.readLine(); // Discard the header 
  	
  	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
  	
  	writer.write(ARFFFileWriter.header);
  	
  	while((line = reader.readLine()) != null)
  	{
      writeLine(line, writer);
  	}
  	
  	writer.close();
  	reader.close();
  }
  
  /**
   * Write data records - remove the first 2 fields namely the job ID and the company ID.s
   * @param line
   * @param writer
   */
  private void writeLine(String line, BufferedWriter writer) throws IOException
  {
  	String[] fields = line.split(",");
  	
  	StringBuilder output = new StringBuilder();
  	
  	for(int i = 2; i < fields.length; i++)
  		output.append(fields[i]).append(",");
  	
 // 	output.setLength(output.length() - 1);
  	output.append("?");
  	writer.write(output.toString());
  	writer.newLine();
  }
  
  public static void main(String[] args)
  {
  	try
  	{
  		if(args == null || args.length != 2 || args[0].toLowerCase().contains("help"))
  		{
  			printUsage();
  			return;
  		}
  		
  		File inputFile = new File(args[0]);
  		File outputFile = new File(args[1]);
  		
  		if(!inputFile.exists() || !inputFile.isFile())
  		{
  			System.err.println("Error: input file : " + inputFile.getAbsolutePath() + " must be a valid readable file");
  			return;
  		}
  		
  		if(outputFile.isDirectory())
  		{
  			System.err.println("Error: output file : " + outputFile.getAbsolutePath() + " must be a valid writable file");
  			return;
  		}
  		TestFileCreator testFileCreator = new TestFileCreator(inputFile, outputFile);
  		
  	}
  	catch(Exception e)
  	{
  		e.printStackTrace();
  	}
  }
  
  private static void printUsage()
  {
  	System.err.println("Usage : ");
  	System.err.println("InputFile - a test_features file in CSV format to convert to ARFF format");
  	System.err.println("OutputFile - ARFF format file for which salary must be predicted");
  }
}
