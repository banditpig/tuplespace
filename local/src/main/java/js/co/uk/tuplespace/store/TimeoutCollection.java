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

import js.co.uk.tuplespace.matcher.Matcher;
import js.co.uk.tuplespace.tuple.MatchAllTuplesTemplate;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @param <V> 
 */
public class TimeoutCollection<V> {


    /**
     * The locks for put and get.
     */
    private final ReentrantLock lockForPut = new ReentrantLock();
    private final ReentrantLock lockForGet = new ReentrantLock();


    private BlockingQueue<Condition> conditionQueueTimeoutQueue = new LinkedBlockingQueue<Condition>();
    private TimeoutQueue<Condition> conditionQueue = new TimeoutQueue<Condition>(conditionQueueTimeoutQueue);


    //stores the templates
    /**
     * The template list.
     */
    private final CopyOnWriteArrayList<V> templateList = new CopyOnWriteArrayList<V>();

    //the primaryCollection itself
    private final CopyOnWriteArrayList<V> primaryCollection = new CopyOnWriteArrayList<V>();

    //values are put on the queue for timeout
    private final TimeoutQueue<V> valueTimeoutQueue;

    private TransactionManager<V> txnMgr;


    /**
     * The value timeout consumer queue.
     */
    private final BlockingQueue<V> valueTimeoutQueueConsumer;

    /**
     * The matcher.
     */
    private Matcher<V> matcher = null;


    private volatile boolean keepGoing = true;
    private final BlockingQueue<V> valueConsumerQueue;

    private final Thread valueTimeoutReader;

    private final Thread conditionQueueReader;


    /**
     * @param valueConsumerQueue the BlockingQueue that receives the  values  as they expire
     */
    public TimeoutCollection(final BlockingQueue<V> valueConsumerQueue) {


        valueTimeoutQueueConsumer = new LinkedBlockingQueue<V>();
        valueTimeoutQueue = new TimeoutQueue<V>(valueTimeoutQueueConsumer);
        this.valueConsumerQueue = valueConsumerQueue;

        /*
        I don't think having a valueConsumerQueue is adding anything (except complexity)
         because, ok, the timeouts are sent to a consumer queue but when tuples  are added/removed nothing is notified so
         really this is not really useful unless notifications for add/removed are implemented and then these timeouts will be
         handled by that mechanism.

         */
        valueTimeoutReader = new Thread(new Runnable() {
            @Override
            public void run() {
                //noinspection InfiniteLoopStatement
                while (isKeepGoing()) {
                    try {
                        //wait for a value  to expire
                        final V v = valueTimeoutQueueConsumer.poll(1, TimeUnit.NANOSECONDS);
                        //...if have a value then it has expired so remove the value from the primaryCollection
                        if (v != null) {

                            primaryCollection.remove(v);
                            //and into value queue - this exports the V being removed into an external Queue
                            if (valueConsumerQueue != null) {
                                valueConsumerQueue.add(v);
                            }
                        }


                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();

                    }
                }

            }
        });

        valueTimeoutReader.setDaemon(true);
        valueTimeoutReader.start();

        conditionQueueReader = new Thread(new Runnable() {
            @Override
            public void run() {
                //noinspection InfiniteLoopStatement
                while (isKeepGoing()) {
                    try {
                        //wait for a value  to expire
                        Condition condition = conditionQueueTimeoutQueue.poll(1, TimeUnit.NANOSECONDS);
                        if (condition != null) {
                            lockForGet.lock();
                            try {

                                condition.signalAll();

                            }
                            finally {
                                lockForGet.unlock();
                            }

                        }


                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();

                    }
                }

            }
        });
        conditionQueueReader.setDaemon(true);
        conditionQueueReader.start();


    }

    /**
     * Adds the supplied collection of TimeoutEntry to this TimeoutCollection.
     * Each entry, as given by TimeoutEntry#getItem, is added to the collection  for timeout a some point in the future
     * as dictated by the details of the TimeoutEntry.
     *
     * @param timeouts the collection of TimeoutEntry
     */
    public void addAllTimeouts(final Collection<TimeoutEntry<V>> timeouts) {
        lockForPut.lock();//needs to be the put lock as we are adding to the collection and queue
        try {
            for (TimeoutEntry<V> timeout : timeouts) {
                primaryCollection.add(timeout.getItem());
                valueTimeoutQueue.add(timeout);

            }

            lockForGet.lock();
            try {
                for (final Condition con : conditionQueue.toEntryList()) {
                    con.signalAll();

                }
            }
            finally {
                lockForGet.unlock();
            }


        }
        finally {
            lockForPut.unlock();
        }


    }

    /**
     * Terminated this collection
     */
    void terminate() {
        //need to stop the    keyTimeoutReader thread running
        setKeepGoing(false);
        valueTimeoutReader.interrupt();
        conditionQueueReader.interrupt();
        //and the header reader in the q
        valueTimeoutQueue.terminate();

    }

    /**
     * Sets the transaction manager to use.
     *
     * @param txnMgr the transaction manager
     */
    public void setTransactionManager(final TransactionManager<V> txnMgr) {
        this.txnMgr = txnMgr;
    }


    /**
     * Returns true if the collection is empty
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return size() == 0;
    }


    /**
     * Sets the matcher to use in this TimeoutMap
     *
     * @param matcher the Matcher to use
     */
    public synchronized void setMatcher(final Matcher<V> matcher) {
        this.matcher = matcher;


    }

    /**
     * Puts the supplied value into the map .
     * The value will timeout after Long.MAX_VALUE days , which is 2<sup>63</sup>-1 days, effectively meaning there is no timeout.
     *
     * @param value the value to be stored
     */
    public void put(final V value) {
        try {
            put(value, Long.MAX_VALUE, TimeUnit.DAYS, null);
        } catch (final TransactionException e) {
            //a null txnId will not cause an exception
        }


    }

    /**
     * Put into the map for a certain ammount of time.
     *
     * @param value   the value to be stored
     * @param timeOut the time period after which the value  is removed
     * @param unit    the time unit that the timeOut value refers to
     * @param txnId   the transaction, may  be null
     * @throws TransactionException possibel transaction exception
     */
    public void put(final V value, final long timeOut, final TimeUnit unit, final TransactionID txnId) throws TransactionException {
        lockForPut.lock();
        try {

            final TimeoutEntry<V> timeoutEntry = new TimeoutEntry<V>(value, timeOut, unit);
            if (txnId != null) {

                final Transaction<V> txn = txnMgr.getTransaction(txnId);
                txn.addValueToValues(timeoutEntry);
            } else {
                primaryCollection.add(value);
                valueTimeoutQueue.add(timeoutEntry);
            }

            //check if any conditions are waiting for a match - if they are then release them with signal all
            checkForMatch(value);


        }
        finally {
            lockForPut.unlock();
        }

    }

    /**
     * Blocking take of an entry that matches the supplied template. The match is determined by the Matcher.  If a matche does
     * occur the matched value is removed from the collection.
     *
     * @param template the template to match against
     * @return a value that matches the supplied template
     */
    public V take(final V template) {
        try {
            return take(template, Long.MAX_VALUE, TimeUnit.DAYS, null);

        } catch (TransactionException e) {
            //a null txnId will not cause an exception
        }
        return null;

    }

    /**
     * Attempt, for a limited time, to get a value V that matches the target as defined by the  matcher.
     * If a match does occur the matching entry is removed from the map prior to being returned.
     *
     * @param template the template to try and match against
     * @param timeOut  the time period after which the match attempt is aborted
     * @param unit     the time unit that the timeOut value refers to
     * @param txnId    the transaction, may be null
     * @return an entry that matches the template or null
     * @throws TransactionException possible transaction exception
     */


    public V take(final V template, final long timeOut, final TimeUnit unit, final TransactionID txnId) throws TransactionException {

        return waitForMatch(template, timeOut, unit, true, txnId);


    }


    /**
     * Attempt, for a limited time, to read  a value V that matches the target as defined by the  matcher.
     * If a match does occur the matching entry is not removed from the map prior to being returned.
     *
     * @param template the template to try and match against
     * @param timeOut  the time period after which the match attempt is aborted
     * @param unit     the time unit that the timeOut value refers to
     * @param txn      the transaction, may be null
     * @return an entry that matches the template or null
     * @throws TransactionException possible transaction exception
     */
    public V read(final V template, final long timeOut, final TimeUnit unit, final TransactionID txn) throws TransactionException {

        return waitForMatch(template, timeOut, unit, false, txn);
    }

    /**
     * A non-blocking read that attempts to match immediately using the supplied template.
     *
     * @param template the template to try and match against
     * @param txnId    the transaction, may be null
     * @return an entry that matches the template or null
     * @throws TransactionException possible transaction exception
     */
    public V readIfExists(final V template, final TransactionID txnId) throws TransactionException {
        lockForGet.lock();
        V matchedValue;
        try {
            if (txnId == null) {  //read outside txn
                //its a read  with no txn - its not being removed so txnMgr permission is not reqd.

                return getMatch(template, primaryCollection);

            } else {
                //its a read in a transaction
                /*
                    The search space is the primary collection and the values in the txn.
                    If its matched in the primary space then it must be marked as read.
                */
                final Transaction<V> txn = txnMgr.getTransaction(txnId);
                matchedValue = getMatch(template, primaryCollection);
                if (matchedValue != null) {

                    txn.addValueToRead(matchedValue);

                } else {

                    //failed to match in main so look in txn values
                    matchedValue = getMatch(template, txn.getValues());
                }
                return matchedValue;

            }
        }
        finally {
            lockForGet.unlock();
        }
    }

    /**
     * The size of this collection  at the instant that this method is called.
     *
     * @return the size of the collection at the instant that this method is called
     */
    public synchronized int size() {

        return primaryCollection.size();

    }

    /**
     * Helper method to show how many templates are currently in the set of templates
     * waiting for a match.
     *
     * @return the size of the template queue at the instant that this method is called
     */
    public synchronized int templateQueueSize() {

        return templateList.size();
    }

    /**
     *
     * @return   a list of all tuples at the instance of invocation
     */
    public synchronized List<V> listAllValues() {

        return primaryCollection;
    }

    /**
     * Clear.
     */
    public synchronized void clear() {

        valueTimeoutQueueConsumer.clear();
        valueTimeoutQueue.clear();

        //there may be match attempts still hanging  so release them.
        lockForGet.lock();
        try {

            for (final Condition con : conditionQueue.toEntryList()) {
                con.signalAll();

            }
            conditionQueue.clear();
        }
        finally {
            lockForGet.unlock();
        }

        templateList.clear();
        primaryCollection.clear();

        if (valueConsumerQueue != null) {
            valueConsumerQueue.clear();
        }


    }

    /**
     * @param template the template
     * @param timeOut  the timeout
     * @param timeUnit the timeUnit
     * @param isTake   read or write flag
     * @param txnId    transaction id - may be null
     * @return a matched value - note that it may never return
     * @throws TransactionException possible  transaction exception
     */
    private V waitForMatch(final V template, final long timeOut, final TimeUnit timeUnit, final boolean isTake, final TransactionID txnId) throws TransactionException {

        lockForGet.lock();
        try {


            templateList.add(template);

            //make a condition for this attempt at matching. The condition is 'global' ie its for a match here or in a txn
            final Condition condition = lockForGet.newCondition();
            conditionQueue.add(condition, timeOut, timeUnit);

            //while the condition exists
            V matchedValue = null;
            while (conditionQueue.contains(condition)) {

                try {
                    if (txnId == null) {

                        if (isTake) {   //its a get without a txn - but do need txnMgr permission to get it
                            matchedValue = getMatch(template, primaryCollection);
                            if (matchedValue != null && txnMgr.isAvailable(matchedValue)) { //want to remove it - has it been read under any  txn?
                                primaryCollection.remove(matchedValue);
                                valueTimeoutQueue.remove(matchedValue);
                                break;
                            }
                            matchedValue = null;
                        } else {    //its a read  with no txn - its not being removed so txnMgr permission is not reqd.

                            matchedValue = getMatch(template, primaryCollection);
                            if (matchedValue != null) {
                                break;
                            }
                        }


                    } else {  //in a transaction
                        final Transaction<V> txn = txnMgr.getTransaction(txnId);
                        if (isTake) {   //a get in a transaction

                            /*
                            The search space is the primary collection plus values held in the txn itself (ie those values that
                            have been written under the txn), permission is needed from the txnMgr because the value may have been read in another txn
                             */
                            //search the txn first
                            matchedValue = getMatch(template, txn.getValues());
                            if (matchedValue != null) {
                                //ok, remove it from the txn
                                txn.getValues().remove(matchedValue);
                                break;
                            } else {  //failed to match in the txn so look in the primary collection

                                matchedValue = getMatch(template, primaryCollection);
                                if (matchedValue != null && txnMgr.isAvailable(matchedValue, txn)) {
                                    primaryCollection.remove(matchedValue);

                                    /*
                                     How to handle timeouts when a get is done under a txn. If we do not call
                                     TimeoutEntry<V> timeoutEntry = valueTimeoutQueue.remove(matchedValue);
                                     then if the txn is only aborted after the values timeout has occured then an abort
                                     will put the space in a bad state. It'll have values that should have timedout.

                                     If we do call TimeoutEntry<V> timeoutEntry = valueTimeoutQueue.remove(matchedValue);
                                     then the timeoutEntry can be saved in the txn. But the timeoutEntry will not
                                     be 'active' whilst its in the txn so if the abort occurs after the timeout for the value should
                                     have occured then again the space is in an illegal state.

                                     Perhaps this TimeoutEntry<V> timeoutEntry = valueTimeoutQueue.remove(matchedValue); should
                                     be called and some absolute value of time in the  TimeoutEntry couldbe added to
                                     check if a TimeoutEntry is allowed to go back into the queue and only then will the value be committed

                                     */
                                    //*** This smells!. Soemtimes - but very rarely -  this timeoutEntry is null, the remove(...) method
                                    //is hacked to cover the error. But need to get to the root of why this happens.
                                    final TimeoutEntry<V> timeoutEntry = valueTimeoutQueue.remove(matchedValue);

                                    txn.addValueToTaken(timeoutEntry);
                                    txn.getValuesRead().remove(matchedValue);//not sure if need to do this
                                    break;
                                }
                            }

                        } else {    //its a read in a transaction
                            /*
                           The search space is the primary collection and the values in the txn.
                           If its matched in the primary space then it must be marked as read
                            */
                            matchedValue = getMatch(template, primaryCollection);
                            if (matchedValue != null) {
                                txn.addValueToRead(matchedValue);
                                break;
                            } else {

                                //failed to match in main to look in txn values
                                matchedValue = getMatch(template, txn.getValues());
                                if (matchedValue != null) {
                                    break;
                                }

                            }

                        }

                    }
                    //keep waiting
                    condition.await();


                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            //if we get here then we've matched or timedout, either way the template must be removed.
            templateList.remove(template);


            return matchedValue;
        }

        finally {
            lockForGet.unlock();
        }
    }


    /**
     * Thread termination flag
     *
     * @param state the new value
     */
    private synchronized void setKeepGoing(boolean state) {
        keepGoing = state;
    }

    /**
     * @return true if the    keyTimeoutReader is to continue reading
     */
    private synchronized boolean isKeepGoing() {
        return keepGoing;
    }


    /**
     * Checks to see if any templates match the supplied value.
     *
     * @param value the value to try and match against
     */
    private void checkForMatch(final V value) {

        for (final V template : templateList) {
            if (matcher.match(value, template)) {

                lockForGet.lock();
                try {

                    for (final Condition con : conditionQueue.toEntryList()) {
                        con.signalAll();
                    }
                }
                finally {
                    lockForGet.unlock();
                }
                return;
            }
        }
    }

    /**
     * Gets the match.
     *
     * @param template the template to try and match against
     * @param values   the collection of values to use as a matching space
     * @return an entry that matches the template or null
     */

    private V getMatch(final V template, final Collection<V> values) {

        for (V value : values) {
            //the MatchAllTuplesTemplate guarantees a match so return the first
            if (template instanceof MatchAllTuplesTemplate) {

                return value;
            }

            if (matcher.match(value, template)) {

                return value;

            }
        }
        return null;
    }


}
