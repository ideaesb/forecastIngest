package gov.noaa.cbrfc;


import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;



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
public class WsfController {

	private static final Logger log = LoggerFactory.getLogger(WsfController.class);
	private static final String leapYear = "2016";
	
	
	@Autowired
	private LocationRepository locationRepository;
	@Autowired
	private SimulationRepository simulationRepository;
	@Autowired
	private EnsembleRepository ensembleRepository;
	@Autowired
	private ForecastEventRepository forecastEventRepository;

	
	@ModelAttribute("allLocations")
	public List<Location> populateLocationSelction() {
		List<Location> locations = new ArrayList<Location>();
		// findAll returns "Iterable" to convert to list using Java 8
		// http://stackoverflow.com/questions/6416706/easy-way-to-change-iterable-into-collection
		locationRepository.findAll().forEach(locations::add);
		return locations;	}

	/*
	@ModelAttribute("allSimulations")
	public List<Simulation> populateSimulationSelect() {
		List<Simulation> simulations = new ArrayList<Simulation>();
		// findAll returns "Iterable" to convert to list using Java 8
		// http://stackoverflow.com/questions/6416706/easy-way-to-change-iterable-into-collection
		simulationRepository.findAll().forEach(simulations::add);
		return simulations;
	}
    */
	
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

	


   @RequestMapping("/wro/wsf")
    public String getPlotsData(@RequestParam(value="locId", required=false, defaultValue="") String locationId,
            @RequestParam(value="frDate", required=false, defaultValue="2016-04-01") String fromDate,
            @RequestParam(value="toDate", required=false, defaultValue="2016-07-31") String toDate,
            Model model) 
     {
	   	
   	
	   	// log the request in its pristine condition!
	   	if (StringUtils.isBlank(locationId)) log.info("Location ID was NULL/EMPTY");
	   	else log.info("Location ID in request  = " + locationId);  
	   	
	   	
	   	//////////////////////
	   	//  Establish Location
	   	//
	   	
	   	// set Location based on request parameter, or pick a random one!
	   	Location location = null;
	   	if (locationId.trim().length() == 0) // this will never be null, thanks to checking defaultValue=""
	   	{
	   		// pick anyone to get going
	   		Iterator<Location> locIter = locationRepository.findAll().iterator();
		   	//nextInt is normally exclusive of the top value,
		   	//so add 1 to make it inclusive
		   	//ThreadLocalRandom.current().nextInt(0, locIter.size if available!);
	   		if (locIter.hasNext()) location = locIter.next();
	   		if (location == null) log.error("Will not start page because no RANDOM location was found");
	   		else log.info("Randomly picked location ID = " + location.getId() + ", " + location.getStationName());
	   	}
	   	else
	   	{
	        log.info("Retrieving location ID " + locationId );
	        location = locationRepository.findOne(locationId);
            if (location == null)  log.error("Location objected generated for ID " + locationId + " was NULL");
	        else log.info("Successfully retrieved Location (" + locationId + ") = " + location.getStationName());
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

       	
       	
       	////////////////////////////////////////////////////
       	/// Populate the model with request parameters 
       	///

        
     	model.addAttribute("locationId", location.getId());
     	model.addAttribute("currentLocation", location);
       	model.addAttribute("fromDate", ffDate); log.info("From Date " + mmmdd.format(ffDate));
   		model.addAttribute("toDate", ttDate); log.info("To Date " + mmmdd.format(ttDate));
     	

   		
   		////////////////////////////////////////////////////////////////////////////////////
   		//  F I N D   B Y    L O C A T I O N
   		//
   		
 		long before = System.currentTimeMillis();
		List<ForecastEvent> events = forecastEventRepository.findByLocation(location);
  
		// couple of cheap queries 
		List<Simulation> simulations = new ArrayList<Simulation>();
		simulationRepository.findAll().forEach(simulations::add);
		List<Ensemble> ensembles = new ArrayList<Ensemble>();
		ensembleRepository.findAll().forEach(ensembles::add);
		
		
		long after = System.currentTimeMillis();
		model.addAttribute("queryTime", "Query Run Time " + (after-before) + " milliseconds");
	    log.info("Query Run Time for forecastEventRepository.findByLocation(" + location.getId() + ") = " + ((after-before)/1000) + " seconds");
		
		/////////////////////////////////////////////////////
		// Sanity Checks 

		if (events == null || events.isEmpty())
		{
			log.error("No events found.  FAILURE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!.");
		}
		else
		{
	        // how many events??
			
			ForecastEvent event = events.get(0);
			log.info("event ID " + event.getEnsemble().getMember() + ", size of flowData = " + StringUtils.split(event.getFlowData(),',').length);
			log.info(events.size() + " ensembles found.  Success: (Query Took " + (after-before) + " milliseconds).");
		}

		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		//  Imagine a spreadsheet.  Columns are simulation (forecast/run) dates, rows are ensemble members (years)
		//  Cells[i=year][j=forecast date] are cumulative FLOWS forecast for that year, according to run date according 
		//  to some "filter" logic (or none, meaning no filter or simply add all flows for that year - cumulative flows).  
		//  The only filter considered for now is from/to and by default it is Apr to July.  
		//  So only flows between April to July are summed up for any given run. 
		//  This filtered flows are in cumulative period flows.   
		//  HOWEVER (per John Friday 22-JAN-2016), if the simulation date itself is within April-July (from/to), there is 
		//  usually an actual volume upto that data - so no need for model data.  
		//  Sooooo - if run date is AFTER from date and BEFORE to Date, cumulativePeriodFlows should add from currentDate (not from) 

	    // this will add up all the flows between from and to dates for percentile states
		double [][] cumulativeFlows = new double[ensembles.size()][simulations.size()];   // will add all flows 
		double [][] cumulativePeriodflows = new double[ensembles.size()][simulations.size()];  // will add only flows between start/end (from/to  dates provided)
			    
	    
		for (ForecastEvent event : events)
		{

            Simulation simulation = event.getSimulation(); 
            int simIndex = NumberUtils.toInt(simulation.getId() + "") - 1;
            LocalDate simulationDate   = LocalDate.parse(formatter.format(simulation.getSimulationDate()));
            // the first flow in comma separted string is interpreted as current day 
            LocalDate currentDate   = simulationDate;
            int currentYear = currentDate.getYear();
            
            Ensemble ensemble = event.getEnsemble(); 
            int ensIndex = (int) ensemble.getMember() - 1981;
            String [] flows = StringUtils.split(event.getFlowData(),',');
			
			
            // cast start and end month/days in terms of current simulation run date			
		 	SimpleDateFormat partialFormatter = new SimpleDateFormat("-MM-dd"); 
			LocalDate startDate = LocalDate.parse(currentYear + partialFormatter.format(ffDate));
			LocalDate endDate   = LocalDate.parse(currentYear + partialFormatter.format(ttDate));		
	
			// check if current date is between start and end 
			for (String flow: flows)
			{
				   
				  double raw = NumberUtils.toDouble(flow,0.0); 
				  double kaf = (raw * 1.983471)/1000;
				
				  // add everything in cumulative 
				  if (raw > 0.0) cumulativeFlows[ensIndex][simIndex] = cumulativeFlows[ensIndex][simIndex] + kaf;
				  
				  // if current flow is within forecast period 
				  if( (currentDate.isEqual(startDate) || currentDate.isAfter(startDate)) && 
					  (currentDate.isEqual(endDate) || currentDate.isBefore(endDate))  )
				  {
						//log.info("ADDING currentDate = " + currentDate + " since it is  between " + startDate + ", and " + endDate);  
						
					    // if simulation date is in the forecast period 
					     if ( (simulationDate.isEqual(startDate) || simulationDate.isAfter(startDate)) &&
					          (simulationDate.isEqual(endDate) || simulationDate.isBefore(endDate)) )
					     {
					    	 
					    	 //////////////////////////////////////////////
					    	 // use simulation date as start date
					    	 
							  // if current flow is within forecast period (simulation date to end date)
					    	  // again - why? - because until simulation date we already have observed flows, no need to forecasts
							  if( (currentDate.isEqual(simulationDate) || currentDate.isAfter(simulationDate)) && 
								  (currentDate.isEqual(endDate) || currentDate.isBefore(endDate))  )
							  {
								  
								  if (raw > 0.0) cumulativePeriodflows[ensIndex][simIndex] = cumulativePeriodflows[ensIndex][simIndex] + kaf;
							  }
					     }
					     else
					     {
					    	// simulation date is not in forecast period, just add the flow
					        // just add the flow to period flows 
						    if (raw > 0.0) cumulativePeriodflows[ensIndex][simIndex] = cumulativePeriodflows[ensIndex][simIndex] + kaf;
					     }
				  }
				  else
				  {
		 				//log.info("IGNORING currentDate = " + currentDate + "... NOT between " + startDate + ", and " + endDate);  
				  }
					  
				  if (currentDate.isEqual(LocalDate.parse(currentYear+"-12-31"))) currentDate = LocalDate.parse(currentYear+"-01-01");
				  else  currentDate = currentDate.plusDays(1);
			}
				
				
		} // end looping on all events for current location  

		
		
		
		
		// generate stats for each simulation
		List<PeriodStats> stats = new ArrayList<PeriodStats>();

		for (Simulation simulation : simulations)
		{
		
		   int s = NumberUtils.toInt(simulation.getId() + "") - 1;
		   double [] period = new double[ensembles.size()];
		   double [] all    = new double[ensembles.size()];
		   
		   
		   
		   for (Ensemble ensemble: ensembles)
		   {
			   int e = (int) ensemble.getMember() - 1981;
			   period[e] = cumulativePeriodflows[e][s];
			   all[e] = cumulativeFlows[e][s];
		   }
		   
		   
		   
		   // get the gold - stats
		   
		   
			
			  Percentile percentile = new Percentile(); // default is 50%
			  percentile.setData(period);
			  double percentile10 = percentile.evaluate(10.0);
			  double percentile30 = percentile.evaluate(30.0);
			  double percentile50 = percentile.evaluate();
			  double percentile70 = percentile.evaluate(70.0);
			  double percentile90 = percentile.evaluate(90.0);
			  
			  List<Double> cumFlowCollection = new ArrayList<Double>();
			  double sum = 0.0;
			  double cum = 0.0;
			  for (int i = 0; i < ensembles.size(); i++) 
			  {
				  cumFlowCollection.add(period[i]);
				  sum = sum + period[i];
				  cum = cum + all[i];
			   }
			  
			  double maximum = Collections.max(cumFlowCollection);
			  double minimum = Collections.min(cumFlowCollection);
			  
			  double periodAverage = sum/ensembles.size();
			  double average = cum/ensembles.size();
		   
			  // populate the simple stat object
		      PeriodStats p = new PeriodStats();
		      
		      p.date = formatter.format(simulation.getSimulationDate());
		      //p.from = formatter.format(ffDate);
		      //p.to   = formatter.format(ttDate);
		      
		      p.min = Math.round(minimum) + "";
		      p.max = Math.round(maximum) + "";
		   
		      //p.periodAverage = Math.round(periodAverage)+"";
		      //p.average = Math.round(average)+"";
		      
		      
		      //p.median = Math.round(percentile50)+"";
		      
		      p.p10 = Math.round(percentile10)+"";
		      p.p30 = Math.round(percentile30)+"";
		      p.p50 = Math.round(percentile50)+"";
		      p.p70 = Math.round(percentile70)+"";
		      p.p90 = Math.round(percentile90)+"";
		      
		      stats.add(p);
		      
		}
		
		
		model.addAttribute("simulations", simulations);
		model.addAttribute("ensembles", ensembles);
		model.addAttribute("stats", stats);
		
		return "wsf";
	 }
}
