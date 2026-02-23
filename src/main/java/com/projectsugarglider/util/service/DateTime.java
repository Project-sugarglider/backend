package com.projectsugarglider.util.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

import org.springframework.stereotype.Service;

/**
 * 시간관련 util 모음
 */
@Service
public class DateTime {

    private static final DateTimeFormatter YYYYMMDDHHMM = 
    DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter YYYYMMDD =
        DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * utc 현재시각
     */
    public OffsetDateTime utcTime (){
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    /**
     * utc에서 kst로 변환
     */
    public OffsetDateTime kstTime(OffsetDateTime dateTime){
        return dateTime
            .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
            .toOffsetDateTime();
    }

    /**
     * kst 현재시각
     */
    public OffsetDateTime kstNow(){
        return OffsetDateTime.now(ZoneOffset.UTC)
            .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
            .toOffsetDateTime();
    }

    /**
     * kst 기준 내일 같은시각
     */
    public OffsetDateTime kstPlusDays(long days) {
        return kstNow().plusDays(days);
    }

    /**
     * OffsetDateTime 날짜값을 
     * 
     * - String year  %04d
     * - String month %02d
     * - String day   %02d
     * - String time  %02d00
     * 
     * 으로 바꿔주는 함수
     */
    public String[] toYYYYMMDDHHMM(OffsetDateTime dt){
        String y = String.format("%04d", dt.getYear());
        String m = String.format("%02d", dt.getMonthValue());
        String d = String.format("%02d", dt.getDayOfMonth());
        String t = String.format("%02d00", dt.getHour());
        return new String[]{y, m, d, t};
    }

    /**
     * kst 현재시각을 
     * 
     * - String YYYYMMDD
     * 
     * 로 바꿔주는 함수
     */
    public String kstNowYYYYMMDD() {
        LocalDate todayKst = kstNow().toLocalDate();
        return todayKst.format(YYYYMMDD);
    }

    /**
     * String 날짜값을 OffsetDateTime으로 바꿔주는 함수
     * 
     * KST기준YYYYMMDDHHMM으로 바뀜
     */
    public OffsetDateTime parseKst(String date, String time) {
    return LocalDateTime
        .parse(date + time, YYYYMMDDHHMM)
        .atZone(KST)
        .toOffsetDateTime();
    }

    /**
     * int year값 반환
     */
    public int timeYear(OffsetDateTime dateTime) {
        return dateTime.getYear();
    }

    /**
     * int lastyear값 반환
     */
    public int lastYear(OffsetDateTime dateTime) {
        return dateTime.minusYears(1).getYear();
    }

    /**
     * int month값 반환
     */
    public int timeMonth(OffsetDateTime dateTime) {
        return dateTime.getMonthValue();
    }

    /**
     * int day값 반환
     */
    public int timeDay(OffsetDateTime dateTime) {
        return dateTime.getDayOfMonth();
    }

    /**
     * Date값 반환
     */
    public Date timeDate(OffsetDateTime dateTime){
        return Date.from(dateTime.toInstant());
    }
    // TODO : 시간 채크(이번주 시작이 아닌 지난 금요일을 기준으로
    public java.util.List<String> recentFridayCandidates(int weeks) {
        LocalDate todayKst = kstNow().toLocalDate();

        // "이번 주 금요일" 기준(월요일 시작 주)
        LocalDate currentWeekMonday = todayKst.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate currentWeekFriday = currentWeekMonday.plusDays(4);

        // 아직 금요일 전이면 지난주 금요일부터 시작
        if (todayKst.isBefore(currentWeekFriday)) {
            currentWeekFriday = currentWeekFriday.minusWeeks(1);
        }

        java.util.List<String> list = new java.util.ArrayList<>();
        for (int i = 0; i < weeks; i++) {
            list.add(currentWeekFriday.minusWeeks(i).format(YYYYMMDD));
        }
        return list;
    }

    //TODO : 하나로 합치기
    /**
     * 1주전 금요일(월요일 시작 주 기준)을 KST 기준 YYYYMMDD 문자열로 반환
     */
    public String previousWeekFridayYyyyMmDd() {
        LocalDate todayKst = kstNow().toLocalDate();
        LocalDate currentWeekMonday = todayKst.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate previousWeekFriday = currentWeekMonday.minusDays(3); // 월요일 - 3일 = 2주전 금요일
        return previousWeekFriday.format(YYYYMMDD);
    }

    /**
     * 2주전 금요일(월요일 시작 주 기준)을 KST 기준 YYYYMMDD 문자열로 반환
     */
    public String previousTwoWeekFridayYyyyMmDd() {
        LocalDate todayKst = kstNow().toLocalDate();
        LocalDate currentWeekMonday = todayKst.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate previousWeekFriday = currentWeekMonday.minusDays(3); // 월요일 - 3일 = 2주전 금요일
        LocalDate twoWeekAgoFriday = previousWeekFriday.minusWeeks(1);
        return twoWeekAgoFriday.format(YYYYMMDD);
    }

    /**
     * 1달전 금요일(월요일 시작 주 기준)을 KST 기준 YYYYMMDD 문자열로 반환
     */
    public String previousMonthFridayYyyyMmDd() {
        LocalDate todayKst = kstNow().toLocalDate();
        LocalDate currentWeekMonday = todayKst.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate previousWeekFriday = currentWeekMonday.minusDays(31); // 월요일 - 31일 = 4주전 금요일
        return previousWeekFriday.format(YYYYMMDD);
    }

}
