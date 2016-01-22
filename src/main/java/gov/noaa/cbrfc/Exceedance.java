package gov.noaa.cbrfc;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

@Entity
public class Exceedance {

	
  protected Exceedance(){}; // JPA 
  
  /**
   * Array used to populate table
   */
  public Exceedance(double [] values)
  {
	  Percentile percentile = new Percentile(); // default is 50%
	  percentile.setData(values);
	  percentile10 = percentile.evaluate(10.0);
	  percentile30 = percentile.evaluate(30.0);
	  percentile50 = percentile.evaluate();
	  percentile70 = percentile.evaluate(70.0);
	  percentile90 = percentile.evaluate(90.0);
  }

  /**
   * Constructor for flexible period  
   * Array used to populate table
   * @values flow volumes, sorted?
   * @begin begin index
   * @length lenth 
   */
  public Exceedance(double [] values, int begin, int length)
  {
	  Percentile percentile = new Percentile(); // default is 50%
	  percentile.setData(values, begin, length);
	  percentile10 = percentile.evaluate(10.0);
	  percentile30 = percentile.evaluate(30.0);
	  percentile50 = percentile.evaluate();
	  percentile70 = percentile.evaluate(70.0);
	  percentile90 = percentile.evaluate(90.0);
  }
	   
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;

  
  ////////////////
  // Join Columns 
	
  @ManyToOne(fetch = FetchType.LAZY)
  private Location location;

  @ManyToOne(fetch = FetchType.LAZY)
  private Simulation simulation;
  
  
  /// member variables
  private Timestamp runDate;
  
  //private double minimum;
  private double percentile10;
  private double percentile30;
  //private double periodAverage;
  private double percentile50;
  private double percentile70;
  private double percentile90;
  //private double maximum;
  //private double average;
  private double observed;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public Simulation getSimulation() {
		return simulation;
	}
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}
	public Timestamp getRunDate() {
		return runDate;
	}
	public void setRunDate(Timestamp runDate) {
		this.runDate = runDate;
	}
	public double getPercentile10() {
		return percentile10;
	}
	public void setPercentile10(double percentile10) {
		this.percentile10 = percentile10;
	}
	public double getPercentile30() {
		return percentile30;
	}
	public void setPercentile30(double percentile30) {
		this.percentile30 = percentile30;
	}
	public double getPercentile50() {
		return percentile50;
	}
	public void setPercentile50(double percentile50) {
		this.percentile50 = percentile50;
	}
	public double getPercentile70() {
		return percentile70;
	}
	public void setPercentile70(double percentile70) {
		this.percentile70 = percentile70;
	}
	public double getPercentile90() {
		return percentile90;
	}
	public void setPercentile90(double percentile90) {
		this.percentile90 = percentile90;
	}
	public double getObserved() {
		return observed;
	}
	public void setObserved(double observed) {
		this.observed = observed;
	}
    /*
	public double getMinimum() {
		return minimum;
	}

	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}

	public double getPeriodAverage() {
		return periodAverage;
	}

	public void setPeriodAverage(double periodAverage) {
		this.periodAverage = periodAverage;
	}

	public double getMaximum() {
		return maximum;
	}

	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}
	*/
  
}
