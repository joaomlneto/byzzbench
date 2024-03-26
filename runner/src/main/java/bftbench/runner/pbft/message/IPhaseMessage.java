package bftbench.runner.pbft.message;

import java.io.Serializable;

public interface IPhaseMessage extends Serializable {
    long getViewNumber();
    long getSequenceNumber();
    byte[] getDigest();
}
