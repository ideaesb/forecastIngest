package gov.noaa.cbrfc;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
/**
 * 
 * @author uday kari
 *
 */
@Entity
public class Simulation {
	

	// IDENTITY should map well to id being defined as bigserial 
	// (which will pull nextval from simulation_id_seq) 
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    private Timestamp startDate;
    private Timestamp endDate;
    
    private Timestamp simulationDate;
 
	private int timeStepMultiplier;
    private String units;
    
    public String toString ()
    {
		return String.format(
				"Forecast[Period = '%s' - '%s', Simulation Date = '%s']", 
				startDate, endDate, simulationDate); 
    }
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public Timestamp getSimulationDate() {
		return simulationDate;
	}

	public void setSimulationDate(Timestamp simulationDate) {
		this.simulationDate = simulationDate;
	}

	public int getTimeStepMultiplier() {
		return timeStepMultiplier;
	}

	public void setTimeStepMultiplier(int timeStepMultiplier) {
		this.timeStepMultiplier = timeStepMultiplier;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}
    
    
    
}
