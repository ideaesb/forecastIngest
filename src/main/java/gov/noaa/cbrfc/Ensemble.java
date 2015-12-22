package gov.noaa.cbrfc;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
/**
 * 
 * @author uday kari
 *
 */
@Entity
public class Ensemble {

    @Id
    private int member; 
    
    private String timeSeriesType;
    

    ///////////////////////
    // getters and setters

    public int getMember() {
		return member;
	}
	public void setMember(int ensembleMemberIndex) {
		this.member = ensembleMemberIndex;
	}
	public String getTimeSeriesType() {
		return timeSeriesType;
	}
	public void setTimeSeriesType(String timeSeriesType) {
		this.timeSeriesType = timeSeriesType;
	}

	////////////
	// toString
	@Override
	public String toString()
	{
		return String.format(
				"Ensemble[index = '%s', type ='%s']", member, timeSeriesType); 
	}
   
}
