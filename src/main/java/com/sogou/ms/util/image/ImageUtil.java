package com.sogou.ms.util.image;

import com.sogou.ms.util._;

/**
 * User: madtracy
 * Time: 2016/7/19.
 */
public class ImageUtil {

	private static final String piratedCoverFormat = "http://img0%d.sogoucdn.com/app/a/59/%s.jpg";
	private static final String httpsPiratedCoverFormat = "https://img0%d.sogoucdn.com/app/a/59/%s.jpg";

	private static final String thumbnailFormat = "http://img0%d.sogoucdn.com/v2/thumb/resize%s?appid=%s&name=%s";
	private static final String httpsThumbnailFormat = "https://img0%d.sogoucdn.com/v2/thumb/resize%s?appid=%s&name=%s";

	/**
	 * 根据盗版书id返回盗版书封面
	 * @param id 盗版书的id
	 * @param https 是否是https
	 * @return
	 */
	public static String piratedCover(String id,boolean https){
		if(_.isEmpty(id))
			throw new RuntimeException("ThumbnailUtil.piratedCover empty id");
		int val =(Math.abs(id.hashCode())%4)+1;
		String format = https?httpsPiratedCoverFormat:piratedCoverFormat;
		return _.f(format,val,id);
	}

	/**
	 * 根据盗版id及缩略尺寸生成缩略图
	 * @param id 盗版书的id
	 * @param size 缩略尺寸，格式为 "/w/138/h/190"
	 * @param https 是否是https
	 * @return
	 */
	public static String thumbnailById(String id,ThumbnailSizeImpl size,boolean https){
		if(_.isEmpty(id))
			throw new RuntimeException("ThumbnailUtil.thumbnailById empty id");
		int val =(Math.abs(id.hashCode())%4)+1;
		String appid = "59";//盗版书缩略图的id
		String name = id+".jpg";//盗版书的图片名格式
		String format = https?httpsThumbnailFormat:thumbnailFormat;
		return _.f(format,val,size.size(),appid,name);
	}



	/**
	 * 对于缩略图服务的图片进行缩略，缩略图服务格式为: http://img0{1-4}.sogoucdn.com/app/a/$appid/$imageName,例如：
	 * http://img03.sogoucdn.com/app/a/10070003/D0BB26C9C061F7F5FC8B90911646753D,http://img01.sogoucdn.com/app/a/59/5377805652963819149.jpg
	 * 主要是将appid跟imageName提取出来，然后就可以缩略了
	 *
	 * @param image 缩略图服务的图片
	 * @param size 缩略尺寸，格式为 "/w/138/h/190"
	 * @param https 是否是https
	 * @return
	 */
	public static String thumbnail(String image,ThumbnailSizeImpl size,boolean https){
		if(_.isEmpty(image)){
			throw new RuntimeException("ThumbnailUtil.thumbnail empty image");
		}
		String appid="",name="";
		String[] str = image.split("/");
		int length = str.length;
		if(length>1){
			appid = str[length-2];
			name = str[length-1];
		}
		int val = (Math.abs(_.trimToEmpty(name).hashCode())%4)+1;
		String format = https?httpsThumbnailFormat:thumbnailFormat;
		return _.f(format,val,size.size(),appid,name);
	}

	public static String thumbnail(String image,String defailtImage,ThumbnailSizeImpl size,boolean https){
		String url = thumbnail(image,size,https);
		return _.f("%s&r=%s",url,_.urlenc(defailtImage));
	}


}
