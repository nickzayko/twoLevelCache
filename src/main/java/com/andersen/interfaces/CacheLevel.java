package com.andersen.interfaces;

import java.io.IOException;

/**
 * Интерфейс является абстракцией двухуровнего кеша.
 * Метод reCache() используется для перемещения объектов между уровнями кеша
 */
public interface CacheLevel<Key, Value> extends Cache<Key, Value>, CallObjectCounts<Key> {

    void reCache() throws IOException, ClassNotFoundException;

}
