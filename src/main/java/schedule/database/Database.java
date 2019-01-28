package schedule.database;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

public class Database {

    private EventDB eventDB = new EventDB();

    public Database() throws SQLException {
        eventDB.createTableIfNotExists();
    }

    public Long insertEvent(Event e) throws SQLException {
        return eventDB.insert(e);
    }

    public int updateEvent(Event e) throws SQLException {
        return eventDB.update(e);
    }

    public int deleteEvent(Event e) throws SQLException {
        return eventDB.delete(e);
    }

    public List<Event> selectEventsWhereDay(OffsetDateTime dateTime) throws SQLException {
        return eventDB.selectWhereDay(dateTime);
    }

    public List<Event> selectEventsWhereWeek(OffsetDateTime dateTime) throws SQLException {
        return eventDB.selectWhereWeek(dateTime);
    }

}
