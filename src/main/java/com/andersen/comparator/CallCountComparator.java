package com.andersen.comparator;

import java.util.Comparator;
import java.util.Map;

/**
 * используется для сортировки объектов по количеству вызовов в кеше, а также устранения ситуации, когда вызовы равны
 */
public class CallCountComparator implements Comparator {

    Map base;

    public CallCountComparator(Map base) {
        this.base = base;
    }

    @Override
    public int compare(Object o1, Object o2) {
        if ((Integer) base.get(o1) < (Integer) base.get(o2)){
            return 1;
        } else if ((Integer) base.get(o1) < (Integer) base.get(o2)){
            return 1;
        } else {
            return -1;
        }
    }
}
