package gov.noaa.cbrfc;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ExceedanceRepository extends CrudRepository<Exceedance, Integer>{
	//List<Exceedance> findById(@Param("id") String id);
	List<Exceedance> findByLocation_Id(@Param("location_id") String location_id);
}
