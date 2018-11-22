package com.andersen.interfaces;

import java.util.Set;

/**
 * Интерфейс используется алгоритмами вытеснения, для подсчета
 * количества вызовов объекта в кеше*/
public interface CallObjectCounts<Key> {

    /**
     * Возвращают Set ключей, сортированный в соответствии с
     * количеством вызовов каждого ключа
     */
    Set<Key> getMostCalledKeys();

    /**
     * Возвращает количество вызовов объекта по ключу
     */
    int getNumberOfCallsToObject(Key key);
}
