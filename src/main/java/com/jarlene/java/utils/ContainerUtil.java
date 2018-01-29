package com.jarlene.java.utils;

import java.lang.reflect.Field;
import java.util.*;

public class ContainerUtil {


    /**
     * list 中数据按照某个字段进行排序
     * @param list list数据
     * @param sortField T 数据结构中的字段
     * @param desc 是否降序
     * @param <T> 数据类型
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
                return (o1.getValue()).compareTo(o2.getValue());
            } else {
                return (o2.getValue()).compareTo(o1.getValue());
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


    /**
     * 按照数据的字段进行过滤
     * @param data 原始数据
     * @param filters 过滤字段以及值
     * @param <T> 数据类型
     * @return
     */
    public static <T> List<T> filter(List<T> data, Map<String, Object> filters) {
        if (data == null || data.isEmpty() || filters == null || filters.isEmpty()) {
            return data;
        }
        Map<Field, Object> fields = new HashMap<>();
        for (String fieldName : filters.keySet()) {
            try {
                Field field = data.get(0).getClass().getField(fieldName);
                field.setAccessible(true);
                fields.put(field, filters.get(fieldName));
            } catch (NoSuchFieldException e) {
                continue;
            }
        }

        List<T> result = new ArrayList<>();

        for (T item : data) {
            boolean add = true;
            for (Field field : fields.keySet()) {
                try {
                    if (!field.get(item).toString().equalsIgnoreCase(fields.get(field).toString())) {
                        add = false;
                    }
                    if (!add) {
                        break;
                    }
                } catch (IllegalAccessException e) {
                    continue;
                }
            }
            if (add) {
                result.add(item);
            }
        }
        return result;
    }
}
