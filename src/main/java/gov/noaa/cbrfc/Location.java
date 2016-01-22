package gov.noaa.cbrfc;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Location {
	
    
	@Id
    private String id;

    private String stationName;
    
    private double x;
    private double y;
    private double z;

    private String pointType;
    private String responsibleRfc;
    

    // required by JPA, not to be used directly
    protected Location() {}

    

    ////////////
	// toString
	@Override
	public String toString()
	{
		return String.format(
				"Location[id = '%s', stationName = '%s', ('%s', '%s')]", 
				id, stationName, pointType, responsibleRfc); 
	}


    ///////////////////
    // getters, setters

	
	public String getId() {
		return id;
	}


	public void setId(String locationId) {
		this.id = locationId;
	}


	public String getStationName() {
		return stationName;
	}


	public void setStationName(String stationName) {
		this.stationName = stationName;
	}


	public double getX() {
		return x;
	}



	public void setX(double x) {
		this.x = x;
	}



	public double getY() {
		return y;
	}



	public void setY(double y) {
		this.y = y;
	}



	public double getZ() {
		return z;
	}



	public void setZ(double z) {
		this.z = z;
	}



	public String getPointType() {
		return pointType;
	}



	public void setPointType(String pointType) {
		this.pointType = pointType;
	}



	public String getResponsibleRfc() {
		return responsibleRfc;
	}



	public void setResponsibleRfc(String responsibleRfc) {
		this.responsibleRfc = responsibleRfc;
	}




}
