package gov.noaa.cbrfc;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ForecastEventRepository extends CrudRepository<ForecastEvent, Long> {
	
	 List<ForecastEvent> findBySimulationAndEnsembleAndLocation(Simulation simulation, Ensemble ensemble, Location location);

}
