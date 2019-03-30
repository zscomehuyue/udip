package com.alibaba.otter.common.push.index.es;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.InitializingBean;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public class MappingServiceImpl implements IMappingService, InitializingBean {
    private static final String DEFAULT_DATE_TIME = "0000-00-00 00:00:00";
    private static final String TIME = "HH:mm:ss";
    private static final String DATE = "yyyy-MM-dd";
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final DateTimeFormatter FORMATTER_TIME = DateTimeFormatter.ofPattern(TIME);
    private static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern(DATE);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter MILLI_SECOND_FORMATER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);
    private Map<Integer, Converter> mysqlType2EsMapping;

    public Object getEsObject(int mysqlType, String data) {
        Optional<Map.Entry<Integer, Converter>> result = mysqlType2EsMapping.entrySet().parallelStream()
                .filter(entry -> entry.getKey() == mysqlType).findFirst();
        return (result.isPresent() ? result.get().getValue() : (Converter) data1 -> data1).convert(data);
    }

    public void afterPropertiesSet() throws Exception {
        mysqlType2EsMapping = Maps.newHashMap();
        mysqlType2EsMapping.put(Types.CHAR, data -> data);
        mysqlType2EsMapping.put(Types.VARCHAR, data -> data);
        mysqlType2EsMapping.put(Types.BLOB, data -> data);
        mysqlType2EsMapping.put(Types.INTEGER, Long::valueOf);//FIXME to int
        mysqlType2EsMapping.put(Types.BIGINT, Long::valueOf);//FIXME add it ,old is string ;test index is ok ;
        mysqlType2EsMapping.put(Types.DATE, data -> parse(data));
        mysqlType2EsMapping.put(Types.TIMESTAMP, data -> parse(data));
        mysqlType2EsMapping.put(Types.FLOAT, Double::valueOf);
        mysqlType2EsMapping.put(Types.DOUBLE, Double::valueOf);
        mysqlType2EsMapping.put(Types.DECIMAL, Double::valueOf);
    }

    private Object parse(String date) {
        if (date.trim().length() == DATE_PATTERN.length()) {
            if (DEFAULT_DATE_TIME.equals(date.trim())) {
                date="1970-01-01 00:00:00";
            }
            return LocalDateTime.parse(date, FORMATTER);
        } else if (date.trim().length() == TIMESTAMP_PATTERN.length()) {
            return LocalDateTime.parse(date, MILLI_SECOND_FORMATER);
        } else if (date.trim().length() == DATE.length()) {
            return LocalDate.parse(date, FORMATTER_DATE);
        } else if (date.trim().length() == TIME.length()) {
            return LocalTime.parse(date, FORMATTER_TIME);
        }
        return null;
    }

    @FunctionalInterface
    private interface Converter {
        Object convert(String data);
    }

    public static void main(String[] args) {
        MappingServiceImpl service = new MappingServiceImpl();
        try {
            service.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object esObject = service.getEsObject(Types.BIGINT, "2");
        System.out.println(esObject.getClass().getSimpleName());
//
//        System.out.println(TIME.length());
//        System.out.println(DATE.length());
//        String s = "1970-00-00 00:00:00";
//        LocalDateTime.parse(s, FORMATTER);
//        System.out.println(LocalDateTime.MIN);
    }
}
