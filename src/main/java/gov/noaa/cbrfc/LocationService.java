package gov.noaa.cbrfc;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

	@Autowired
	private LocationRepository locationRepository;
	
	public List<Location> findAll() {
		List<Location> locations = new ArrayList<Location>();
		// findAll returns "Iterable" to convert to list using Java 8
		// http://stackoverflow.com/questions/6416706/easy-way-to-change-iterable-into-collection
		locationRepository.findAll().forEach(locations::add);
		return locations;
	}
	
}
