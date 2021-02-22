package helper;

import lombok.SneakyThrows;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeHelper {
    private static final DateFormat defaultDateFormat = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);

    @SneakyThrows
    public static String convertDateFormat(String date, String format) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(defaultDateFormat.parse(date));
        return convertDateFormat(cal.getTime(), format);
    }

    public static String convertDateFormat(Date date, String format) {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    public static Date convertStringToDate(String date) {
        return convertStringToDate(date, Constants.DEFAULT_DATE_FORMAT);
    }

    @SneakyThrows
    public static Date convertStringToDate(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(date);
    }

    public static String getDayFromTodayByYear(int years) {
        return modifyDate(getToday(Constants.DEFAULT_DATE_FORMAT), -1, 0, years);
    }

    public static String getToday(String format) {
        DateFormat df = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        return df.format(c.getTime());
    }

    public static String getToday() {
        return getToday(Constants.DEFAULT_DATE_FORMAT);
    }

    public static String getDateFromToday(int numberOfDays) {
        return getDateFromToday(numberOfDays, 0, 0);
    }

    public static String getDateFromToday(int days, int months, int years) {
        return modifyDate(getToday(Constants.DEFAULT_DATE_FORMAT), days, months, years);
    }

    public static String getDateFromADateByYear(String specificDate, int years) {
        return modifyDate(specificDate, -1, 0, years);
    }

    public static String getDateFromADateByMonth(String specificDate, int months) {
        return modifyDate(specificDate, -1, months, 0);
    }

    public static String getDateFromADateByDay(String specificDate, int days) {
        return modifyDate(specificDate, days, 0, 0);
    }

    public static String modifyDate(String inputDate, int days, int months, int years) {
        Calendar cal = getCalendar(inputDate);
        cal.add(Calendar.YEAR, years);
        cal.add(Calendar.MONTH, months);
        cal.add(Calendar.DATE, days);
        return defaultDateFormat.format(cal.getTime());
    }

    public static int getDayFromDate(String date) {
        return getCalendar(date).get(Calendar.DATE);
    }

    /**
     * Get month from date in short format
     *
     * @param date String e.g. 10/25/1990
     * @return String in short format e.g. Jun
     */
    public static String getShortMonthFromDate(String date) {
        return getCalendar(date).getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
    }

    public static int getYearFromDate(String date) {
        return getCalendar(date).get(Calendar.YEAR);
    }

    public static Calendar getCalendar(String date) {
        Date anchorDate = convertStringToDate(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(anchorDate);

        return calendar;
    }
}
