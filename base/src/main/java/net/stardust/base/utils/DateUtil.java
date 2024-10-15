package net.stardust.base.utils;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

public final class DateUtil {

    private DateUtil() {
    }

    public static String formatToRFC1123DateTime(TemporalAccessor temporalAccessor) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(Objects.requireNonNull(temporalAccessor, "temporalAccessor"));
    }

}
