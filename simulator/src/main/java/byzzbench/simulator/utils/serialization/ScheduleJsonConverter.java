package byzzbench.simulator.utils.serialization;

import byzzbench.simulator.domain.Schedule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;

import java.io.IOException;

/**
 * Converts a schedule object to a JSON string and vice versa.
 */
public class ScheduleJsonConverter implements AttributeConverter<Schedule, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Schedule attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert to Json", e);
        }
    }

    @Override
    public Schedule convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, Schedule.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not convert from Json", e);
        }
    }
}
