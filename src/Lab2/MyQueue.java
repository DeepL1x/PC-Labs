package Lab2;

import java.util.LinkedList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyQueue<T> {
    private final LinkedList<T> queue;
    private final ReadWriteLock lock;
    private final Integer maxSize;
    private Long start;
    private Long maxTime = 0L;
    private Long minTime = 0L;
    private Long ignored;
    private boolean timeInit = false;
    private boolean onTimer = false;

    public MyQueue(Integer maxSize) {
        queue = new LinkedList<>();
        lock = new ReentrantReadWriteLock();
        this.maxSize = maxSize;
        ignored = 0L;
    }

    public boolean isEmpty() {
        boolean isEmpty;
        lock.readLock().lock();

        isEmpty = queue.isEmpty();

        lock.readLock().unlock();
        return isEmpty;
    }

    public long getMaxTime(){
        long time;
        if (onTimer){
            lock.writeLock().lock();
            long finish = System.nanoTime();
            time = finish - start;
            if (time > maxTime)
                maxTime = time;
            if (!timeInit) {
                minTime = time;
                timeInit = true;
            }
            lock.writeLock().unlock();
        }
        lock.readLock().lock();
        time = maxTime;
        lock.readLock().unlock();
        return time;
    }

    public long getMinTime(){
        long time;
        if (onTimer){
            lock.writeLock().lock();
            long finish = System.nanoTime();
            time = finish - start;
            if (!timeInit) {
                minTime = time;
                timeInit = true;
            } else if (time < minTime)
                minTime = time;
            lock.writeLock().unlock();
        }
        lock.readLock().lock();
        time = minTime;
        lock.readLock().unlock();
        return time;
    }

    public long getIgnored(){
        long amount;
        lock.readLock().lock();
        amount = ignored;
        lock.readLock().unlock();
        return amount;
    }

    public int getSize() {
        int size;
        lock.readLock().lock();

        size = queue.size();

        lock.readLock().unlock();
        return size;
    }

    public void clear() {
        lock.writeLock().lock();

        while (!queue.isEmpty())
            queue.poll();

        lock.writeLock().unlock();
    }

    public T pop() {
        T elem;
        lock.writeLock().lock();
        if (queue.size() == maxSize) {
            Long finish = System.nanoTime();
            long time = finish - start;
            if (time > maxTime) {
                maxTime = time;
                onTimer = false;
                if (!timeInit) {
                    minTime = time;
                    timeInit = true;
                }
            } else if (time < minTime)
                minTime = time;
        }
        if (queue.isEmpty())
            return null;
        else
            elem = queue.poll();

        lock.writeLock().unlock();
        return elem;
    }

    public boolean add(T elem) {
        boolean res;
        lock.writeLock().lock();
        if (queue.size() == maxSize - 1) {
            start = System.nanoTime();
            if (!onTimer)
                onTimer = true;
        }
        if (queue.size() < maxSize)
            res = queue.add(elem);
        else {
            res = false;
            ignored++;
        }
        lock.writeLock().unlock();
        return res;
    }
}
