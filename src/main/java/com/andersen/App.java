package com.andersen;

import com.andersen.caches.TwoLevelCache;
import com.andersen.object.MyFile;

import java.io.IOException;
import java.security.Key;

/**
 * Проверка кеша!
 *
 */
public class App 
{
    private final static int MAX_RAM_CACHE_CAPACITY = 1;
    private final static int REQUESTS_FOR_CACHE = 3;

    public static void main( String[] args )
    {
        try {
            TwoLevelCache twoLevelCache = new TwoLevelCache(MAX_RAM_CACHE_CAPACITY, REQUESTS_FOR_CACHE);

            for (int i=0; i<21; i++){
                twoLevelCache.cache(""+1, new MyFile(""+i, i));
                if (i%3 == 0){
                    System.out.println(twoLevelCache.getObject(""+(i-2)));
                    testCacheSize(twoLevelCache);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void testCacheSize(TwoLevelCache twoLevelCache) {
        System.out.println("----------------------------------------");
        System.out.println("Ram: "+ twoLevelCache.ramCacheSize());
        System.out.println("Memory: " + twoLevelCache.memoryCacheSize());
        System.out.println("----------------------------------------");
    }
}
