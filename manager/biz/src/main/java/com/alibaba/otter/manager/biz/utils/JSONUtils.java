package com.alibaba.otter.manager.biz.utils;

import java.util.Map;
import java.util.Map.Entry;

/**
 * JSON格式化工具
 * @author tangdelong
 * 2016年11月3日
 */
public class JSONUtils {
	
	
    /**
     * JSON格式化输出
     * @param jsonStr json串
     * 
     * @return 格式化结果
     * @author tangdelong
     * 2016年11月3日
     */
    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) return "";
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '{':
                case '[':
                    sb.append(current);
                    sb.append('\n');
                    indent++;
                    addIndentBlank(sb, indent);
                    break;
                case '}':
                case ']':
                    sb.append('\n');
                    indent--;
                    addIndentBlank(sb, indent);
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\') {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString();
    }
    
    /**
     * 添加space
     * @author tangdelong
     * 2016年11月3日
     */
    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }
    
    
    /**
     * JSON格式拼装，对传入的参数进行循环的处理，如需顺序需传入有序Map
     * @author tangdelong
     * 2017年6月30日
     */
    public static String jsonWrapper(Map<String, String> map){
    	if(map == null){
    		return null;
    	}
    	int i = 0;
    	StringBuilder sb = new StringBuilder();
    	sb.append("{");
    	for(Entry<String, String> m : map.entrySet()){
    		
    		if(i > 0){
    			sb.append(",\"");
    		}else{
    			sb.append("\"");
    		}
    		sb.append(m.getKey());
    		sb.append("\":\"");
    		sb.append(m.getValue());
			sb.append("\"");
    		
    		i++;
    	}
    	sb.append("}");
    	
    	
    	return sb.toString();
    }
    
    
    
    

}
