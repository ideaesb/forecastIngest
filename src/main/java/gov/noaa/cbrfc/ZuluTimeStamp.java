package gov.noaa.cbrfc;

import java.sql.Timestamp;
import java.util.Date;
import java.time.ZonedDateTime;
import java.time.Instant;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Utility method to convert "YYYY-MM-DD", "hh:mm:ss" in XML into SQL Timestamp (UTC) 
 * 
 * This has depndency on java 8 new java.time package.  Use Joda Time for lower versions of jdk.  
 * 
 * where:
 *
 *    YYYY = four-digit year
 *    MM   = two-digit month (01=January, etc.)
 *    DD   = two-digit day of month (01 through 31)
 *    hh   = two digits of hour (00 through 23) (am/pm NOT allowed)
 *    mm   = two digits of minute (00 through 59)
 *    ss   = two digits of second (00 through 59)
 *    
 * @author udaykari
 *
 */
public class ZuluTimeStamp {
	
	public static Timestamp toInstant(XMLGregorianCalendar dateValue, XMLGregorianCalendar timeValue)
	{
		int year = dateValue.getYear();
		int month = dateValue.getMonth();
		int day = dateValue.getDay();

		int hour = timeValue.getHour();
		int minute = timeValue.getMinute();
		int second = timeValue.getSecond();
		
		String iso8601DateStr = year + "-" + prependZero(month)  + "-" + prependZero(day) + "T" + prependZero(hour) + ":" + prependZero(minute) + ":" + prependZero(second) + "Z";
		// http://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date, 
		// java.util.Date date = Date.from( Instant.parse( "2014-12-12T10:39:40Z" )); Adam for input known to be UTC, zulu (which is the case by NOAA/NWS/RFC convention)
		// Date date = Date.from( ZonedDateTime.parse( "2014-12-12T10:39:40Z" ).toInstant() ); Basil Bourque for other timezones
		
		Date zuluDate = Date.from( Instant.parse( iso8601DateStr ) );
		
		// return SQL wrapper 
		return new Timestamp(zuluDate.getTime());
		
	}

	/**
	 * prepend a zero(0) if int less than 10
	 * @param num
	 * @return
	 */
	private static String prependZero(int num)
	{
		if (num < 10) return "0"+num;
		else return num+"";
			
	}
}
