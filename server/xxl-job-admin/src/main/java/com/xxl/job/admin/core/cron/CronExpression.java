package com.xxl.job.admin.core.cron;

import java.util.*;
import java.io.*;

public class CronExpression {
    protected static final int SECOND;
    protected static final int MINUTE;
    protected static final int HOUR;
    protected static final int DAY_OF_MONTH;
    protected static final int MONTH;
    protected static final int DAY_OF_WEEK;
    protected static final int YEAR;
    protected static final int ALL_SPEC_INT;
    protected static final int NO_SPEC_INT;
    protected static final java.lang.Integer ALL_SPEC;
    protected static final java.lang.Integer NO_SPEC;
    protected static final java.util.Map<java.lang.String, java.lang.Integer> monthMap;
    protected static final java.util.Map<java.lang.String, java.lang.Integer> dayMap;
    private final java.lang.String cronExpression;
    private java.util.TimeZone timeZone;
    protected transient java.util.TreeSet<java.lang.Integer> seconds;
    protected transient java.util.TreeSet<java.lang.Integer> minutes;
    protected transient java.util.TreeSet<java.lang.Integer> hours;
    protected transient java.util.TreeSet<java.lang.Integer> daysOfMonth;
    protected transient java.util.TreeSet<java.lang.Integer> months;
    protected transient java.util.TreeSet<java.lang.Integer> daysOfWeek;
    protected transient java.util.TreeSet<java.lang.Integer> years;
    protected transient boolean lastdayOfWeek;
    protected transient int nthdayOfWeek;
    protected transient boolean lastdayOfMonth;
    protected transient boolean nearestWeekday;
    protected transient int lastdayOffset;
    protected transient boolean expressionParsed;
    public static final int MAX_YEAR;
}
