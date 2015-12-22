package gov.noaa.cbrfc;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SimulationRepository extends CrudRepository<Simulation, Integer> {
	List<Simulation> findById(long id);
}
