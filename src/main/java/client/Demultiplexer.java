package client;

import java.util.*;
import java.io.IOException;
import java.util.concurrent.locks.*;

import conn.*;

public class Demultiplexer implements AutoCloseable {
    private TaggedConnection conn;
    private Lock l = new ReentrantLock();
    private Map<Integer,Entry> map = new HashMap<>();
    private IOException ex = null;

    private class Entry {
        Condition cond = l.newCondition();
        Deque<byte[]> queue = new ArrayDeque<>();
        int waiters = 0;
    }

    private Entry get(int tag) {
        Entry e = map.get(tag);
        if (e == null) {
            e = new Entry();
            map.put(tag,e);
        }
        return e;
    }

    public Demultiplexer(TaggedConnection conn) {
        this.conn = conn;
    }

    public void start() {
        new Thread(() -> {
            try {
            for(;;) {
                Frame f = conn.receive();
                l.lock();
                try {
                    Entry e = this.get(f.tag);
                    e.queue.add(f.data);
                    e.cond.signal();
                } finally {
                    l.unlock();
                }
            }
            } catch (IOException ex) {
                l.lock();
                try {
                    this.ex = ex;
                    for (Entry es : map.values()) {
                        es.cond.signalAll();
                    }
                } finally {
                    l.unlock();
                }
            }
        }).start();
    }

    public void send (Frame frame) throws IOException {
        conn.send(frame);
    }

    public void send(int tag, byte[] data) throws IOException {
        conn.send(tag,data);
    }

    public byte[] receive(int tag) throws IOException, InterruptedException {
        l.lock();
        try {
            Entry e = this.get(tag);

            e.waiters++;
            while(e.queue.isEmpty() && this.ex == null) {
                e.cond.await();
            }
            e.waiters--;

            byte[] b = e.queue.poll();
            
            if (e.waiters == 0 && e.queue.isEmpty()) {
                map.remove(tag);
            }
            if (b != null) return b;
            else {
                throw this.ex;
            }
        } finally {
            l.unlock();
        }
    }

    public void close() throws IOException {
        conn.close();
    }
}