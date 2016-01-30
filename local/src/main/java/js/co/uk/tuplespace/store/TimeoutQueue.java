/*
 * ******************************************************************************
 *  * Copyright (c) 2011. Mike Houghton.
 *  *
 *  *
 *  * This file is part of 'TupleSpace'.
 *  *
 *  * 'TupleSpace' is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 *  * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * 'TupleSpace' is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along with 'TupleSpace'.
 *  * If not, see http://www.gnu.org/licenses/.
 *  *****************************************************************************
 */

package js.co.uk.tuplespace.store;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;


/**
 * A queue that times out its entries.
 *
 * @param <T> the generic type
 */
public class TimeoutQueue<T>  {

    /**
     * The lock.
     */
    private transient final ReentrantLock lock = new ReentrantLock();

    /**
     * The internal q that provide the functionality.
     */
    private final DelayQueue<TimeoutEntry<T>> expireQueue = new DelayQueue<TimeoutEntry<T>>();


    /**
     * provides mapping from entries to entries in the q.
     */
    //  private final ConcurrentHashMap<T, TimeoutEntry<T>> entryMap = new ConcurrentHashMap<T, TimeoutEntry<T>>();
    private final CopyOnWriteArrayList<T> entries = new CopyOnWriteArrayList<T>();
    private volatile boolean keepGoing = true;
    private final Thread headerReader;

    /**
     * Create a TimeoutQueue that uses the supplied  BlockingQueue as a consumer of
     * entries. As each entry timesout it is placed into the BlockingQueue.
     * The underlying queue has no explicit capacity restrictions
     *
     * @param consumerQueue the queue on to which expired entries are placed (using  consumerQueue#put(...) )
     */
    public TimeoutQueue(final BlockingQueue<T> consumerQueue) {


        headerReader = new Thread(new HeadReader(consumerQueue));
        headerReader.setDaemon(true);
        headerReader.start();
    }


    /**
     * Determines if the supplied entry is on the TimeoutQueue.
     *
     * @param entry the entry to check
     * @return true if the supplied entry is on the queue and has not yet timed out
     */
    public boolean contains(final T entry) {

        lock.lock();

        try {
            return entries.contains(entry);
        }
        finally {
            lock.unlock();
        }

    }


    /**
     * Size of the queue at the instant of calling.
     *
     * @return the int
     */
    public int size() {

        return expireQueue.size();
    }


    /**
     * Inserts the specified element into this queue if it is possible to do so immediately without
     * violating capacity restrictions, returning true upon success and throwing
     * an IllegalStateException if no space is currently available. An IllegalArgumentException is thrown
     * if the   timeOut parameter is zero.
     *
     * @param entry   the item to add for time out
     * @param timeOut how long until the item expires
     * @param unit    the time unit of the timeOut parameter
     * @return TimeoutEntry<T>
     */
    public TimeoutEntry<T> add(final T entry, final long timeOut, final TimeUnit unit) {


        lock.lock();
        try {
            if (timeOut == 0) {
                throw new IllegalArgumentException("Timeout of zero is not allowed");
            }
            //wrap it and  map it
            final TimeoutEntry<T> timeoutEntry = new TimeoutEntry<T>(entry, timeOut, unit);
            entries.add(entry);
            expireQueue.add(timeoutEntry);

            return timeoutEntry;
        }
        finally {
            lock.unlock();
        }

    }

    /**
     * Adds the supplied TimeoutEntry<T>  to this queue
     *
     * @param timeoutEntry the TimeoutEntry<T>
     */
    public void add(final TimeoutEntry<T> timeoutEntry) {
        lock.lock();
        try {
            entries.add(timeoutEntry.getItem());
            expireQueue.add(timeoutEntry);
        }
        finally {
            lock.unlock();
        }

    }

    /**
     * Terminates this queue. Items are no longer timedout but any existing entries are still in the queue
     */
    public void terminate() {
        setKeepGoing(false);
        headerReader.interrupt();

    }

    /**
     * Clears the TimeoutQueue. All entries are deleted immediately
     */
    public void clear() {
        lock.lock();
        try {
            expireQueue.clear();
            entries.clear();
        }
        finally {
            lock.unlock();
        }

    }


    /**
     * Removes the item of type T
     *
     * @param item item to remove
     * @return the  TimeoutEntry that wraps this item
     */
    public TimeoutEntry<T> remove(final T item) {
        lock.lock();
        try {

            /*
            //*** This smells! - see comments in waitForMatch that start with  comment  *** This smells!

           
             */
            final TimeoutEntry<T>[] timeouts = new TimeoutEntry[entries.size()];
            expireQueue.toArray(timeouts);

            for (final TimeoutEntry<T> timeout : timeouts) {
                if (timeout.getItem() == item) {
                    entries.remove(item);
                    expireQueue.remove(timeout);
                    return timeout;
                }
            }

            //TODO why does this sometimes happen? Has the entry timed out before it was removed?
            /*
            This is a hack-fix, the caller is expecting a  TimeoutEntry wrapping item, so make one!!
             */
            return new TimeoutEntry<T>(item,Long.MAX_VALUE, TimeUnit.DAYS);


        }
        finally {
            lock.unlock();
        }

    }

    /**
     * Gets all of the  TimeoutEntry<T> in this queue at the instant of invokation
     *
     * @return a collection of all the TimeoutEntry<T>
     */
    public Collection<TimeoutEntry<T>> toTimeoutCollection() {
        lock.lock();
        try {
            final TimeoutEntry<T>[] timeouts = new TimeoutEntry[expireQueue.size()];
            expireQueue.toArray(timeouts);

            final List<TimeoutEntry<T>> list = new ArrayList<TimeoutEntry<T>>();
            list.addAll(Arrays.asList(timeouts));

            return list;
        } finally {
            lock.unlock();
        }


    }

    /**
     * returns a list of all the entries, ie the Ts, currently held at the moment of invocation
     *
     * @return the entries
     */
    public List<T> toEntryList() {
        lock.lock();

        try {
            return entries;
        } finally {
            lock.unlock();
        }

    }


    /**
     * Take.
     *
     * @return the t
     * @throws InterruptedException the interrupted exception
     */
    private T take() throws InterruptedException {

        final TimeoutEntry<T> entry = expireQueue.poll(1, TimeUnit.NANOSECONDS);
        if (entry != null) {
            entries.remove(entry.getItem());
            return entry.getItem();
        }

        return null;


    }

    /**
     * @param state the new state
     */
    private synchronized void setKeepGoing(boolean state) {
        keepGoing = state;
    }

    /**
     * @return the state
     */
    private synchronized boolean isKeepGoing() {
        return keepGoing;
    }

    /**
     * The Class HeadReader.
     */
    private class HeadReader implements Runnable {
        private final BlockingQueue<T> consumerQueue;

        public HeadReader(final BlockingQueue<T> consumerQueue) {
            this.consumerQueue = consumerQueue;
        }

        /**
         * Run.
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (isKeepGoing()) {
                try {
                    final T item = take();


                    if ((item != null) && (consumerQueue != null)) {
                        consumerQueue.put(item);
                    }

                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }

        }
    }
}
