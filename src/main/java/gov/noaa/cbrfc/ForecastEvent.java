package gov.noaa.cbrfc;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


/**
 * many-to-many UNIDIRECTIONAL (default JPA) with extra columns 
 * http://stackoverflow.com/questions/5127129/mapping-many-to-many-association-table-with-extra-columns
 * http://stackoverflow.com/questions/7197181/jpa-unidirectional-many-to-one-and-cascading-delete
 * @author uday kari
 *
 */
@Entity
public class ForecastEvent  {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    ////////////////
	// Join Columns 
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Location location;

	@ManyToOne(fetch = FetchType.LAZY)
	private Ensemble ensemble;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Simulation simulation;
	
	
	/////////
	//  Extra columns - based on Event Complex Type

	//private double [] flowVolumes;
	


	private Timestamp flowTimestamp; 
	private double flowVolume;
	private int flag;
	private String flagSource;
	private String comment;
	private String userName; // user is a reserved PostGreSQL keyword, so use userName
	private boolean missing;
	// getters, setters
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Ensemble getEnsemble() {
		return ensemble;
	}

	public void setEnsemble(Ensemble ensemble) {
		this.ensemble = ensemble;
	}

	public Simulation getSimulation() {
		return simulation;
	}

	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}

	public Timestamp getFlowTimestamp() {
		return flowTimestamp;
	}

	public void setFlowTimestamp(Timestamp timestamp) {
		this.flowTimestamp = timestamp;
	}

	public double getFlowVolume() {
		return flowVolume;
	}

	public void setFlowVolume(double value) {
		this.flowVolume = value;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getFlagSource() {
		return flagSource;
	}

	public void setFlagSource(String flagSource) {
		this.flagSource = flagSource;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String user) {
		this.userName = user;
	}

	public boolean isMissing() {
		return missing;
	}

	public void setMissing(boolean missing) {
		this.missing = missing;
	}
	/*
	public double[] getFlowVolumes() {
		return flowVolumes;
	}

	public void setFlowVolumes(double[] flowVolumes) {
		this.flowVolumes = flowVolumes;
	}
	*/
	////////////
	// toString
	@Override
	public String toString()
	{
		return String.format(
				"Event[timeStamp = '%s', value ='%s']", flowTimestamp, flowVolume); 
	}
   
	
	
	

}
