package byzzbench.simulator.protocols.XRPL;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XRPLLedger implements Serializable {
    private String Id;
    private String parentId;
    private int seq;
    List<String> transactions;

    public XRPLLedger(String parentID_, int seq_, List<String> transactions) {
        this.parentId = parentID_;
        this.seq = seq_;
        this.transactions = new ArrayList<>();
        this.applyTxes(transactions);
        this.calculateSHA512_256();
    }
    /* 
    public XRPLLedger(XRPLLedger l) {
        this.Id = l.Id;
        this.parentId = l.parentId;
        this.seq = l.seq;
        this.transactions = new ArrayList<>();
        for (String tx : l.transactions) {
            this.transactions.addLast(tx);            
        }
    }
*/
    public String getId() {
        return this.Id;
    }

    public String getParentId() {
        return this.parentId;
    }

    public int getSeq() {
        return this.seq;
    }

    public void applyTxes(List<String> txes) {
        for (String tx : txes) {
            this.transactions.addLast(tx);            
        }
    }

    public List<String> getTransactions() {
        return this.transactions;
    }

    public boolean equals(XRPLLedger l) {
        return this.Id.equals(l.getId()) && this.parentId.equals(l.getParentId()) && this.seq == l.getSeq() && this.areTxesSame(l.getTransactions());
    }

    private boolean areTxesSame(List<String> transactions2) {
        Collections.sort(transactions2);
        Collections.sort(this.transactions);
        return transactions2.equals(this.transactions);
    }

    private byte[] getBytes() {
        try {
            // Convert parentId to bytes
            byte[] parentIdBytes = parentId != null ? parentId.getBytes(StandardCharsets.UTF_8) : new byte[0];

            // Convert seq to bytes
            byte[] seqBytes = ByteBuffer.allocate(Integer.BYTES).putInt(seq).array();

            // Convert transactions to bytes
            int transactionsLength = transactions != null ? transactions.size() : 0;
            ByteBuffer transactionsBuffer = ByteBuffer.allocate(Integer.BYTES);
            transactionsBuffer.putInt(transactionsLength);
            for (String transaction : transactions) {
                byte[] transactionBytes = transaction != null ? transaction.getBytes(StandardCharsets.UTF_8) : new byte[0];
                transactionsBuffer.putInt(transactionBytes.length);
                transactionsBuffer.put(transactionBytes);
            }

            // Combine all byte arrays
            ByteBuffer buffer = ByteBuffer.allocate(
                    Integer.BYTES + parentIdBytes.length +
                    seqBytes.length + 
                    transactionsBuffer.position());

            // Add the lengths and byte arrays to the buffer
            buffer.putInt(parentIdBytes.length);
            buffer.put(parentIdBytes);
            buffer.put(seqBytes);
            buffer.put(transactionsBuffer.array(), 0, transactionsBuffer.position());

            return buffer.array();
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to byte array", e);
        }
    }

    public void calculateSHA512_256() {
        try {
            //The original XRPL uses SHA512/256 but it appears Java doesn't support it internally.
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(this.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 64) { 
                hashtext = "0" + hashtext; 
            } 
            this.Id = hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
