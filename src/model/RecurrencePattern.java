package model;

import java.util.ArrayList;
import java.util.List;

public class RecurrencePattern {
    public enum Type {
        NONE,
        DAILY,
        WEEKLY,
        MONTHLY
    }

    private final Type type;
    private final int interval;
    private final String startDate;
    private final String endDate;
    private final List<Integer> weekdays;
    private final Integer dayOfMonth;

    public RecurrencePattern(Type type, int interval, String startDate, String endDate, List<Integer> weekdays, Integer dayOfMonth) {
        this.type = type == null ? Type.NONE : type;
        this.interval = interval <= 0 ? 1 : interval;
        this.startDate = startDate == null ? "" : startDate;
        this.endDate = endDate == null ? "" : endDate;
        this.weekdays = weekdays == null ? new ArrayList<>() : new ArrayList<>(weekdays);
        this.dayOfMonth = dayOfMonth;
    }

    public static RecurrencePattern none() {
        return new RecurrencePattern(Type.NONE, 1, "", "", new ArrayList<>(), null);
    }

    public Type getType() {
        return type;
    }

    public int getInterval() {
        return interval;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public List<Integer> getWeekdays() {
        return new ArrayList<>(weekdays);
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public boolean isRecurring() {
        return type != Type.NONE;
    }

    @Override
    public String toString() {
        if (!isRecurring()) {
            return "NONE";
        }
        return type + " every " + interval + " starting " + startDate + (endDate.isBlank() ? "" : " until " + endDate);
    }
}
