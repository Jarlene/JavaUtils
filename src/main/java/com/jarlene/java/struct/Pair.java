package com.jarlene.java.struct;


import java.io.Serializable;
import java.util.Map;

/**
 * Created by jarlene on 2017/6/26.
 */
public class Pair<K, V> implements Serializable, Comparable<V>{

    public K k;
    public V v;

    public Pair(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public static <K, V>  Pair<K, V>  make(K k, V v) {
        return new Pair<K, V>(k, v);
    }

    public static <K, V> void insertToMap(Map<K, V> map, Pair<K, V> pair) {
        if (map == null) {
            return;
        }
        map.put(pair.k, pair.v);
    }

    @Override
    public int compareTo(V o) {
        String vVal = v.toString();
        String oVal = o.toString();
        return vVal.compareTo(oVal);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair<K, V> pair = (Pair<K, V>) obj;
        return k.equals(pair.k) && v.equals(pair.v);
    }

    @Override
    public int hashCode() {
        return k.hashCode() + v.hashCode();
    }

    @Override
    public String toString() {
        return v.toString();
    }
}
