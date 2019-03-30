package com.alibaba.otter.common.push.index.wide.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WideHelper {

    public static final Map<String, List<String>> CURRICULUM_COMPENSATE = new ConcurrentHashMap() {{
        put("curriculum", new ArrayList<String>() {{
            add("clazz_id");
            add("classtimeType_id");
            add("classtime_id");
//            add("curriculum_classId");
//            add("curriculum_classtimeTypeId");
//            add("curriculum_classtimeId");
        }});
        put("clazz", new ArrayList<String>() {{
            add("classlevel_id");
            add("department_id");
//            add("clazz_levelId");
//            add("clazz_servicecenterId");
        }});

    }};


    public static final Map<String, List<String>> CURRICULUM_CONDTION_COMPENSATE = new ConcurrentHashMap() {{
        put("curriculum", new ArrayList<String>() {{
            add("classRegistCount_classId");
        }});
    }};

    public static class TableColumn {
        private boolean isPrimaryKey;
        private String sourceName;
        private String targetName;

        public TableColumn(boolean isPrimaryKey, String sourceName, String targetName) {
            this.isPrimaryKey = isPrimaryKey;
            this.sourceName = sourceName;
            this.targetName = targetName;
        }

        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }

        public boolean isNotPrimaryKey() {
            return !isPrimaryKey;
        }

        public void setPrimaryKey(boolean primaryKey) {
            isPrimaryKey = primaryKey;
        }

        public String getSourceName() {
            return sourceName;
        }

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

    }

    public static final Map<String, List<TableColumn>> CURRICULUM_CONDTION_PKID_COMPENSATE = new ConcurrentHashMap() {{
        put("classRegistCount", new ArrayList<TableColumn>() {{
            add(new TableColumn(true, "crc_id", "id"));
            add(new TableColumn(false, "crc_class_id", "classId"));
        }});
    }};


}
