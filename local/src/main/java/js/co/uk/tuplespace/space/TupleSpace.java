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

package js.co.uk.tuplespace.space;

import js.co.uk.tuplespace.events.*;
import js.co.uk.tuplespace.matcher.FieldBasedMatcher;
import js.co.uk.tuplespace.matcher.Matcher;
import js.co.uk.tuplespace.store.*;
import js.co.uk.tuplespace.tuple.Tuple;

import javax.swing.event.EventListenerList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The Class TupleSpace. The default implementation of the   {@link Space} interface.
 */
public class TupleSpace implements Space {

    public static final String DEFAULT_NAME = "defaultSpaceName";
    /* Collection of tuples to time out */
    private transient final TimeoutCollection<Tuple> spaceCollection;

    private transient final String name;
    private transient final TransactionManager<Tuple> txnMgr;
    /*
    Might be better to use Spring and AOP to intercept some of the methods in
    TimeoutCollection to get it to fire out events when values are added/removed/timedout
     */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Instantiates a new tuple space with the default space name.
     */
    public TupleSpace() {
        this(DEFAULT_NAME);

    }


    /**
     * Creates a TupleSpace with the supplied name. Calls to
     * js.co.uk.tuplespace.space.Space#getName()
     * will return the supplied name
     *
     * @param name the name of the space
     */
    public TupleSpace(final String name) {

        this.name = name;
        final BlockingQueue<Tuple> removalQueue = new LinkedBlockingQueue<Tuple>();
        spaceCollection = new TimeoutCollection<Tuple>(removalQueue);


        spaceCollection.setMatcher(new FieldBasedMatcher());
        txnMgr = new TransactionManager<Tuple>(spaceCollection);
        spaceCollection.setTransactionManager(txnMgr);
        final Thread header = new Thread(new Runnable() {
            public void run() {
                //noinspection InfiniteLoopStatement
                while (true) {
                    try {
                        //just remove it, if want to use it then do tuple = removalQueue.take();
                        final Tuple tuple = removalQueue.take();
                        fireSpaceChangedEvent(new SpaceChangeEvent(name, null, tuple, EventHint.TUPLE_TIMEDOUT));
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        header.setDaemon(true);
        header.start();

    }


    /**
     * Adds the supplied listener for space change events
     *
     * @param listener
     */
    public void addSpaceChangeListener(final SpaceChangeListener listener) {
        listenerList.add(SpaceChangeListener.class, listener);
    }

    /**
     * Removes the supplied listener for space change events
     *
     * @param listener
     */
    public void removeSpaceChangeListener(final SpaceChangeListener listener) {
        listenerList.remove(SpaceChangeListener.class, listener);
    }

    /**
     * Gets the name of the space.
     *
     * @return the name of the space
     */
    public String getName() {
        return name;
    }

    /**
     * Attempts to match a tuple in the space to the supplied template. There is no timeout so the
     * operation will block until a match occurs.
     * The 'get' operation is destructive in that a matched tuple is removed from the space.
     *
     * @param template the template for matching.
     * @return the matched tuple.
     */
    public Tuple get(final Tuple template) {
        final Tuple tuple = spaceCollection.take(template);
        fireSpaceChangedEvent(new SpaceChangeEvent(name, null, tuple, EventHint.TUPLE_REMOVED));
        return tuple;
    }

    /**
     * (non-Javadoc)
     *
     * @see js.co.uk.tuplespace.space.Space#get(Tuple, long)
     */
    @Override
    public Tuple get(final Tuple template, final TransactionID txnId) throws TransactionException {

        final Tuple tuple = spaceCollection.take(template, Long.MAX_VALUE, TimeUnit.DAYS, txnId);
        fireSpaceChangedEvent(new SpaceChangeEvent(name, txnId, tuple, EventHint.TUPLE_REMOVED));
        return tuple;
    }

    /**
     * Attempts to match a tuple in the space to the supplied template. The operation will block for up to timeOut milliseconds.
     * The 'get' operation is destructive in that a matched tuple is removed from the space.
     *
     * @param template the template
     * @param timeOut  how long to wait for a match.
     * @return the tuple that matches the template.
     */
    public Tuple get(final Tuple template, final long timeOut) {
        try {
            final Tuple tuple = spaceCollection.take(template, timeOut, TimeUnit.MILLISECONDS, null);
            fireSpaceChangedEvent(new SpaceChangeEvent(name, null, tuple, EventHint.TUPLE_REMOVED));
            return tuple;

        } catch (TransactionException e) {
            //a null txnId will not allow a TransactionException to be generated
        }
        return null;
    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.space.Space#get(Tuple, long, Long)
     */
    @Override
    public Tuple get(final Tuple template, final long timeOut, final TransactionID txnId) throws TransactionException {

        final Tuple tuple = spaceCollection.take(template, timeOut, TimeUnit.MILLISECONDS, txnId);
        fireSpaceChangedEvent(new SpaceChangeEvent(name, txnId, tuple, EventHint.TUPLE_REMOVED));
        return tuple;
    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.space.Space#purgeAllEntries()
     */
    @Override
    public void purgeAllEntries() {
        spaceCollection.clear();
        fireSpaceChangedEvent(new SpaceChangeEvent(name, (TransactionID)null, (Tuple)null, EventHint.SPACE_PURGED));

    }

    /**
     * Puts the tuple into the space with no timeout.
     * (In practice the timeout is Long.MAX_VALUE Days)
     *
     * @param tuple the tuple
     */
    public void put(final Tuple tuple) {
        try {
            spaceCollection.put(tuple, Long.MAX_VALUE, TimeUnit.DAYS, null);
            fireSpaceChangedEvent(new SpaceChangeEvent(name, null, tuple, EventHint.TUPLE_ADDED));
        } catch (TransactionException e) {
            //a null txnId will not allow a TransactionException to be generated
        }
    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.space.Space#put(Tuple, Long)
     */
    @Override
    public void put(final Tuple tuple, final TransactionID txnId) throws TransactionException {
        fireSpaceChangedEvent(new SpaceChangeEvent(name, txnId, tuple, EventHint.TUPLE_ADDED));
        spaceCollection.put(tuple, Long.MAX_VALUE, TimeUnit.DAYS, txnId);
    }

    /**
     * Puts the tuple into the space and the tuple will be purged after it has
     * been in the space for timeOut milliseconds.
     *
     * @param tuple   the tuple
     * @param timeOut the time out
     */
    public void put(final Tuple tuple, final int timeOut) {
        try {
            spaceCollection.put(tuple, timeOut, TimeUnit.MILLISECONDS, null);
            fireSpaceChangedEvent(new SpaceChangeEvent(name, null, tuple, EventHint.TUPLE_ADDED));
        } catch (TransactionException e) {

            //a null txnId will not allow a TransactionException to be generated

        }

    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.space.Space#put(Tuple, int, Long)
     */
    @Override
    public void put(final Tuple tuple, final int timeOut, final TransactionID txnId) throws TransactionException {
        // throw new UnsupportedOperationException();
        fireSpaceChangedEvent(new SpaceChangeEvent(name, txnId, tuple, EventHint.TUPLE_ADDED));
        spaceCollection.put(tuple, timeOut, TimeUnit.MILLISECONDS, txnId);
    }

    /**
     * Attempts to match a tuple in the space to the supplied template. The operation will block indefinitely <p>
     * The template is matched using the space's current matcher. See {@link js.co.uk.tuplespace.matcher.Matcher}
     * Similar to the 'get' operations except that 'gets' removes the tuple and read does not <p>
     *
     * @param template the template
     * @return the tuple that matches the template.
     */
    @Override
    public Tuple read(final Tuple template) {
        try {
            return spaceCollection.read(template, Long.MAX_VALUE, TimeUnit.DAYS, null);
        } catch (TransactionException te) {
            //a null txnId will not allow a TransactionException to be generated
        }
        return null;
    }

     /**
     * Attempts to match a tuple in the space to the supplied template under the given transaction and no timeout.
     * (In practice the timeout is Long.MAX_VALUE Days)
     * 
     * 
     * @param template the template
     * @param txnId the id of the transaction
     * @return the tuple that matches the template.
     * @throws TransactionException 
     */
    @Override
    public Tuple read(final Tuple template, final TransactionID txnId) throws TransactionException {
     
        return spaceCollection.read(template, Long.MAX_VALUE, TimeUnit.DAYS, txnId);
    }
    /**
     * Attempts to match a tuple in the space to the supplied template. The operation will block for up to timeOut milliseconds. <p>
     * The template is matched using the space's current matcher. See {@link js.co.uk.tuplespace.matcher.Matcher}
     * Similar to the 'get' operations except that 'gets' removes the tuple and read does not.
     *
     * @param template the template
     * @param timeOut  how long to wait for a match.
     * @return the tuple that matches the template.
     */
    public Tuple read(final Tuple template, final long timeOut) {
        try {
            return spaceCollection.read(template, timeOut, TimeUnit.MILLISECONDS, null);

        } catch (TransactionException te) {
            //a null txnId will not allow a TransactionException to be generated
        }

        return null;
    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.space.Space#read(Tuple, long, Long)
     */
    @Override
    public Tuple read(final Tuple template, final long timeOut, final TransactionID txnId) throws TransactionException {

        return spaceCollection.read(template, timeOut, TimeUnit.MILLISECONDS, txnId);
    }

    /**
     * This is a non-blocking read using the supplied template.
     * It will return immediately with either a null value or a tuple  that matches the supplied template.
     *
     * @param template the template fro matching
     * @return a matching tuple or null
     */
    public Tuple readIfExists(final Tuple template) {
        try {
            return spaceCollection.readIfExists(template, null);
        } catch (TransactionException txnEx) {
            //a null txnId will not allow a TransactionException to be generated
        }
        return null;
    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.space.Space#readIfExists(Tuple, Long)
     */
    @Override
    public Tuple readIfExists(final Tuple template, final TransactionID txnId) throws TransactionException {

        return spaceCollection.readIfExists(template, txnId);
    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.space.Space#setMatcher(js.co.uk.tuplespace.matcher.Matcher)
     */
    @Override
    public void setMatcher(final Matcher<Tuple> matcher) {
        spaceCollection.setMatcher(matcher);
    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.space.Space#size()
     */
    @Override
    public int size() {
        return spaceCollection.size();
    }

    /**
     * Lists all the tuples at the instant of invocation.
     *
     * @return
     */
    public List<Tuple> listAllTuples() {

        return spaceCollection.listAllValues();

    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.space.Space#pendingMatchesCount()
     */
    @Override
    public int pendingMatchesCount() {
        return spaceCollection.templateQueueSize();
    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.store.Transactional#beginTxn(Long)
     */
    @Override
    public TransactionID beginTxn(final Long timeOut) throws TransactionException {
        final TransactionID id = txnMgr.beginTxn(timeOut);
        fireSpaceChangedEvent(new SpaceChangeTransactionEvent(name, id, null, EventHint.TXN_CREATED));
        return id;
    }

    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.store.Transactional#abortTxn(Long)
     */
    @Override
    public Collection<TimeoutEntry<Tuple>> abortTxn(final TransactionID txnId) throws TransactionException {

        final Collection<TimeoutEntry<Tuple>> items = txnMgr.abortTxn(txnId);
        fireSpaceChangedEvent(new SpaceChangeTransactionEvent(name, txnId, items, EventHint.TXN_ABORTED));
        return items;
    }

    // Collection<TimeoutEntry<Tuple>>
    /* (non-Javadoc)
     * @see js.co.uk.tuplespace.store.Transactional#commitTxn(Long)
     */
    @Override
    public Collection<TimeoutEntry<Tuple>> commitTxn(final TransactionID txnId) throws TransactionException {

        final Collection<TimeoutEntry<Tuple>> items = txnMgr.commitTxn(txnId);
        fireSpaceChangedEvent(new SpaceChangeTransactionEvent(name, txnId, items,EventHint.TXN_COMMITTED));
        return items;
    }


    /**
     * Tells listeners of change event
     *
     * @param spaceChangeEvent
     */
    private void fireSpaceChangedEvent(final SpaceChange spaceChangeEvent) {

        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {

                ((SpaceChangeListener) listeners[i + 1]).spaceChanged(spaceChangeEvent);

        }

    }


}
