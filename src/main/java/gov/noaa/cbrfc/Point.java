package gov.noaa.cbrfc;

public enum Point {

	STREAMFLOW ("streamflow"),
	RESERVOIR ("reservoir");
	
	private String text;
	
	Point(String text)
	{
		this.text = text;
	}
	
	public static Point fromString(String text) {
	    if (text != null) {
	      for (Point p : Point.values()) {
	        if (text.equalsIgnoreCase(p.text)) {
	          return p;
	        }
	      }
	    }
	    throw new IllegalArgumentException("No constant with text " + text + " found");
	  }
	
}
