package com.alibaba.otter.shared.common.utils;

import org.apache.commons.lang.StringUtils;

public class NameFormatUtils {
    public static String formatName(String name) {
        name = StringUtils.isAllUpperCase(name.replace("_", "")) ? name.toLowerCase() : name;
        StringBuilder names = new StringBuilder();
        String[] values = name.split("_");
        names.append(values[0]);
        for (int i = 1; i < values.length; i++) {
            names.append(values[i].substring(0, 1).toUpperCase() + values[i].substring(1));
        }
        return names.toString();
    }

}
