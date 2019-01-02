package schedule;

import org.apache.commons.dbutils.QueryRunner;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class EventDB {

    private QueryRunner runner;

    public EventDB() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:./database");
        runner = new QueryRunner(dataSource);
    }

    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS event("
                + "id IDENTITY PRIMARY KEY,"
                + "name NVARCHAR(255),"
                + "description NVARCHAR(255),"
                + "startDateTime TIMESTAMP WITH TIME ZONE,"
                + "endDateTime TIMESTAMP WITH TIME ZONE"
                + ")";
        runner.update(sql);
    }

    public Long insert(Event e) throws SQLException {
        String sql = "INSERT INTO event (name, description, startDateTime, endDateTime) VALUES(?, ?, ?, ?)";
        return runner.insert(sql, this::toId, e.getName(), e.getDescription(), e.getStartDateTime(), e.getEndDateTime());
    }

    public int update(Event e) throws SQLException {
        String sql = "UPDATE event SET name = ?, description = ?, startDateTime = ?, endDateTime = ? WHERE id = ?";
        return runner.update(sql, e.getName(), e.getDescription(), e.getStartDateTime(), e.getEndDateTime(), e.getId());
    }

    public int delete(Event e) throws SQLException {
        String sql = "DELETE FROM event WHERE id = ?";
        return runner.update(sql, e.getId());
    }

    public List<Event> selectWhereDay(OffsetDateTime dateTime) throws SQLException {
        String sql = "SELECT * FROM event WHERE YEAR(startDateTime) = ? AND DAY_OF_YEAR(startDateTime) = ?";
        return runner.query(sql, this::toEventList, dateTime.getYear(), dateTime.getDayOfYear());
    }

    public List<Event> selectWhereWeek(OffsetDateTime dateTime) throws SQLException {
        String sql = "SELECT * FROM event WHERE startDateTime BETWEEN ? AND ?";
        OffsetDateTime startOfWeek = dateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay());
        OffsetDateTime endOfWeek = dateTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay());
        return runner.query(sql, this::toEventList, startOfWeek, endOfWeek);
    }

    private Long toId(ResultSet rs) throws SQLException {
        return rs.next() ? rs.getLong(1) : null;
    }

    private Event toEvent(ResultSet rs) throws SQLException {
        return rs.next() ? new Event(rs.getLong(1), rs.getNString(2), rs.getNString(3),
                rs.getObject(4, OffsetDateTime.class), rs.getObject(5, OffsetDateTime.class)) : null;
    }

    private List<Event> toEventList(ResultSet rs) throws SQLException {
        List<Event> events = new ArrayList<>();
        Event e;
        while ((e = toEvent(rs)) != null) {
            events.add(e);
        }
        return events;
    }

}
