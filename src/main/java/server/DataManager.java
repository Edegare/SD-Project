package server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataManager {
    private Map<String, byte[]> dataMap = new HashMap<>();
    private Lock l_manager = new ReentrantLock();
    private Condition c = l_manager.newCondition();

    // Single Write 
    public boolean put(String key, byte[] value) {
        l_manager.lock();
        try {
            if (value.length==0) return false;
            this.dataMap.put(key, value);
            c.signalAll(); // Notify all waiting threads
            return true;
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
            c.signalAll(); // Notify all waiting threads
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

    // Conditional get
    public byte[] getWhen(String key, String keyCond, byte[] valueCond) throws InterruptedException{
        l_manager.lock();

        try {
            byte[] v = this.dataMap.get(keyCond);

            // Check if value of the keycond is equal to the given value
            while (v == null || !Arrays.equals(v, valueCond)) {
                c.await();
                v = this.dataMap.get(keyCond);
            }

            return this.dataMap.getOrDefault(key, null);            
        } finally {
            l_manager.unlock();
        }
    }
}
