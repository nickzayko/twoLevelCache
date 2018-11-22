package com.andersen.caches;

import com.andersen.comparator.CallCountComparator;
import com.andersen.interfaces.Cache;
import com.andersen.interfaces.CallObjectCounts;

import java.io.*;
import java.util.*;

/**
 * Второй уровень кеша, объекты хранятся на жестком диске в папке temp
 */
public class MemoryCache<Key, Value extends Serializable> implements Cache<Key, Value>, CallObjectCounts<Key> {

    /**
     * коллекции используются аналогично коллекциям из RamCache
     */
    HashMap<Key, String> hashMap;
    TreeMap<Key, Integer> frequencyMap;



    /**
     * создаем временную папку, в которой будут храниться элементы второго уровня кеша
     */
    public MemoryCache() throws IOException {
        hashMap = new HashMap<Key,String>();
        frequencyMap = new TreeMap<Key,Integer>();

        File tempFolder = new File("temp/");
        if (!tempFolder.exists()){
            tempFolder.mkdirs();
        }
    }

    /**
     * 1. Метод UUID.randomUUID().toString() используется для получения
     * уникального идентификационного имени для хранения на жестком диске в папке temp.
     * <P> вообще классный Класс! Раньше, как альтернативой, пользовался бы currentTime
     * 2. В hashMap помещается полный путь до этого объекта, а в качестве ключа используется
     * хэш этого объекта в системе.
     */
    @Override
    public void cache(Key key, Value value) throws IOException, ClassNotFoundException {
        String pathToObject;
        pathToObject = "temp/"+ UUID.randomUUID().toString()+".temp";

        frequencyMap.put(key, 1);
        hashMap.put(key, pathToObject);

        FileOutputStream fileOutputStream = new FileOutputStream(pathToObject);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
        objectOutputStream.close();
        fileOutputStream.flush();
        fileOutputStream.close();

    }

    /**
     * 1. Получаем по ключу адрес файла на жестком диске из hashMap
     * 2. Читаем его
     * 3. Десериализуем
     * 4. Увеличиваем на один частоту вызова
     * 5. Очищаем потоки.
     */
    @Override
    public Value getObject(Key key) throws IOException, ClassNotFoundException {
        if (hashMap.containsKey(key)){
            String filePath = hashMap.get(key);
            FileInputStream fileInputStream = new FileInputStream(filePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Value objectDeSerialization = (Value) objectInputStream.readObject();
            int frequency = frequencyMap.get(key);
            frequencyMap.put(key, ++frequency);
            objectInputStream.close();
            fileInputStream.close();
            return objectDeSerialization;
        }
        return null;
    }

    @Override
    public void deleteObject(Key key) {
        if (hashMap.containsKey(key)){
            File deletedFile = new File(hashMap.remove(key));
//            hashMap.remove(key);
            deletedFile.delete();
            frequencyMap.remove(key);
        }
    }

    @Override
    public void cleanCache() {
        for (Key key : hashMap.keySet()) {
            File deletingFile = new File(hashMap.remove(key));
            deletingFile.delete();
        }
        hashMap.clear();
        frequencyMap.clear();
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
            Value removedObject = this.getObject(key);
            this.deleteObject(key);
            return removedObject;
        }
        return null;
    }

    @Override
    public Set<Key> getMostCalledKeys() {
        CallCountComparator callCountComparator = new CallCountComparator(frequencyMap);
        TreeMap<Key, Integer> sorted = new TreeMap<>(callCountComparator);
        sorted.putAll(frequencyMap);
        return sorted.keySet();
    }

    @Override
    public int getNumberOfCallsToObject(Key key) {
        if (hashMap.containsKey(key)){
            return frequencyMap.get(key);
        }
        return 0;
    }
}
