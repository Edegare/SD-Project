package server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataManager {
    private Map<String, byte[]> dataMap = new HashMap<>();
    private Lock l_manager = new ReentrantLock();
    /* private ReadLock l_read_manager = l_manager.readLock();
    private WriteLock l_write_manager = l_manager.writeLock();*/

    // Single Write 
    public void put(String key, byte[] value) {
        l_manager.lock();
        try {
            this.dataMap.put(key, value);
        } finally {
            l_manager.unlock();
        }
    }

    // Single Read
    public byte[] get(String key) {
        l_manager.lock();
        try {

            return this.dataMap.getOrDefault(key, null);
        } finally {
            l_manager.unlock();
        }
    }

    // Multi Write
    public void multiPut(Map<String, byte[]> mapValues) {
        l_manager.lock();
        try {
            for (Map.Entry<String, byte[]> e : mapValues.entrySet()) {
                this.dataMap.put(e.getKey(), e.getValue());
            }
        } finally {
            l_manager.unlock();
        }
    }

    // Multi Read
    public Map<String, byte[]> multiGet(Set<String> keys) {
        l_manager.lock();
        try {
            Map<String, byte[]> res = new HashMap<>();
            for (String key : keys) {
                if (this.dataMap.containsKey(key)) {
                    res.put(key, this.dataMap.get(key));
                }
            }
            return res;
        } finally {
            l_manager.unlock();
        }
    }
}
