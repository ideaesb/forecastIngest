package gov.noaa.cbrfc;

import java.text.SimpleDateFormat;

import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class GreetingController {

	private static final Logger log = LoggerFactory.getLogger(GreetingController.class);
	
	@Autowired
	private LocationService locationService;
	
	@Autowired
	private LocationRepository locationRepository;
	@Autowired
	private ExceedanceRepository xceedanceRepository;
	
	@ModelAttribute("allLocations")
	public List<Location> populateLocations() {
	    return this.locationService.findAll();
	}
	
    @RequestMapping("/wro/esp")
    public String greeting(@RequestParam(value="id", required=false, defaultValue="") String currentId, Model model) {
        
    	Location location = null;
    	if (currentId == null || currentId.trim().length() == 0) 
    	{
    		// pick anyone
    		Iterator<Location> locIter = locationRepository.findAll().iterator();
    		if (locIter.hasNext()) location = locIter.next();
    		
    		if (location == null) log.error("Will not start page because no RANDOM location was found");
    	}
    	else
    	{
            log.info("Retrieving location ID " + currentId );
            location = locationRepository.findOne(currentId);
        
            if (location == null)  log.error("Location retrieved by " + currentId + " was NULL");
            else log.info("Successfully retrieved Location (" + currentId + ") = " + location.getStationName());
    	}
        
		model.addAttribute("locationId", location.getId());
		model.addAttribute("currentLocation", location);
        
        List<Exceedance> exceeds = xceedanceRepository.findByLocation_Id(location.getId());
        List<ExceedanceForDisplay> plainExceeds = new ArrayList<ExceedanceForDisplay>();
        
        model.addAttribute("exceedances", exceeds);
        
        // compute the javascript inline 
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        int count=0;
        for (Exceedance x: exceeds)
        {
        	// get the data and put in longs
        	long p10 = Math.round(x.getPercentile10());
        	long p30 = Math.round(x.getPercentile30());
        	long p50 = Math.round(x.getPercentile50());
        	long p70 = Math.round(x.getPercentile70());
        	long p90 = Math.round(x.getPercentile90());
        	
        	
        	ExceedanceForDisplay ed = new ExceedanceForDisplay();
        	
        	ed.setDate(formatter.format(x.getRunDate()));
        	ed.setP10(p10+"");
        	ed.setP30(p30+"");
        	ed.setP50(p50+"");
        	ed.setP70(p70+"");
        	ed.setP90(p90+"");
        	
        	
        	plainExceeds.add(ed);
        	
        	
        	// preceed with comma, if not first element
        	if (count > 0) sb.append(",");
        	
        	// start
        	sb.append("{");
        	
        	sb.append("date: "); sb.append(formatter.format(x.getRunDate())); sb.append(",");

        	sb.append("p10: "); sb.append(p10); sb.append(",");
        	sb.append("p30: "); sb.append(p30); sb.append(",");
        	sb.append("p50: "); sb.append(p50); sb.append(",");
        	sb.append("p70: "); sb.append(p70); sb.append(",");
        	sb.append("p90: "); sb.append(p90); //sb.append(",");
        	
        	sb.append("}");
        	
        	
        	count++;
        }
        sb.append("];");
        model.addAttribute("espdata", sb.toString());
        model.addAttribute("simplexs", plainExceeds);
        
        
        return "greeting";
    }

    
    
}