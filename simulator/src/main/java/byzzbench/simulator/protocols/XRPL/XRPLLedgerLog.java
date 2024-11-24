package byzzbench.simulator.protocols.XRPL;

import java.util.ArrayList;
import java.util.List;

import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.state.LogEntry;

// Will store as a tree but the longest path (the active path)
// will be returned to the methods
public class XRPLLedgerLog extends CommitLog {
    List<XRPLLedgerLogEntry> list = new ArrayList<>();

    @Override
    public void add(LogEntry entry) {
        this.list.add(entry);
    }

    @Override
    public int getLength() {
        return this.list.size();
    }

    @Override
    public LogEntry get(int index) {
        return this.list.get(index);
    }

}
