package gov.noaa.cbrfc;


import java.time.LocalDate;
import java.util.*;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

@Controller
public class QpfController {

	
	private static final Logger log = LoggerFactory.getLogger(QpfController.class);
	private static final String leapYear = "2016";
	
	@Autowired
	private LocationRepository locationRepository;
	@Autowired
	private SimulationRepository simulationRepository;
	@Autowired
	private ForecastEventRepository forecastEventRepository;
	
	
	
	@ModelAttribute("allLocations")
	public List<Location> populateLocationSelction() {
		List<Location> locations = new ArrayList<Location>();
		// findAll returns "Iterable" to convert to list using Java 8
		// http://stackoverflow.com/questions/6416706/easy-way-to-change-iterable-into-collection
		locationRepository.findAll().forEach(locations::add);
		return locations;	}
	
	@ModelAttribute("allSimulations")
	public List<Simulation> populateSimulationSelect() {
		List<Simulation> simulations = new ArrayList<Simulation>();
		// findAll returns "Iterable" to convert to list using Java 8
		// http://stackoverflow.com/questions/6416706/easy-way-to-change-iterable-into-collection
		simulationRepository.findAll().forEach(simulations::add);
		return simulations;
	}

	@ModelAttribute("allDates")
	public List<Date> populateDates() {
		List<Date> dates = new ArrayList<Date>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		LocalDate newYearDay = LocalDate.parse(leapYear  + "-01-01");
		for (int i = 0; i < 366; i++) 
		{
			try
			{
			  dates.add(formatter.parse(newYearDay.plusDays(i).toString()));
			}
			catch (Exception e)
			{
				log.error(e.toString());
			}
		}
		return dates;
	}

    @RequestMapping("/wro/wsftable")
    public String traces(@RequestParam(value="locId", required=false, defaultValue="") String locationId,
    		             @RequestParam(value="simId", required=false, defaultValue="") String simulationId,
    		             @RequestParam(value="frDate", required=false, defaultValue="2016-04-01") String fromDate,
    		             @RequestParam(value="toDate", required=false, defaultValue="2016-07-31") String toDate,
    		             @RequestParam(value="id", required=false) List<String> ids,  
    		               Model model) {
   
    	
 
    	
    	
    	//////////////////////////////////////////////
    	// Get location as first id (fix this later!)
    	
    	if (ids != null && ids.size() > 0)
    	{
    	  locationId = StringUtils.trimToEmpty(ids.get(0)); 
    	  simulationId = StringUtils.trimToEmpty(ids.get(1)); 
    	}
    	
    	log.info("Location ID  = " + locationId); if (StringUtils.isBlank(locationId)) log.info("Location ID was NULL/EMPTY");
    	log.info("Simulation ID = " + simulationId); if (StringUtils.isBlank(simulationId)) log.info("Simulation ID was NULL/EMPTY");
    	
    	
    	
    	//////////////////////
    	//  Establish Location
    	//
    	
    	// set Location based on request parameter, or pick a random one!
    	Location location = null;
    	if (locationId.trim().length() == 0) // this will never be null, thanks to checking defaultValue=""
    	{
    		// pick anyone
    		Iterator<Location> locIter = locationRepository.findAll().iterator();
    		if (locIter.hasNext()) location = locIter.next();
    		if (location == null) log.error("Will not start page because no RANDOM location was found");
    	}
    	else
    	{
            log.info("Retrieving location ID " + locationId );
            location = locationRepository.findOne(locationId);
        
            if (location == null)  log.error("Location objected generated for ID " + locationId + " was NULL");
            else log.info("Successfully retrieved Location (" + locationId + ") = " + location.getStationName());
    	}
        
    	
    	/////////////////////////
    	//  Establish Simulation
    	//
    	
    	Simulation simulation = null;
       	if (NumberUtils.toInt(simulationId) == 0) 
    	{
    		// pick anyone
    		Iterator<Simulation> simIter = simulationRepository.findAll().iterator();
    		if (simIter.hasNext()) simulation = simIter.next();
    		else log.error("No Simulations were found in the database");
    		if (simulation == null) log.error("Will not start page because no RANDOM/SEED simulation was found");
    	}
    	else
    	{
            log.info("Retrieving Simulation # " + simulationId );
            List<Simulation> simulationz = simulationRepository.findById(NumberUtils.toLong(simulationId));
            if (simulationz == null || simulationz.isEmpty())
            {
            	log.error("No Simulation Found for ID " + simulationId);
            }
            else if (simulationz.size() > 1)
            {
            	log.error("THIS SHOULD NOT HAPPEN.  More than one simulations Found for ID " + simulationId + ".  Database (autoincrement) corrupted or overriden??");
            }
            else
            {
              simulation = simulationRepository.findById(NumberUtils.toLong(simulationId)).get(0);
            }
        
            if (simulation == null)  log.error("Simulation # " + simulationId + " was NULL");
            else log.info("Successfully retrieved Simulation (" + simulationId + ") = " + simulation.getSimulationDate());
    	}    			
    	
       	
       	
       	///////////////////////
       	//  Establish from and to
       	//
       	
       	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
       	SimpleDateFormat mmmdd = new SimpleDateFormat("MMM-dd"); 
       	
       	Date ffDate = new Date(); ffDate.setYear(NumberUtils.toInt(leapYear,2016));
   		Date ttDate = new Date(); ttDate.setYear(NumberUtils.toInt(leapYear,2016));
       	
       	if (StringUtils.contains(fromDate, '-'))
       	{
       		try
       		{
       		  ffDate = formatter.parse(fromDate);  
	   		  ttDate = formatter.parse(toDate);
       		}
       		catch (ParseException p)
       		{
       			log.error("Error parsing from/to dates " + p.toString());
       		}
	   	}
       	else
       	{
       		// parse longs
       		ffDate = new Date(NumberUtils.toLong(fromDate));  
       		ttDate = new Date(NumberUtils.toLong(toDate));
       		
       	}

       	
       	
       	/////////////////////////
       	/// Populate the model
       	///

       	
       	model.addAttribute("fromDate", ffDate); log.info("From Date " + mmmdd.format(ffDate));
   		model.addAttribute("toDate", ttDate); log.info("To Date " + mmmdd.format(ttDate));
     	
    	
		model.addAttribute("locationId", location.getId());
		model.addAttribute("currentLocation", location);
        
		model.addAttribute("simulationId", simulation.getId());
		model.addAttribute("currentSimulation", simulation);
		
		//model.addAttribute("fromDate", fDate); model.addAttribute("toDate", tDate);
        
		//log.info("Retrieving forecast events for location = " + location.getStationName() + ", simulation = " + simulation.getSimulationDate());
		
		
		
		/////////////////////////////////////////////////////
		// Run, "get all ensembles for this location" query
		
		long before = System.currentTimeMillis();
		List<ForecastEvent> events = forecastEventRepository.findBySimulationAndLocation(simulation, location);

		
		long after = System.currentTimeMillis();
		model.addAttribute("queryTime", "Query Run Time " + (after-before) + " milliseconds");
		if (events == null || events.isEmpty())
		{
			log.error("No events found.  Failure.");
		}
		else
		{
	        // how many events
			
			ForecastEvent event = events.get(0);
			log.info("event ID " + event.getEnsemble().getMember() + ", size of flowData = " + StringUtils.split(event.getFlowData(),',').length);
			
			log.info(events.size() + " ensembles found.  Success: (Query Took " + (after-before) + " milliseconds).");
		}
	
		
		// this is of size 30, for years 1981-2010
		model.addAttribute("events", events);		
		
		
		
		// this will add up all the flows between from and to dates for percentile states
		double [] cumulativeFlows = new double[events.size()];   // will add all flows 
		double [] cumulativePeriodflows = new double[events.size()];  // will add only flows between start/end
		

		log.info("Doing LocalDate.parse(" + formatter.format(simulation.getSimulationDate()) + ")");
		LocalDate currentDate   = LocalDate.parse(formatter.format(simulation.getSimulationDate()));

		int currentYear = currentDate.getYear();
		log.info("Current Year " + currentYear);
		
	 	SimpleDateFormat partialFormatter = new SimpleDateFormat("-MM-dd"); 
		
		log.info("Doing LocalDate.parse(" + currentYear + partialFormatter.format(ffDate) + ")");
		LocalDate startDate = LocalDate.parse(currentYear + partialFormatter.format(ffDate));
		log.info("Doing LocalDate.parse(" + currentYear + partialFormatter.format(ttDate) + ")");
		LocalDate endDate   = LocalDate.parse(currentYear + partialFormatter.format(ttDate));		

		int e = 0; // iterate from 0 to 30 ensembles
		for (ForecastEvent event : events)
		{
			currentDate   = LocalDate.parse(formatter.format(simulation.getSimulationDate()));
			String [] flowStrArr = StringUtils.split(event.getFlowData(),',');
			
			// check if current date is between start and end 
			for (String flowStr: flowStrArr)
			{
			   
				double raw = NumberUtils.toDouble(flowStr,0.0); double kaf = (raw * 1.983471)/1000;
				if (raw > 0.0) cumulativeFlows[e] = cumulativeFlows[e] + kaf;
				
			  if( (currentDate.isEqual(startDate) || currentDate.isAfter(startDate)) && 
				  (currentDate.isEqual(endDate) || currentDate.isBefore(endDate)) )
			  {
				//log.info("ADDING currentDate = " + currentDate + " since it is  between " + startDate + ", and " + endDate);  
				
				if (raw > 0.0) cumulativePeriodflows[e] = cumulativePeriodflows[e] + kaf;
			  }
			  else
			  {
 				//log.info("IGNORING currentDate = " + currentDate + "... NOT between " + startDate + ", and " + endDate);  
			  }
			  
			  if (currentDate.isEqual(LocalDate.parse(currentYear+"-12-31"))) currentDate = LocalDate.parse(currentYear+"-01-01");
			  else  currentDate = currentDate.plusDays(1);
			}
			
			e++;
		}

		
	
		
		  Percentile percentile = new Percentile(); // default is 50%
		  percentile.setData(cumulativePeriodflows);
		  double percentile10 = percentile.evaluate(10.0);
		  double percentile30 = percentile.evaluate(30.0);
		  double percentile50 = percentile.evaluate();
		  double percentile70 = percentile.evaluate(70.0);
		  double percentile90 = percentile.evaluate(90.0);
		  
		  List<Double> cumFlowCollection = new ArrayList<Double>();
		  double sum = 0.0;
		  double cum = 0.0;
		  for (int i = 0; i < cumulativePeriodflows.length; i++) {
			  cumFlowCollection.add(cumulativePeriodflows[i]);
			  sum = sum + cumulativePeriodflows[i];
			  cum = cum + cumulativeFlows[i];
		   }
		  
		  double maximum = Collections.max(cumFlowCollection);
		  double minimum = Collections.min(cumFlowCollection);
		  double periodAverage = sum/cumulativePeriodflows.length;
		  double average = cum/cumulativeFlows.length;;
		  
		  
		  
		  ////
		  // populate
		  
		  model.addAttribute("maximum", maximum);
		  model.addAttribute("minimum", minimum);
		  model.addAttribute("percentile90", percentile90);
		  model.addAttribute("percentile70", percentile70);
		  model.addAttribute("percentile50", percentile50);
		  model.addAttribute("percentile30", percentile30);
		  model.addAttribute("percentile10", percentile10);
		  model.addAttribute("periodAverage", periodAverage);
		  model.addAttribute("average", average);
		
        // returns template called qpf
        return "qpf";
    }

	
}
