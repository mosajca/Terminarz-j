package schedule.database;

import java.time.OffsetDateTime;

public class Event {

    private Long id;
    private String name;
    private String description;
    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;

    public Event(Long id, String name, String description, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public Event(String name, String description, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        this(null, name, description, startDateTime, endDateTime);
    }

    public Event() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

}
