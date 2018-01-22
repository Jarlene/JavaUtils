package com.jarlene.java.utils;

import java.lang.reflect.Field;
import java.util.*;

public class ContainerUtil {


    /**
     * list 中数据按照某个字段进行排序
     * @param list list数据
     * @param sortField T 数据结构中的字段
     * @param desc 是否降序
     * @param <T>
     */
    public static <T>  void sortList(List<T> list, String sortField, final boolean desc) {
        if (list == null || list.isEmpty()) {
            return;
        }
        try {
            final Field field = list.get(0).getClass().getField(sortField);
            field.setAccessible(true);
            list.sort((o1, o2) -> {
                int retVal = 0;
                try {
                    String one = field.get(o1).toString();
                    String two = field.get(o2).toString();
                    if (desc) {
                        retVal = two.compareTo(one);
                    } else {
                        retVal = one.compareTo(two);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return retVal;
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    /**
     * Map 按照key进行排序
     * @param map
     * @param desc
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map, final boolean desc) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        list.sort((o1, o2) -> {
            if (desc) {
                return (o2.getKey()).compareTo(o1.getKey());
            } else {
                return (o1.getKey()).compareTo(o2.getKey());
            }
        });
        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Map 按照value进行排序
     * @param map
     * @param desc
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, final boolean desc) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        list.sort((o1, o2) -> {
            if (desc) {
                return (o2.getValue()).compareTo(o1.getValue());
            } else {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }


    /**
     * 将list中数据按照某个字段进行归纳为Map
     * @param orig
     * @param fieldStr
     * @param <T>
     * @param <V>
     * @return
     */
    public static <T, V> Map<T, List<V>> listToMap(List<V> orig, String fieldStr) {
        Map<T, List<V>> result = new HashMap<>();
        if (orig == null || orig.isEmpty() ) {
            return result;
        }
        try {
            final Field field = orig.get(0).getClass().getField(fieldStr);
            field.setAccessible(true);
            for (V it : orig) {
                T t = (T) field.get(it);
                if (result.containsKey(t)) {
                    result.get(t).add(it);
                } else {
                    List<V> list = new ArrayList<>();
                    list.add(it);
                    result.put(t, list);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return result;

    }


    /**
     * 乱序
     * @param list
     * @param <T>
     */
    public static<T> void shuffle(List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Collections.shuffle(list);
    }
}
