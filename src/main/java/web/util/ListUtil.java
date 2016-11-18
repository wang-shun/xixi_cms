package web.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: madtracy
 * Time: 2016/5/25.
 */
public class ListUtil {

	public static <E> List<E> page(List<E> list, int pageNum, int length){
		if(list == null || list.isEmpty())
			return null;
		pageNum = pageNum<1?1:pageNum;
		int start = (pageNum-1)*length,
				end = pageNum*length,
				size = list.size();
		if(start>=size)
			return null;
		end = end<size?end:size;
		return list.subList(start,end);
	}

	public static <E,T> List<E> getList(T key, Map<T,List<E>> map){
		List<E> list = map.get(key);
		if(list == null){
			list = new ArrayList<>();
			map.put(key,list);
		}
		return list;
	}
}
