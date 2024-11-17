package com.staking.stakingservice.common.util;

import lombok.experimental.UtilityClass;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeUtils {
    public static final ZoneOffset UTC_ZONE = ZoneOffset.UTC;
    private static final String BATCH_ID_FORMAT = "yyyyMMdd";

    public static Instant getCurrentUtc() {
        return Instant.now();
    }

    public static Integer generateBatchId() {
        return Integer.parseInt(
                OffsetDateTime.now(UTC_ZONE)
                        .format(DateTimeFormatter.ofPattern(BATCH_ID_FORMAT)));
    }

    public static Integer generateBatchId(Instant instant) {
        return Integer.parseInt(
                instant.atOffset(UTC_ZONE)
                        .format(DateTimeFormatter.ofPattern(BATCH_ID_FORMAT)));
    }

    public static String formatUtc(Instant instant, String format) {
        return DateTimeFormatter.ofPattern(format)
                .withZone(UTC_ZONE)
                .format(instant);
    }

    public static Instant parseUtc(String dateTimeStr, String format) {
        return DateTimeFormatter.ofPattern(format)
                .withZone(UTC_ZONE)
                .parse(dateTimeStr, Instant::from);
    }

    public static Instant getBatchDate(Integer batchId) {
        return LocalDate.parse(
                String.valueOf(batchId),
                DateTimeFormatter.ofPattern(BATCH_ID_FORMAT))
                .atStartOfDay()
                .toInstant(UTC_ZONE);
    }
}