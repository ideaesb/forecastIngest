package gov.noaa.cbrfc;


import java.util.*;

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

@Controller
public class TracesController {

	private static final Logger log = LoggerFactory.getLogger(TracesController.class);
	
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

    @RequestMapping("/wro/traces")
    public String traces(@RequestParam(value="locId", required=false, defaultValue="") String locationId,
    		             @RequestParam(value="simId", required=false, defaultValue="") String simulationId,
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
    	
       	
       	/////////////////////////
       	/// Populate the model
       	///
    	
		model.addAttribute("locationId", location.getId());
		model.addAttribute("currentLocation", location);
        
		model.addAttribute("simulationId", simulation.getId());
		model.addAttribute("currentSimulation", simulation);
        
		log.info("Retrieving forecast events for location = " + location.getStationName() + ", simulation = " + simulation.getSimulationDate());
		long before = System.currentTimeMillis();
		List<ForecastEvent> events = forecastEventRepository.findBySimulationAndLocation(simulation, location);
		
	
		
		long after = System.currentTimeMillis();
		model.addAttribute("queryTime", "Query Took " + (after-before) + " milliseconds");
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
		
		model.addAttribute("events", events);		
		
		// from simulation (header info) create as many event dates (these were never stored)
		
        // returns template called traces
        return "traces";
    }
	
	
}
