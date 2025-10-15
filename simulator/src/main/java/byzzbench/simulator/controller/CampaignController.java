package byzzbench.simulator.controller;


import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * REST controller for CRUD operations on {@link Campaign}.
 */
@RestController
@RequiredArgsConstructor
@Log
public class CampaignController {
    private final CampaignRepository campaignRepository;

    /**
     * Get the list of all available campaigns.
     *
     * @return a list of campaign ids
     */
    @GetMapping("/campaigns")
    public List<Long> getCampaigns() {
        return StreamSupport.stream(campaignRepository.findAll().spliterator(), false)
                .map(Campaign::getCampaignId)
                .toList();
    }

    /**
     * Create a campaign
     */
    @PostMapping("/campaigns")
    public void createCampaign() {
        Campaign campaign = new Campaign();
        campaign.setNumScenarios(10);
        campaignRepository.save(campaign);
    }

    /**
     * Get a campaign by id.
     *
     * @param campaignId the id of the campaign
     * @return the campaign
     */
    @GetMapping("/campaigns/{campaignId}")
    public Campaign getCampaign(@PathVariable Long campaignId) {
        log.info("Fetching campaign with id: " + campaignId);
        return campaignRepository.findById(campaignId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    }

    /**
     * Start running scenarios for a given campaign.
     *
     * @param campaignId the id of the campaign
     */
    @PostMapping("/campaigns/{campaignId}/start")
    public void start(@PathVariable Long campaignId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Stop the current scenario.
     */
    @PostMapping("/campaigns/{campaignId}/stop")
    public void stop(@PathVariable Long campaignId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
