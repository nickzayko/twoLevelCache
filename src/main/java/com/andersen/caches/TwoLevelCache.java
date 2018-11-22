package com.andersen.caches;

import com.andersen.interfaces.CacheLevel;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * Собственно это наш двухуровневый кеш.
 * Он инкапсулирует в себе два уровня кеша, и устанавливает
 * некоторую политику взаимоотношений между ними:
 * 1. Политика инвалидации, - алгоритм вытеснения на основе самых редко вызываемых объектов.
 * 2. Ограничение кол-ва памяти (maxRamCacheCapacity, сейчас переменная не используется
 * но возможно использовать это ограничение аналогично numberOfRequestsForReCache,
 * то есть сейчас рекеширование производится при увеличении количества запросов к объекту,
 * можно же производить рекеширование в тот момент, когда количество объектов в memoryCache
 * достигнет определённой отметки; ограничений на MemoryCache нет).
 * 3. Конкурентный доступ к кешу.
 */
public class TwoLevelCache<Key, Value extends Serializable> implements CacheLevel<Key, Value> {

    RamCache<Key, Value> ramCache;
    MemoryCache<Key,Value> memoryCache;

    /**
     * Максимальное количество записей в кэше оперативной памяти.
     * Если количество элементов превосходит это число - вызывается алгоритм рекэширования.
     */
    private int maxRamCacheCapacity;
    /**
     * Количество запросов к кэшу после последнего рекэширования.
     */
    private int numberOfRequests;
    /**
     * Количество запросов, необходимое для рекэширования.
     */
    private int requestsForReCache;

    public TwoLevelCache(int maxRamCacheCapacity, int requestsForReCache) throws IOException {
        this.maxRamCacheCapacity = maxRamCacheCapacity;
        this.requestsForReCache = requestsForReCache;
        this.ramCache = new RamCache<Key,Value>();
        this.memoryCache = new MemoryCache<Key, Value>();
        this.numberOfRequests = 0;
    }

    /**
     * Кешируем только в ram.
     * Принимает ключ и значение.
     */
    @Override
    public void cache(Key key, Value value) throws IOException, ClassNotFoundException {
        ramCache.cache(key, value);
    }

    /**
     * Проверяет наличие объекта на двух уровнях кеша,
     * если находит, возвращает его.
     */
    @Override
    public Value getObject(Key key) throws IOException, ClassNotFoundException {
        if (ramCache.containsKey(key)){
            doReCache();
            return ramCache.getObject(key);
        }

        if (memoryCache.containsKey(key)){
            doReCache();
            return  memoryCache.getObject(key);
        }
        return null;
    }


    /**
     * Инкрементирует количество вызовов объекта, при необходимости
     * вызывает reCache()
     * <p>
     * прим. несколько помогает избежать дублирования кода в методе doReCache()
     * собственно, работает в паре с этим методом.
     */
    private void doReCache() throws IOException, ClassNotFoundException {
        numberOfRequests++;
        if (numberOfRequests>requestsForReCache){
            this.reCache();
            numberOfRequests = 0;
        }
    }

    @Override
    public void deleteObject(Key key) {
        if (ramCache.containsKey(key)){
            ramCache.deleteObject(key);
        }
        if (memoryCache.containsKey(key)){
            memoryCache.deleteObject(key);
        }
    }

    @Override
    public void cleanCache() {
        ramCache.cleanCache();
        memoryCache.cleanCache();
    }

    @Override
    public int cacheSize() {
        return ramCache.cacheSize()+memoryCache.cacheSize();
    }

    public int ramCacheSize(){
        return ramCache.cacheSize();
    }

    public int memoryCacheSize(){
        return memoryCache.cacheSize();
    }

    /**
     * 1. При рекэшировании находится среднее арифметическое количества вызовов каждого объекта
     * 2. Редко используемые объекты переносятся из оперативной памяти, на жесткий диск.
     * 3. И наоборот, все объекты которые часто используются, хранящиеся на жестком диске,
     *    забрасываются в оперативную память.
     * <p>
     * Происходит постоянное перетасовывание объектов между двумя кэшами.
     */
    @Override
    public void reCache() throws IOException, ClassNotFoundException {
        TreeSet<Key> ramKeySet = new TreeSet<Key>(ramCache.getMostCalledKeys());
        int boundFrequency = 0;

//        1.
        for (Key key : ramKeySet) {
            boundFrequency += ramCache.getNumberOfCallsToObject(key);
        }
        boundFrequency /= ramKeySet.size();

//        2.
        for (Key key : ramKeySet) {
            if (ramCache.getNumberOfCallsToObject(key) <= boundFrequency){
                memoryCache.cache(key, ramCache.removeObject(key));
            }
        }

//        3.
        TreeSet<Key> memoryKeySet = new TreeSet<Key>(memoryCache.getMostCalledKeys());
        for (Key key : memoryKeySet) {
            try {
                if (memoryCache.getNumberOfCallsToObject(key) > boundFrequency) {
                    ramCache.cache(key, memoryCache.removeObject(key));
                }
            } catch (IOException  e){
                memoryCache.deleteObject(key);
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public Set<Key> getMostCalledKeys() {
        TreeSet<Key> set = new TreeSet<Key>(ramCache.getMostCalledKeys());
        set.addAll(memoryCache.getMostCalledKeys());
        return set;
    }


    @Override
    public boolean containsKey(Key key) {
        return memoryCache.containsKey(key) || ramCache.containsKey(key);
    }

    @Override
    public Value removeObject(Key key) throws IOException, ClassNotFoundException {
        if (ramCache.containsKey(key)){
            return ramCache.removeObject(key);
        }
        if (memoryCache.containsKey(key)){
            return memoryCache.removeObject(key);
        }
        return null;
    }



    @Override
    public int getNumberOfCallsToObject(Key key) {
        if (ramCache.containsKey(key)){
            return ramCache.getNumberOfCallsToObject(key);
        }
        if (memoryCache.containsKey(key)){
            return memoryCache.getNumberOfCallsToObject(key);
        }
        return 0;
    }
}
