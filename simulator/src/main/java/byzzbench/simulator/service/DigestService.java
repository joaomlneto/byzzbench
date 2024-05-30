package byzzbench.simulator.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service
@Getter
public class DigestService {
    private final MessageDigest md;

    public DigestService() {
        try {
            this.md = MessageDigest.getInstance("SHA-1");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
