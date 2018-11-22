package com.andersen.caches;

import com.andersen.comparator.CallCountComparator;
import com.andersen.interfaces.Cache;
import com.andersen.interfaces.CallObjectCounts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * Первый уровень памяти кеша, в основе лежит HashMap
 */
public class RamCache<Key, Value> implements Cache<Key, Value>, CallObjectCounts<Key> {

    /**
     * Сам кеш
     */
    private HashMap<Key, Value> hashMap;

    /**
     * Храним частоту вызовов объектов из кеша
     */
    private TreeMap<Key, Integer> callCounterMap;


    public RamCache() {
        hashMap = new HashMap<Key, Value>();
        callCounterMap = new TreeMap<Key, Integer>();
    }

    /**
     * Частота вызовов по умолчанию равна '1'
     */
    @Override
    public void cache(Key key, Value value) throws IOException, ClassNotFoundException {
        callCounterMap.put(key, 1);
        hashMap.put(key, value);
    }

    /**
     * если вызывали какой-либо объект, то увеличиваем число обращений к этому объекту
     */
    @Override
    public Value getObject(Key key) throws IOException, ClassNotFoundException {
        if (hashMap.containsKey(key)){
            int count = callCounterMap.get(key);
            count = count+1;
            callCounterMap.put(key, count);
            return hashMap.get(key);
        }
        return null;
    }

    @Override
    public void deleteObject(Key key) {
        if (hashMap.containsKey(key)){
            hashMap.remove(key);
            callCounterMap.remove(key);
        } else {
            System.out.println("Cache don't contains object with key: "+key);
        }
    }

    @Override
    public void cleanCache() {
        hashMap.clear();
        callCounterMap.clear();
    }

    @Override
    public int cacheSize() {
        return hashMap.size();
    }

    @Override
    public boolean containsKey(Key key) {
        return hashMap.containsKey(key);
    }

    @Override
    public Value removeObject(Key key) throws IOException, ClassNotFoundException {
        if (hashMap.containsKey(key)){
            Value object = this.getObject(key);
            this.deleteObject(key);
            return object;
        }
        return null;
    }

    @Override
    public Set<Key> getMostCalledKeys() {
        CallCountComparator callCountComparator = new CallCountComparator(callCounterMap);
        TreeMap<Key, Integer> sorted = new TreeMap(callCountComparator);
        sorted.putAll(callCounterMap);
        return sorted.keySet();
    }

    @Override
    public int getNumberOfCallsToObject(Key key) {
        if (hashMap.containsKey(key)){
            return callCounterMap.get(key);
        }
        return 0;
    }
}
