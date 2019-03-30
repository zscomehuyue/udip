package com.alibaba.otter.manager.deployer;

import com.alibaba.otter.common.push.index.wide.config.FieldHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Test {


    private static void city() {
        FieldHelper.CITY_CODE_CACHE.entrySet().forEach(entry -> {
            // System.out.println(entry.getKey()+"=="+entry.getValue());
        });
        String url = "jdbc:mysql://192.168.13.9:3325/xxgl";
        System.out.println(FieldHelper.CITY_CODE_CACHE.get(url));
    }

    public static void main(String[] args) {
//        city();
//        System.out.println(OffsetDateTime.now().toEpochSecond());
        String url = "jdbc:mysql://192.168.13.189:3354/huhehaote_xxgl";
        System.out.println(FieldHelper.CITY_CODE_CACHE.get(url));
    }

    private static void findIds() {
        try {

            List<String> list27 = FileUtils.readLines(new File("/worker/testworker/ddd/axon/27.log")).stream().filter(line -> {
                if (StringUtils.isNotEmpty(line) && line.contains("department_id")) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());

            List<String> list23 = FileUtils.readLines(new File("/worker/testworker/ddd/axon/23.log")).stream().filter(line -> {
                if (StringUtils.isNotEmpty(line) && line.contains("department_id")) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());

            list23.forEach(l -> System.out.println("=23=" + l));
            System.out.println("====================================================");
            System.out.println("====================================================");
            System.out.println("====================================================");
            list27.forEach(l -> System.out.println("=27=" + l));

            System.out.println("=23.size=" + list23.size());
            System.out.println("=27.size=" + list27.size());
            List<String> list = list27.stream().filter(line -> list23.contains(line)).collect(Collectors.toList());
            Optional.ofNullable(list).ifPresent(list2 -> {
                list2.forEach(l -> System.out.println("27 包含 23 Line=" + l));
            });


            System.out.println("====================================================");
            System.out.println("====================================================");
            System.out.println("====================================================");
            System.out.println("====================================================");

            List<String> list23Lines = list23.stream().filter(ll -> list27.contains(ll)).collect(Collectors.toList());
            Optional.ofNullable(list23Lines).ifPresent(list2 -> {
                list2.forEach(l -> System.out.println("=23 包含 27 Line=>" + l));
            });


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void findIds2() {
        try {

            List<String> list2334 = FileUtils.readLines(new File("/worker/testworker/ddd/axon/23-34.log")).stream().filter(line -> {
                if (StringUtils.isNotEmpty(line) && line.contains("department_id")) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());

            List<String> list23 = FileUtils.readLines(new File("/worker/testworker/ddd/axon/23.log")).stream().filter(line -> {
                if (StringUtils.isNotEmpty(line) && line.contains("department_id")) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());


            System.out.println("=23.size=" + list23.size());
            System.out.println("=27.size=" + list2334.size());
            list2334.removeAll(list23);

            list2334.forEach(l -> System.out.println("=remain=" + l));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
