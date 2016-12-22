package salaryPredictor;

import java.io.*;
import java.util.*;

/**
 * Builds the header file for the given CSV.
 * This is a bad approch to hardcode headers in the code, but when I try to use weka classes to programmatically generate
 * a header, there are unusual weka exceptions while executing the program. Interertingly, this behavior is not seen when
 * running from Eclipse. Thus, for convenience, I am going to write hardcoded header file.
 * @author nirav99
 *
 */
public class ARFFFileWriter
{
  public static final String header = "@relation salary_training_data\n\n" + 

"@attribute jobType {CFO,CEO,VICE_PRESIDENT,MANAGER,JUNIOR,JANITOR,CTO,SENIOR}\n" +
"@attribute degree {MASTERS,HIGH_SCHOOL,DOCTORAL,BACHELORS,NONE}\n" + 
"@attribute major {MATH,NONE,PHYSICS,CHEMISTRY,COMPSCI,BIOLOGY,LITERATURE,BUSINESS,ENGINEERING}\n" +
"@attribute industry {HEALTH,WEB,AUTO,FINANCE,EDUCATION,OIL,SERVICE}\n" +
"@attribute yearsExperience numeric\n" + 
"@attribute milesFromMetropolis numeric\n" +
"@attribute salary numeric\n\n" +

"@data\n";
  
  public ARFFFileWriter()
  {
  	
  }
  
  public void writeFile(File csvFile, File outputFile) throws IOException
  {
  	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF-8"));
  	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
  	
  	writer.write(header);
  	String line;
  	
  	line = reader.readLine(); // Discard the header file
  	while((line = reader.readLine()) != null)
  	{
  		writer.write(line);
  		writer.newLine();
  	}

  	writer.close();
  	reader.close();
  }
}
