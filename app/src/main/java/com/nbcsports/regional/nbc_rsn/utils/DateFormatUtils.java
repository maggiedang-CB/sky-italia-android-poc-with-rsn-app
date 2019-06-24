package com.nbcsports.regional.nbc_rsn.utils;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.data_bar.StatsDate;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

import timber.log.Timber;

public class DateFormatUtils {

    public static boolean is24HourFormat = false;

    // Default value is 0 millisecond
    public static long timeBeforeLiveStreamStart = 0L;

    private final static String MONTH_DAY_YEAR_PATTERN = "MMMM d, yyyy";
    private final static String MONTH_DAY_PATTERN = "MMMM d";
    private final static String THREE_LETTER_MONTH_DAY_PATTERN = "MMM d";

    private final static String _24HOUR_MIN_PATTERN = "HH:mm";
    private final static String _12HOUR_MIN_PATTERN = "hh:mm";

    private final static String UTC_FORMAT_X = RsnApplication.getInstance().getResources().getString(R.string.time_format_string_x);
    private final static String UTC_FORMAT_Z = RsnApplication.getInstance().getResources().getString(R.string.time_format_string_z);

    private final static String X_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}";
    // eg. "2018-06-25T15:55:00+00:00" -> yyyy-MM-dd\'T\'HH:mm:ssX. Now uses yyyy-MM-dd\'T\'HH:mm:ssZZZZZ

    private final static String Z_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d\\dZ";
    // eg. "2018-03-05T13:15:30Z" ->yyyy-MM-dd\'T\'HH:mm:ss\'Z\'.

    private final static String EMPTY_STRING = "";

    public static final String NTP_DATE_PATTERN = "EEE, d MMM yyyy HH:mm:ss z";
    public static final String NTP_DATE_PATTERN_SECOND_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final String NTP_DATE_PATTERN_WITH_OFFSET = "EEE, d MMM yyyy HH:mm:ss zZ";
    public static final String NTP_DATE_PATTERN_WITH_OFFSET_SECOND_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zZ";

    public final static int MILLIS_PER_DAY = 86400000;

    public static long getEpochFromHHmmss(String time) {
        String[] s = time.split(":");

        try {
            long hr = Long.valueOf(s[0]) * 3600 * 1000;
            if (s.length <= 1) return hr;
            long min = Long.valueOf(s[1]) * 60 * 1000;
            if (s.length <= 2) return hr + min;
            long sec = Long.valueOf(s[2]) * 1000;
            return hr + min + sec;
        } catch (Exception e) {
            Timber.e(e.toString());
            return -1;
        }
    }

    private static long getDateInMillis(String publishDate) {
        String dateFormat = getDateFormat(publishDate);
        try {
            DateFormat formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(formatter.parse(publishDate));
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            Timber.e("getDateInMillis() - date formatting error, %s", e.toString());
            return -1;
        }
    }

    private static long getCurrentDateInMillis() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static Calendar getDateAsCalendar(String publishDate) {
        String dateFormat = getDateFormat(publishDate);
        try {
            DateFormat formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(formatter.parse(publishDate));
            return calendar;
        } catch (Exception e) {
            Timber.e("getDateAsCalendar() - date formatting error, %s", e.toString());
            return null;
        }
    }

    public static Calendar getDateAsCalendarForFormat(String publishDate, String dateFormat) {
        try {
            DateFormat formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(formatter.parse(publishDate));
            return calendar;
        } catch (Exception e) {
            Timber.e("getDateAsCalendar() - date formatting error, %s", e.toString());
            return null;
        }
    }

    public static int getDaysAgo(StatsDate statsDate) {
        return DateFormatUtils.getDaysAgo(statsDate.getYear(), statsDate.getMonth(), statsDate.getDate());
    }

    public static int getDaysAgo(int year, int month, int day) {
        long diff = getCurrentDateInMillis() - getDateInMillis("" + year + "-" +
                String.format("%02d", month) + "-" + String.format("%02d", day) + "T00:00:00-00:00");
        return (int) (diff/MILLIS_PER_DAY);
    }

    public static int getYearsAgo(StatsDate statsDate) {
        return (int) (Math.floor(getDaysAgo(statsDate)/365.25));
    }

    //Returns in day(s), hour(s), or min(s) if valid publishedDate, returns "Error" otherwise.
    public static String getTimeAgoText(String publishedDate) {
        if (publishedDate.isEmpty()) return EMPTY_STRING;

        long publishDateMillis = getDateInMillis(publishedDate);
        if (publishDateMillis < 0) return EMPTY_STRING;

        long diffInMills = getCurrentDateInMillis() - publishDateMillis;
        if (diffInMills < 0) return EMPTY_STRING;

        if (diffInMills > DAY_IN_MILLIS) {
            int daysAgo = (int) (diffInMills / DAY_IN_MILLIS);
            String daysAgoText = "";
            if (LocalizationManager.isInitialized()){
                daysAgoText = (daysAgo == 1) ?
                        LocalizationManager.Date.getDaysAgoSingular("" + daysAgo) :
                        LocalizationManager.Date.getDaysAgoPlural("" + daysAgo);
            }
            return daysAgoText;

        } else if (diffInMills > HOUR_IN_MILLIS) {
            int hoursAgo = (int) (diffInMills / HOUR_IN_MILLIS);
            String hoursAgoText = "";
            if (LocalizationManager.isInitialized()){
                hoursAgoText = (hoursAgo == 1) ?
                        LocalizationManager.Date.getHoursAgoSingular(""+hoursAgo) :
                        LocalizationManager.Date.getHoursAgoPlural(""+hoursAgo);
            }
            return hoursAgoText;

        } else { // (diffInMills > MINUTE_IN_MILLIS)
            int minutesAgo = (int) (diffInMills / MINUTE_IN_MILLIS);
            String minsAgoText = "";
            if (LocalizationManager.isInitialized()){
                minsAgoText = minutesAgo == 1 ?
                        LocalizationManager.Date.getMinutesAgoSingular(""+minutesAgo) :
                        LocalizationManager.Date.getMinutesAgoPlural(""+minutesAgo);
            }
            return minsAgoText;
        }
    }

    public static String getCurrentThreeLetterMonthDayText(){
        Calendar now = Calendar.getInstance();
        return LocalDateTime.fromCalendarFields(now).toString(THREE_LETTER_MONTH_DAY_PATTERN);
    }

    public static String getCurrentMonthDayText(){
        Calendar now = Calendar.getInstance();
        return LocalDateTime.fromCalendarFields(now).toString(MONTH_DAY_PATTERN);
    }

    public static String getMonthDayYearText(String publishDateUTC) {
        Calendar publishedDate = getDateAsCalendar(publishDateUTC);
        if (publishedDate == null) return EMPTY_STRING;

        return LocalDateTime.fromCalendarFields(publishedDate).toString(MONTH_DAY_YEAR_PATTERN);
    }

    public static String getHourMinText(String publishedDate) {
        Calendar publishDate = getDateAsCalendar(publishedDate);
        if (publishDate == null) return EMPTY_STRING;

        if (is24HourFormat){
            return LocalDateTime.fromCalendarFields(publishDate).toString(_24HOUR_MIN_PATTERN);

        } else {
            // 0 -> AM, 1 -> PM
            String AM_PM = publishDate.get(Calendar.AM_PM) == 0 ? "AM" : "PM";
            return LocalDateTime.fromCalendarFields(publishDate).toString(_12HOUR_MIN_PATTERN) + AM_PM;
        }
    }

    /* Android versions less than 7.0 would crash when SimpleDateFormat could not match on format yyyy-MM-dd\'T\'HH:mm:ssX.
         eg. if a date string with the format: "2018-03-05T13:15:30Z" when expecting "2018-06-25T15:55:00+00:00"
         was passed, application would crash.

         DISCLAIMER: this is a weak match, please look at X_PATTERN, Z_PATTERN.

         Now uses yyyy-MM-dd\'T\'HH:mm:ssZZZZZ
         */
    private static String getDateFormat(String publishedDate){
        if (publishedDate == null || publishedDate.isEmpty()){
            Timber.d("getDateFormat() - publishedDate is null or empty");
            return EMPTY_STRING;
        }

        Pattern xPattern = Pattern.compile(X_PATTERN);
        Matcher xMatcher = xPattern.matcher(publishedDate);
        if (xMatcher.find()){
            return UTC_FORMAT_X;
        }

        Pattern zPattern = Pattern.compile(Z_PATTERN);
        Matcher zMatcher = zPattern.matcher(publishedDate);
        if (zMatcher.find()){
            return UTC_FORMAT_Z;
        }

        Timber.d("getDateFormat() - did not find acceptable pattern");
        return EMPTY_STRING;
    }

    /**
     * Parse date string from NTP response header into joda DateTime
     *
     * @param ntpDate
     * @return joda DateTime
     * @throws Exception
     */
    public static DateTime parseDateTimeFromNTPResponse(String ntpDate) {
        DateTimeFormatter pattern = DateTimeFormat.forPattern(NTP_DATE_PATTERN);
        try {
            return pattern.parseDateTime(ntpDate).withZone(DateTimeZone.UTC);
        } catch(IllegalArgumentException e1) {
            try {
                pattern = DateTimeFormat.forPattern(NTP_DATE_PATTERN_WITH_OFFSET);
                return pattern.parseDateTime(ntpDate).withZone(DateTimeZone.UTC);
            } catch (IllegalArgumentException e2){
                try {
                    pattern = DateTimeFormat.forPattern(NTP_DATE_PATTERN_SECOND_FORMAT);
                    return pattern.parseDateTime(ntpDate).withZone(DateTimeZone.UTC);
                } catch (IllegalArgumentException e3){
                    pattern = DateTimeFormat.forPattern(NTP_DATE_PATTERN_WITH_OFFSET_SECOND_FORMAT);
                    return pattern.parseDateTime(ntpDate).withZone(DateTimeZone.UTC);
                }
            }
        }
    }

    /**
     * Get current date time with parameter joda DateTimeZone
     *
     * @param newZone
     * @return joda DateTime
     */
    public static DateTime getCurrentDateTimeWithTimeZone(DateTimeZone newZone) {
        DateTime currentLocalDateTime = new DateTime();
        return currentLocalDateTime.withZone(newZone);
    }
}
