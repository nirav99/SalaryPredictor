package salaryPredictor;

public class SalaryInstance
{
  String jobType;
  String degree;
  String major;
  String industry;
  int yearsExperience;
  int distFromMetro;
  double salary;
  
  public SalaryInstance(String jobType, String degree, String major, String industry, int yearsExp, int dist, double salary)
  {
    this.jobType = jobType;
    this.degree = degree;
    this.major = major;
    this.industry = industry;
    this.yearsExperience = yearsExp;
    this.distFromMetro = dist;
    this.salary = salary;
  }
  
  @Override
  public String toString()
  {
    StringBuilder content = new StringBuilder();
    content.append(jobType).append(",");
    content.append(degree).append(",");
    content.append(major).append(",");
    content.append(industry).append(",");
    content.append(yearsExperience).append(",");
    content.append(distFromMetro).append(",");
    content.append(salary);
    
    return content.toString();
  }
}
