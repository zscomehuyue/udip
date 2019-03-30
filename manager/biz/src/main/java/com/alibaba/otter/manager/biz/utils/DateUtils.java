package com.alibaba.otter.manager.biz.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * 时间处理工具类
 *
 * @author tangdelong 2015年7月7日
 */
public class DateUtils {

	/**
	 * 获取当前时间日期
	 * 
	 * @param pattern
	 *            日期格式 如:yyyy-MM-dd HH:mm:ss
	 * @return 日期字符串
	 * 
	 * @author 唐德龙 2014年12月23日
	 */
	public static String getCurrentDate(String pattern) {
		return getDateStr(new Date(), pattern);
	}

	/**
	 * 获取当前时间
	 * 
	 * @return Timestamp
	 * 
	 * @author 唐德龙 2014年12月23日
	 */
	public static Timestamp getCurrentDate() {
		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * 根据date和时间格式，返回时间字符串
	 * 
	 * @param date
	 *            Date类型日期
	 * @param pattern
	 *            日期格式 如:yyyy-MM-dd HH:mm:ss
	 * @return 返回时间字符串
	 *
	 * @author 唐德龙 2014年12月23日
	 */
	public static String getDateStr(Date date, String pattern) {
		if(date == null){
			return null;
		}
		String str = "";
		SimpleDateFormat sdf = null;
		if (date != null && pattern != null) {
			String temp = pattern;
			try {
				sdf = new SimpleDateFormat(temp);
			} catch (IllegalArgumentException e) {
				temp = "yyyy-MM-dd HH:mm:ss";
				sdf = new SimpleDateFormat(temp);
			}
			str = sdf.format(date);
		}
		return str;
	}

	/**
	 * 从给定的字符串开始解析文本，以生成日期。
	 * 
	 * @param date 日期字符串
	 * @param OriginPattern 源日期格式，如:yyyy-MM-dd HH:mm:ss
	 * @param targetPattern 要转换的日期格式，如:yyyy-MM-dd HH:mm:ss
	 * 
	 * @return 返回时间字符串
	 * 
	 * @author tangdelong 2015年10月9日
	 */
	public static String getDateStr(String dateStr, String OriginPattern,String targetPattern) {
		String result = "";
		if (StringUtils.isNotBlank(dateStr)) {
			Date date = DateUtils.getDateByStr(dateStr,OriginPattern);
			result = getDateStr(date, targetPattern);
		}
		return result;
	}

	/**
	 * 根据date获取该日期的周一和周日日期
	 * 
	 * @return 日期字符串
	 *
	 * @author 唐德龙 2014年12月23日
	 */
	public static String[] getWeek(Date date) {
		String monday = "";
		String sunday = "";
		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int day_of_week = cal.get(Calendar.DAY_OF_WEEK) - 2;
			cal.add(Calendar.DATE, -day_of_week);
			monday = getDateStr(cal.getTime(), "yyyy-MM-dd");
			cal.add(Calendar.DATE, 6);
			sunday = getDateStr(cal.getTime(), "yyyy-MM-dd");
		}
		return new String[] { monday, sunday };
	}

	/**
	 * 根据date获取该日期的1号和月末日期
	 * 
	 * @return 日期字符串
	 *
	 * @author 唐德龙 2014年12月23日
	 */
	public static String[] getMonth(Date date) {
		String start = "";
		String end = "";
		if (date != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.set(Calendar.DAY_OF_MONTH,
					calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
			start = getDateStr(calendar.getTime(), "yyyy-MM-dd");
			calendar.set(Calendar.DAY_OF_MONTH,
					calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			end = getDateStr(calendar.getTime(), "yyyy-MM-dd");
		}
		return new String[] { start, end };
	}

	/**
	 * 根据日期转换timestamp ，格式为yyyy-MM-dd
	 * 
	 * @param data
	 *            日期字符串
	 * @return Timestamp
	 *
	 * @author 唐德龙 2015年1月7日
	 */
	public static Timestamp getTimestamp(String data) {
		return getTimestamp(data, "yyyy-MM-dd");
	}

	/**
	 * 根据日期转换timestamp
	 * 
	 * @param data
	 *            日期字符串
	 * @param pattern
	 *            日期格式：如yyyy-MM-dd
	 * @return Timestamp
	 *
	 * @author 唐德龙 2015年1月7日
	 */
	public static Timestamp getTimestamp(String data, String pattern) {
		if (StringUtils.isBlank(data)) {
			return null;
		}
		Date date = getDateByStr(data, pattern);
		Timestamp ts = new Timestamp(date.getTime());
		return ts;
	}

	/**
	 * 根据日期转换Timestamp
	 * 
	 * @param date
	 *            日期
	 * @param pattern
	 *            日期格式：如yyyy-MM-dd
	 * @return Timestamp
	 * 
	 * @author tangdelong 2015年6月16日
	 */
	public static Timestamp getTimestamp(Date date) {
		Timestamp ts = new Timestamp(date.getTime());
		return ts;
	}
	
	
	/**
	 * 获取源日期加减指定天数后的日期字符串
	 * 
	 * @param srcDate
	 *            源日期
	 * @param day
	 *            加减多少天(如-1、2)
	 * @param pattern
	 *            日期格式 yyyy-MM-dd HH:mm:ss
	 *
	 * @author lyl 2017年8月22日
	 */
	public static Timestamp getTimestampByNo(String srcDate, int day, String pattern) {
		Date date = getDateByStr(srcDate, pattern);
		Calendar cd = Calendar.getInstance();
		cd.setTime(date);
		cd.add(Calendar.DAY_OF_MONTH, day);
		return getTimestamp(cd.getTime());
	}
	
	
	/**
	 * 根据指定的字符串获取Date时间
	 * 
	 * @param data
	 *            如：2011-10-10
	 * @param pattern
	 *            日期格式 如：yyyy-MM-dd
	 * 
	 * @author 唐德龙 2015年1月9日
	 */
	public static Date getDateByStr(String data, String pattern) {
		if (StringUtils.isBlank(data)) {
			return null;
		}
		Date date = new Date();
		// 注意format的格式要与日期String的格式相匹配
		DateFormat sdf = new SimpleDateFormat(pattern);
		try {
			date = sdf.parse(data);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 获取源日期加指定天数后的日期字符串
	 * 
	 * @param srcDate
	 *            源日期
	 * @param day
	 *            加多少天
	 * @param pattern
	 *            日期格式 yyyy-MM-dd
	 *
	 * @author 唐德龙 2015年1月9日
	 */
	public static String getDateByAdd(String srcDate, int day, String pattern) {
		Date date = getDateByStr(srcDate, "yyyy-MM-dd");
		Calendar cd = Calendar.getInstance();
		cd.setTime(date);
		cd.add(Calendar.DAY_OF_MONTH, day);
		String str = getDateStr(cd.getTime(), "yyyy-MM-dd");
		return str;
	}


	/**
	 * 获取源日期加指定秒数后的日期字符串
	 * @param date
	 * @param second
	 * @param pattern
	 * @return
	 */
	public static String getDateAddSecond(Date date, int second, String pattern) {
		Calendar cd = Calendar.getInstance();
		cd.setTime(date);
		cd.add(Calendar.SECOND, second);
		String str = getDateStr(cd.getTime(), "yyyy-MM-dd");
		return str;
	}



	/**
	 * 获取当前时间加指定秒数后的时间
	 *
	 * @param second
	 * @param pattern
	 * @return
	 */
	public static String getCurrentDateAddSecond(int second, String pattern) {
		Date date = new Date();
		Calendar cd = Calendar.getInstance();
		cd.setTime(date);
		cd.add(Calendar.SECOND, second);
		String str = getDateStr(cd.getTime(), pattern);
		return str;
	}

	/**
	 * 获取源日期加指定天数后的日期字符串
	 * 
	 * @param srcDate
	 *            源日期
	 * @param day
	 *            加多少天
	 * @param pattern
	 *            日期格式 yyyy-MM-dd
	 *
	 * @author 唐德龙 2015年1月9日
	 */
	public static String getDateByAdd(Date srcDate, int day, String pattern) {
		Calendar cd = Calendar.getInstance();
		cd.setTime(srcDate);
		cd.add(Calendar.DAY_OF_MONTH, day);
		String str = getDateStr(cd.getTime(), "yyyy-MM-dd");
		return str;
	}

	/**
	 * 根据开始时间和结束时间返回时间段内的时间集合
	 * 
	 * @param beginDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @return List,没有返回NULL。
	 * 
	 * @author tangdelong 2015年11月4日
	 */
	public static List<Date> getDatesBetweenTwoDate(Date beginDate, Date endDate) {
		if (beginDate == null || endDate == null) {
			return null;
		}
		List<Date> listDate = new ArrayList<Date>();
		listDate.add(beginDate);// 把开始时间加入集合
		Calendar cal = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		cal.setTime(beginDate);
		boolean bContinue = true;
		while (bContinue) {
			// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
			cal.add(Calendar.DAY_OF_MONTH, 1);
			// 测试此日期是否在指定日期之后
			if (endDate.after(cal.getTime())) {
				listDate.add(cal.getTime());
			} else {
				break;
			}
		}
		listDate.add(endDate);// 把结束时间加入集合
		return listDate;
	}

	/**
	 * 根据开始时间和结束时间返回时间段内的时间集合
	 * 
	 * @param beginDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @return List 数据结构['2015-01-02','2015-01-03','2015-01-04'],没有返回NULL。
	 * 
	 * @author tangdelong 2015年11月4日
	 */
	public static List<String> getDatesBetweenTwoDateToStr(Date beginDate,
			Date endDate) {
		if (beginDate == null || endDate == null) {
			return null;
		}
		List<String> listDate = new ArrayList<String>();
		listDate.add(DateUtils.getDateStr(beginDate, "yyyy-MM-dd"));// 把开始时间加入集合
		Calendar cal = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		cal.setTime(beginDate);
		boolean bContinue = true;
		while (bContinue) {
			// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
			cal.add(Calendar.DAY_OF_MONTH, 1);
			// 测试此日期是否在指定日期之后
			if (endDate.after(cal.getTime())) {
				listDate.add(DateUtils.getDateStr(cal.getTime(), "yyyy-MM-dd"));
			} else {
				break;
			}
		}
		listDate.add(DateUtils.getDateStr(endDate, "yyyy-MM-dd"));// 把结束时间加入集合
		return listDate;
	}

	/**
	 * 根据date日期，获取该日期当前月的1号日期
	 * 
	 * @return 日期字符串
	 *
	 * @author 唐德龙 2014年12月23日
	 */
	public static String getCurrentMonthBeginDate(Date date) {
		String start = "";
		if (date != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.set(Calendar.DAY_OF_MONTH,
					calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
			start = getDateStr(calendar.getTime(), "yyyy-MM-dd");
		}
		return start;
	}
	
	
	

	/**
	 * 计算两个时间时间差
	 * 
	 * @author jiangying
	 * @date 2017年3月14日 下午2:42:44
	 * @param startTime
	 * @param endTime
	 * @param key
	 * @return
	 */
	public static long getDateDiff(String startTime, String endTime, String key) {
		// 按照传入的格式生成一个simpledateformate对象
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
		long nh = 1000 * 60 * 60;// 一小时的毫秒数
		long nm = 1000 * 60;// 一分钟的毫秒数
		long ns = 1000;// 一秒钟的毫秒数
		long diff = 0;
		try {
			// 获得两个时间的毫秒时间差异
			diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();
		} catch (Exception e) {
			// TODO: handle exception
		}
		if ("day".equals(key)) {
			return diff / nd;// 计算差多少天
		} else if ("hour".equals(key)) {
			return diff % nd / nh;// 计算差多少小时
		} else if ("min".equals(key)) {
			return diff % nd % nh / nm;// 计算差多少分钟
		} else if ("sec".equals(key)) {
			return diff % nd % nh % nm / ns;// 计算差多少秒//输出结果
		} else {
			return 0;
		}
	}

	public static long getDateDiff(Date startTime, Date endTime, String key) {
		// 按照传入的格式生成一个simpledateformate对象
		long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
		long nh = 1000 * 60 * 60;// 一小时的毫秒数
		long nm = 1000 * 60;// 一分钟的毫秒数
		long ns = 1000;// 一秒钟的毫秒数
		// 获得两个时间的毫秒时间差异
		long diff = endTime.getTime() - startTime.getTime();
		if ("day".equals(key)) {
			return diff / nd;// 计算差多少天
		} else if ("hour".equals(key)) {
			return diff % nd / nh;// 计算差多少小时
		} else if ("min".equals(key)) {
			return diff % nd % nh / nm;// 计算差多少分钟
		} else if ("sec".equals(key)) {
			return diff % nd % nh % nm / ns;// 计算差多少秒//输出结果
		} else {
			return 0;
		}
	}
	
	
	
	
	/**
     * 计算两个日期间隔多少天,不包括结束时间当天时间。
     * @param beginDate 开始时间
     * @param endDate 结束时间
     * 
     * @return 间隔天数
     * @author tangdelong
     * 2017年5月17日
     */
    public static int getDateIntervalDay(Date beginDate,Date endDate){
    	if(beginDate == null || endDate == null){
    		return 0; 
    	}
    	int days = (int)((endDate.getTime() - beginDate.getTime())/86400000);
    	
    	return days;
    }
	
	
	
}
