package gov.noaa.cbrfc;
public enum TimeSeries {
    ACCUMULATIVE("accumulative"),
    INSTANTANEOUS("instantaneous"),
    MEAN("mean");
    
	private final String value;

    TimeSeries(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TimeSeries fromValue(String v) {
        for (TimeSeries c: TimeSeries.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
