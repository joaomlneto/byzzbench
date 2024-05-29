package byzzbench.runner.service;

import jakarta.inject.Singleton;
import lombok.Getter;

import java.security.MessageDigest;

@Singleton
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
