package com.andersen.interfaces;

import java.io.IOException;

/**
 * Интерфейс, реализуемый кешами
 */
public interface Cache<Key, Value> {

    /**
     * Метод для кеширования объекта
     */
    void cache(Key key, Value value) throws IOException, ClassNotFoundException;

    /**
    * Метод возвращает объект по ключу
    */
    Value getObject(Key key) throws IOException, ClassNotFoundException;

    /**
    * Метод удаляет объект по ключу
    */
    void deleteObject(Key key);

    /**
    * Очистить кеш
    */
    void cleanCache();

    /**
    * Метод определяет размер кеша
    */
    int cacheSize();

    /**
     * Проверяет наличие ключа
     */
    boolean containsKey(Key key);

    /**
     * Удаляет объект из кеша и возвращает его, используется при рекешировании
     */
    Value removeObject(Key key) throws IOException, ClassNotFoundException;
}
