package com.tourcoo.aircraft.ui.photo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2021年04月15日15:48
 * @Email: 971613168@qq.com
 */
public class ListSort {

        /**
         * 按照List<Map<String,Object>>里面map的某个value重新封装成多个不同的list, 原始数据类型List<Map
         * <String,Object>>, 转换后数据类型Map<String,List<Map<String,Object>>>
         *
         * @param list
         * @param oneMapKey
         * @return
         */
        private static Map<String, Object> change(List<Map<String, Object>> list, String oneMapKey) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            Set<Object> setTmp = new HashSet<Object>();
            for (Map<String, Object> tmp : list) {
                setTmp.add(tmp.get(oneMapKey));
            }
            Iterator<Object> it = setTmp.iterator();
            while (it.hasNext()) {
                String oneSetTmpStr = (String) it.next();
                List<Map<String, Object>> oneSetTmpList = new ArrayList<Map<String, Object>>();
                for (Map<String, Object> tmp : list) {
                    String oneMapValueStr = (String) tmp.get(oneMapKey);
                    if (oneMapValueStr.equals(oneSetTmpStr)) {
                        oneSetTmpList.add(tmp);
                    }
                }
                resultMap.put(oneSetTmpStr, oneSetTmpList);
            }
            return resultMap;
        }

        /**
         * 按照List<Map<String,Object>>里面map的某个value重新封装成多个不同的list, 原始数据类型List<Map
         * <String,Object>>, 转换后数据类型Map<String,List<Map<String,Object>>>
         *
         * @param oneMapKey
         * @return
         */
        private static List<Map<String, Object>> change2(List<Map<String, Object>> inList, String oneMapKey,
                                                         List<Map<String, Object>> outList) {
            // 1.将某个key的值存在set中
            Set<Object> setTmp = new HashSet<Object>();
            for (Map<String, Object> tmp : inList) {
                setTmp.add(tmp.get(oneMapKey));
            }
            // 2.遍历set
            Iterator<Object> it = setTmp.iterator();
            while (it.hasNext()) {
                String oneMapValueStr = "";
                String oneSetTmpStr = (String) it.next();
                Map<String, Object> oneSetTmpMap = new HashMap<String, Object>();
                List<Map<String, Object>> oneSetTmpList = new ArrayList<Map<String, Object>>();

                for (Map<String, Object> tmp : inList) {
                    oneMapValueStr = (String) tmp.get(oneMapKey);
                    if (oneSetTmpStr.equals(oneMapValueStr)) {
                        oneSetTmpMap.put("text", oneSetTmpStr);
                        oneSetTmpList.add(tmp);
                    }
                }
                oneSetTmpMap.put("array", oneSetTmpList);
                outList.add(oneSetTmpMap);
            }
            return outList;
    }
}
