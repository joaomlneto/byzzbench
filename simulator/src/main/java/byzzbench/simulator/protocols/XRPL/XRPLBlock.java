package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.state.PartialOrderLogEntry;
import byzzbench.simulator.transport.MessagePayload;

public class XRPLBlock implements MessagePayload, PartialOrderLogEntry<String> {

    @Override
    public String getParentHash() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getParentHash'");
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }

}

/*
 * Notes to self:
 *  - Class for Transactions?
 *  - Class for proposals, ledgers?
 *  - 
 * 
 */
