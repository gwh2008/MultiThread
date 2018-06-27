package com.gaowh.concurrent;

import java.util.Map;

 
/**
 * 黑名单服务
 *
 * @author 高文辉
 *
 */
public class BlackListServiceImpl {
 
    private static CopyOnWriteMap<String, Boolean> blackListMap = new CopyOnWriteMap<String, Boolean>();
 
    public static boolean isBlackList(String id) {
        return blackListMap.get(id) == null ? false : true;
    }
 
    public static void addBlackList(String id) {
        blackListMap.put(id, Boolean.TRUE);
    }
 
    /**
     * 批量添加黑名单
     *
     * @param ids
     */
    public static void addBlackList(Map<String,Boolean> ids) {
        blackListMap.putAll(ids);
    }
 
}