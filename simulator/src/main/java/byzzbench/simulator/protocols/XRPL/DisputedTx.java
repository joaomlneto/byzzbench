package byzzbench.simulator.protocols.XRPL;

import java.util.SortedMap;

public class DisputedTx {
    private final String tx;
    SortedMap<String, Boolean> votes;
    private boolean ourVote;
    private int yesVotes;
    private int noVotes;

    public DisputedTx(String tx_, boolean ourVote_, int yesVotes_, int noVotes_, SortedMap<String, Boolean> votes_) {
        this.tx = tx_;
        this.ourVote = ourVote_;
        this.yesVotes = yesVotes_;
        this.noVotes = noVotes_;
        this.votes = votes_;
    }

    public void switchOurVote() {
        this.ourVote = !this.ourVote;
    }

    public String getTx() {
        return tx;
    }
    public boolean getOurVote() {
        return ourVote;
    }
    public int getYesVotes() {
        return yesVotes;
    }
    public int getNoVotes() {
        return noVotes;
    }
    public boolean getVoteOf(String id) {
        return votes.get(id);
    }

    public void incrementYesVotes() {
        this.yesVotes += 1;
    }

    public void incrementNoVotes() {
        this.noVotes += 1;
    }

    public void addEntryToVotesMap(String nodeId, boolean vote) {
        this.votes.put(nodeId, vote);
    }
}
