package web.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: madtracy
 * Time: 2016/5/25.
 */
public class DateUtil {


	//tracy(2016-05-25):cmsData从Mysql直接转换的time格式比较特别
	public static Date fromSql(String val){
		try{
			return DateUtils.parseDate(val,"yyyy-MM-dd HH:mm:ss.S");
		}catch (Exception ex){
			throw new RuntimeException("parse MySql Time exception:"+val);
		}
	}


	public static Date date(String val){
		try{
			return DateUtils.parseDate(val,"yyyy-MM-dd HH:mm:ss");
		}catch (Exception ex){
			throw new RuntimeException("parse Time exception:"+val);
		}
	}
	
	public static String updPageTime(Date updateTime) {
		try {
			String sdf = "MM/dd HH:mm:ss";
	        SimpleDateFormat format = new SimpleDateFormat(sdf);
			return format.format(updateTime);
		} catch (Exception e) {
			return "";
		}
	}
	
	public static String RechargeRecordTime(Date updateTime) {
		try {
			String sdf1 = "yyyy-MM-dd";
			String sdf2 = "今天 HH:mm";
			
			Calendar today = Calendar.getInstance();	//今天
			Calendar update = Calendar.getInstance();
			today.set(Calendar.YEAR, today.get(Calendar.YEAR));
			today.set(Calendar.MONTH, today.get(Calendar.MONTH));
			today.set(Calendar.DAY_OF_MONTH,today.get(Calendar.DAY_OF_MONTH));
			//  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
			today.set( Calendar.HOUR_OF_DAY, 0);
			today.set( Calendar.MINUTE, 0);
			today.set(Calendar.SECOND, 0);
			
			update.setTime(updateTime);
			SimpleDateFormat format = new SimpleDateFormat();
			if(update.after(today)){
				format = new SimpleDateFormat(sdf2);
			} else {
				format = new SimpleDateFormat(sdf1);
			}
			return format.format(updateTime);
		} catch (Exception e) {
			return "";
		}
	}
}
