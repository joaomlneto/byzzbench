package byzzbench.simulator.controller;


import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.repository.CampaignRepository;
import byzzbench.simulator.service.ScenarioService;
import byzzbench.simulator.service.SimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequiredArgsConstructor
public class CampaignController {
    private final ScenarioService scenarioService;
    private final SimulatorService simulatorService;
    private final CampaignRepository campaignRepository;

    /**
     * Get the list of all campaigns.
     *
     * @return a list of campaign ids
     */
    @GetMapping("/campaigns")
    public List<Long> getCampaigns() {
        return StreamSupport.stream(campaignRepository.findAll().spliterator(), false)
                .map(Campaign::getCampaignId)
                .toList();
    }

    // get campaign

    /**
     * Get a campaign by id.
     *
     * @param id the id of the campaign
     * @return the campaign
     */
    @GetMapping("/campaigns/{id}")
    public Campaign getCampaign(@PathVariable Long id) {
        return campaignRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    }
}
