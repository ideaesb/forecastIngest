package gov.noaa.cbrfc;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface  EnsembleRepository extends CrudRepository<Ensemble, Integer> {
	List<Ensemble> findByMember(int memberIndex);
}
