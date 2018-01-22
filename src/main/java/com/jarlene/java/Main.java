package com.jarlene.java;

import com.jarlene.java.struct.Pair;
import com.jarlene.java.utils.ContainerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jarlene.java.utils.ReflectUtil.*;

public class Main {


    public static class ItemStruct implements Comparable<ItemStruct> {
        public long feedId;

        public long cateId;

        public long itemId;

        public String time;

        public double score;

        public static ItemStruct make(long feedId, long cateId, long itemId, String time, double score) {
            ItemStruct item = new ItemStruct();
            item.cateId = cateId;
            item.feedId = feedId;
            item.score = score;
            item.time = time;
            item.itemId = itemId;
            return item;
        }

        @Override
        public String toString() {
            return "feedId=" + feedId + "/cateId=" + cateId + "/itemId=" + itemId + "/time=" + time + "/score=" + score;
        }

        @Override
        public int compareTo(ItemStruct o) {
            if (o.score == this.score) {
                return 0;
            } else if (this.score < o.score) {
                return 1;
            } else {
                return -1;
            }
        }
    }


    public static void main(String[] args) {

        // ReflectUtil demo
        String sub = on("java.lang.String")
                .create("hahah")
                .call("substring", 2)
                .get();
        System.out.println(sub);


        // ContainerUtil demo
        Pair<String, Double> pair = Pair.make("ss", 3.14);
        System.out.println(pair.toString());
        List<ItemStruct> list = new ArrayList<>();
        list.add(ItemStruct.make(123L, 342L, 221334L, "2018-01-22", 0.432));
        list.add(ItemStruct.make(2323L, 3142L, 122134L, "2018-01-22", 0.422));
        list.add(ItemStruct.make(1213L, 3242L, 122134L, "2018-01-22", 0.452));
        list.add(ItemStruct.make(1232L, 342L, 23134L, "2018-01-22", 0.632));

        ContainerUtil.sortList(list, "feedId", true);
//        System.out.println(list);

        Map<Long, List<ItemStruct>> map = ContainerUtil.listToMap(list, "itemId");
//        System.out.println(map);

        System.out.println(ContainerUtil.sortByKey(map, true));

        Map<Long, ItemStruct> subMap = new HashMap<>();
        for (Long key : map.keySet()) {
            subMap.put(key, map.get(key).get(0));
        }
        System.err.println(ContainerUtil.sortByValue(subMap, true));

    }
}
