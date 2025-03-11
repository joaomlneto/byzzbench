package byzzbench.simulator.repository;

import byzzbench.simulator.domain.Schedule;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ScheduleRepository extends CrudRepository<Schedule, Long> {
    Optional<Schedule> findByScheduleId(Long scheduleId);
}
