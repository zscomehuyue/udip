package com.alibaba.otter.node.etl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.collections.Lists;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatTest {
    public static void main(String[] args) {
        String filePath = "/Data/logs/udip/bizlogs/all.log";
        List<String> filter = Lists.newArrayList();
        filter.add("=processStat=>");
        filter.add("after");
        filter.add("2018-09-23 08:31");
        List<String> filter2 = Lists.newArrayList();
        filter2.add("=processStat=>");
        filter2.add("after");
        filter2.add("2018-09-23 08:32");

        LocalDateTime start = LocalDateTime.parse("2018-09-23 08:31:31", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end = LocalDateTime.parse("2018-09-23 08:32:03", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        long spendTime =0;
        try {
            List<Object> lines = Lists.newArrayList();
            List<String> list = FileUtils.readLines(new File(filePath));
            List<Long> times = Lists.newArrayList();
            list.forEach((line) -> {
                if (StringUtils.isNotEmpty(line)
                        && (filter.stream().allMatch((ft) -> line.contains(ft))
                        || filter2.stream().allMatch((ft) -> line.contains(ft)))) {
//                    System.out.println(line);
                    String[] split = line.split(" ");
                    LocalDateTime parse = LocalDateTime.parse(split[0] + " " + split[1], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                    if (parse.isBefore(end) && parse.isAfter(start)) {
                        lines.add(line);
                        String spend = line.split(",")[6].replace(" milliseconds","").replace("spend:","");
                        times.add(Long.valueOf(spend.trim()));
                        System.out.println("spend="+spend);


                    }

                }
            });
            //lines.forEach(System.out::println);
            System.out.println(lines.size());

            for (int i = 0; i <times.size() ; i++) {
                spendTime+=(times.get(i)/1000);
            }
            System.out.println("avg-spendTime="+ (spendTime));
            System.out.println("avg="+ (spendTime/times.size()));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
