package gov.noaa.cbrfc;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface LocationRepository extends CrudRepository<Location, String> {
	List<Location> findById(String Id);
}
