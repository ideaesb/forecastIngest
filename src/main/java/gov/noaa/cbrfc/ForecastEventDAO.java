package gov.noaa.cbrfc;

/**
 * Use good old fashioned JDBC template to insert arrays into flowVolumes.  
 * JPA can only do ElementCollection (basically another table)
 * This achieves a 10 fold reduction database size
 * @author udaykari
 *
 */
public interface ForecastEventDAO {
	 public void insert(ForecastEvent employee);
     public ForecastEvent findById(int id);
}
