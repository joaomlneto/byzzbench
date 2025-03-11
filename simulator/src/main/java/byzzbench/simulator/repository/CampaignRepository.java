package byzzbench.simulator.repository;

import byzzbench.simulator.domain.Campaign;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CampaignRepository extends CrudRepository<Campaign, Long> {
    Optional<Campaign> findByCampaignId(Long campaignId);
}
