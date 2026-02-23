package com.projectsugarglider.kca.db;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * KCA(소비자원) 상품 가격정보 DB 파티셔닝 매니저.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceInfoPartitionManager {

    private final JdbcTemplate jdbcTemplate;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter BASIC = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 최근 몇 주까지 파티션을 유지/생성할지
     * 예: 8이면 최근 금요일 포함 과거 8주치
     */
    private static final int KEEP_WEEKS = 8;

    /**
     * 서버가 부팅될때 자동으로 파티션의 생성/드랍을 하는 로직입니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createWeeklyPartitions() {
        createPartitions();
        dropPastPartitions();
    }

    /**
     * 최근 금요일 기준으로 과거 KEEP_WEEKS 주차 파티션을 생성합니다.
     * (예: 최근 금요일, 1주전 금요일, 2주전 금요일 ... 7주전 금요일)
     */
    private void createPartitions() {
        LocalDate latestFriday = latestFriday(LocalDate.now(KST));

        for (int i = 0; i < KEEP_WEEKS; i++) {
            LocalDate target = latestFriday.minusWeeks(i);
            String ymd = target.format(BASIC);

            String sql = """
                CREATE TABLE IF NOT EXISTS kca_price_info_%s
                PARTITION OF kca_price_info
                FOR VALUES IN ('%s')
                """.formatted(ymd, ymd);

            jdbcTemplate.execute(sql);
            log.info("▲ 파티션 생성/확인: kca_price_info_{}", ymd);
        }
    }

    /**
     * 오래된 파티션을 드랍하는 로직입니다.
     * 최근 금요일 기준으로 KEEP_WEEKS 보다 오래된 파티션을 드랍합니다.
     * 파티션명 : "kca_price_info_yyyyMMdd", 기준 : TZ=Asia/Seoul.
     *
     * 만약 추후 데이터의 보관이 필요하다면
     * 다른 DB로 옮겨서 저장하는 등의 추가적인 조치가 필요합니다.
     */
    private void dropPastPartitions() {
        LocalDate latestFriday = latestFriday(LocalDate.now(KST));
        LocalDate cutoff = latestFriday.minusWeeks(KEEP_WEEKS - 1); // 이것보다 이전이면 삭제

        String sql = """
            SELECT c.relname
            FROM pg_class c
            JOIN pg_inherits i ON i.inhrelid = c.oid
            JOIN pg_class p ON p.oid = i.inhparent
            WHERE p.relname = 'kca_price_info'
        """;

        List<String> partitions = jdbcTemplate.queryForList(sql, String.class);

        for (String relname : partitions) {
            String prefix = "kca_price_info_";

            // 혹시 이름이 다르거나 default 파티션 같은 예외가 있으면 스킵
            if (relname == null || !relname.startsWith(prefix)) {
                log.info("파티션 스킵(이름 불일치): {}", relname);
                continue;
            }

            String yyyymmdd = relname.substring(prefix.length());

            // yyyyMMdd 형식 아닌 경우 스킵 (예: default 파티션 등)
            if (!yyyymmdd.matches("\\d{8}")) {
                log.info("파티션 스킵(날짜형식 아님): {}", relname);
                continue;
            }

            LocalDate partDate = LocalDate.parse(yyyymmdd, BASIC);

            if (partDate.isBefore(cutoff)) {
                String drop = "DROP TABLE IF EXISTS " + relname;
                log.info("▼ 오래된 파티션 드랍: {} (날짜:{})", relname, partDate);
                jdbcTemplate.execute(drop);
            }
        }
    }

    /**
     * 기준일(today) 기준 "가장 최근 금요일" 반환
     * - 금요일이면 오늘
     * - 월/화/...면 지난 금요일
     */
    private LocalDate latestFriday(LocalDate today) {
        int diff = (today.getDayOfWeek().getValue() - DayOfWeek.FRIDAY.getValue() + 7) % 7;
        return today.minusDays(diff);
    }
}