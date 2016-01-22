package gov.noaa.cbrfc;

/**
 * rendering helper.  make a list of this simple object to pass as list into thymeleaf inline js
 * @author udaykari
 *
 */
public class PeriodStats {

	String date="";
	
	String from=""; 
	String to="";
	
	String min="";
	String p10="";
	String p30="";
	String p50="";
	String median="";
	String periodAverage = "";
	String p70 ="";
	String p90="";
	String max="";
	String average="";
	
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getMin() {
		return min;
	}
	public void setMin(String min) {
		this.min = min;
	}
	public String getP10() {
		return p10;
	}
	public void setP10(String p10) {
		this.p10 = p10;
	}
	public String getP30() {
		return p30;
	}
	public void setP30(String p30) {
		this.p30 = p30;
	}
	public String getP50() {
		return p50;
	}
	public void setP50(String p50) {
		this.p50 = p50;
	}
	public String getMedian() {
		return median;
	}
	public void setMedian(String median) {
		this.median = median;
	}
	public String getPeriodAverage() {
		return periodAverage;
	}
	public void setPeriodAverage(String periodAverage) {
		this.periodAverage = periodAverage;
	}
	public String getP70() {
		return p70;
	}
	public void setP70(String p70) {
		this.p70 = p70;
	}
	public String getP90() {
		return p90;
	}
	public void setP90(String p90) {
		this.p90 = p90;
	}
	public String getMax() {
		return max;
	}
	public void setMax(String max) {
		this.max = max;
	}
	public String getAverage() {
		return average;
	}
	public void setAverage(String average) {
		this.average = average;
	}
	
	
	
	
	
}
