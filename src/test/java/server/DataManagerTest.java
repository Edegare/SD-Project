package server;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DataManagerTest {

    @Test
    void testPutAndGet() {
        DataManager dataManager = new DataManager();

        // Testa adicionar e recuperar valor
        assertTrue(dataManager.put("key1", "value1".getBytes()));
        assertEquals("value1", new String(dataManager.get("key1")));

        // Testa recuperação de chave inexistente
        assertNull(dataManager.get("key2"));
    }

    @Test
    void testMultiPutAndMultiGet() {
        DataManager dataManager = new DataManager();

        Map<String, byte[]> mapValues = new HashMap<>();
        mapValues.put("key1", "value1".getBytes());
        mapValues.put("key2", "value2".getBytes());

        dataManager.multiPut(mapValues);

        Set<String> keys = new HashSet<>(Arrays.asList("key1", "key2", "key3"));
        Map<String, byte[]> results = dataManager.multiGet(keys);

        assertEquals("value1", new String(results.get("key1")));
        assertEquals("value2", new String(results.get("key2")));
        assertNull(results.get("key3"));
    }

    /* @Test
    void testGetWhen() throws InterruptedException {
        DataManager dataManager = new DataManager();

        dataManager.put("condKey", "condValue".getBytes());
        dataManager.put("targetKey", "targetValue".getBytes());

        // Testa condição satisfeita
        byte[] value = dataManager.getWhen("targetKey", "condKey", "condValue".getBytes());
        assertEquals("targetValue", new String(value));
    } */
}
