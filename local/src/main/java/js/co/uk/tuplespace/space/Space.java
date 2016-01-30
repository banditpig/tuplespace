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

import js.co.uk.tuplespace.events.SpaceChangeListener;
import js.co.uk.tuplespace.matcher.Matcher;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.store.Transactional;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.Tuple;

import java.util.List;


/**
 * This  expresses the notions of writing a tuple into the space,
 * with and without a timeout. It also includes 'global' space operations such as purging the space's entries, requesting the
 * number of entries etc.
 */

public interface Space extends Transactional {

    /**
     * Adds the supplied listener for space change events
     * @param listener
     */
    public void addSpaceChangeListener(final SpaceChangeListener listener);

    /**
     * Removes the supplied listener for space change events
     * @param listener
     */
    public void removeSpaceChangeListener(final SpaceChangeListener listener);

    /**
     * Gets the name of the space.
     *
     * @return the name of the space
     */
    public String getName();

    /**
     * Attempts to match a tuple in the space to the supplied template. There is no timeout so the
     * operation will block until a match occurs.
     * The 'take' operation is destructive in that a matched tuple is removed from the space.
     *
     * @param template the template for matching.
     * @return the matched tuple.
     */
    public Tuple get(final Tuple template);


    /**
     * Attempts to match a tuple in the space to the supplied template. There is no timeout so the
     * operation will block until a match occurs.
     * The 'take' operation is destructive in that a matched tuple is removed from the space.
     *
     * @param template the template for matching.
     * @param txnId    the id of the transaction
     * @return the matched tuple.
     * @throws TransactionException a TransactionException
     */
    public Tuple get(final Tuple template, final TransactionID txnId) throws TransactionException;


    /**
     * Attempts to match a tuple in the space to the supplied template. The operation will block
     * for up to timeOut milliseconds.
     * The 'take' operation is destructive in that a matched tuple is removed from the space.
     *
     * @param template the template
     * @param timeOut  how long to wait for a match.
     * @return the tuple that matches the template.
     */
    public Tuple get(final Tuple template, final long timeOut);


    /**
     * Attempts to match a tuple in the space to the supplied template within the supplied transaction.
     * The operation will block for up to timeOut milliseconds.
     * The 'take' operation is destructive in that a matched tuple is removed from the space.
     *
     * @param template the template
     * @param timeOut  how long to wait for a match.
     * @param txnId    the id of the transaction
     * @return the tuple that matches the template.
     * @throws TransactionException a TransactionException
     */
    public Tuple get(final Tuple template, final long timeOut, final TransactionID txnId) throws TransactionException;


    /**
     * Purge all entries. All templates and tuples are removed.
     */
    public void purgeAllEntries();


    /**
     * Puts the tuple into the space with no timeout.
     * (In practice the timeout is Long.MAX_VALUE Days)
     *
     * @param tuple the tuple
     */
    public void put(final Tuple tuple);

    /**
     * Puts the tuple into the space, under a transaction, with no timeout.
     * (In practice the timeout is Long.MAX_VALUE Days)
     *
     * @param tuple the tuple
     * @param txnId the id of the transaction
     * @throws TransactionException a TransactionException
     */
    public void put(final Tuple tuple, final TransactionID txnId) throws TransactionException;


    /**
     * Puts the tuple into the space and the tuple will be purged after it has
     * been in the space for timeOut milliseconds.
     *
     * @param tuple   the tuple
     * @param timeOut the time out
     */
    public void put(final Tuple tuple, final int timeOut);

    /**
     * Puts the tuple into the space, under a transaction,  and the tuple will be purged after it has
     * been in the space for timeOut milliseconds.
     *
     * @param tuple   the tuple
     * @param timeOut the time out
     * @param txnId   the id of the transaction
     * @throws TransactionException a TransactionException
     */
    public void put(final Tuple tuple, final int timeOut, final TransactionID txnId) throws TransactionException;


    /**
     * Attempts to match a tuple in the space to the supplied template. The operation will block indefinitely <p>
     * The template is matched using the space's current matcher. See {@link js.co.uk.tuplespace.matcher.Matcher}
     * Similar to the 'get' operations except that 'gets' removes the tuple and read does not <p>
     *
     * @param template the template
     * @return the tuple that matches the template.
     */
    public Tuple read(final Tuple template);
    
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
    public Tuple read(final Tuple template, final TransactionID txnId) throws TransactionException;

    /**
     * Attempts to match a tuple in the space to the supplied template.
     * The operation will block for up to timeOut milliseconds. <p>
     * The template is matched using the space's current matcher.
     * See {@link js.co.uk.tuplespace.matcher.Matcher}
     * Similar to the 'get' operations except that 'gets' removes the tuple and read does not.
     *
     * @param template the template
     * @param timeOut  how long to wait for a match.
     * @return the tuple that matches the template.
     */
    public Tuple read(final Tuple template, final long timeOut);

    /**
     * Attempts to match a tuple in the space to the supplied template under the given transaction.
     * The operation will block for up to timeOut milliseconds. <p>
     * The template is matched using the space's current matcher. See {@link js.co.uk.tuplespace.matcher.Matcher}
     * Similar to the 'get' operations except that 'gets' removes the tuple and read does not.
     * The attempt at reading will block for up to timeOut milliseconds. <p>
     *
     * @param template the template
     * @param timeOut  how long to wait for a match.
     * @param txnId    the id of the transaction
     * @return the tuple that matches the template.
     * @throws TransactionException a TransactionException
     */
    public Tuple read(final Tuple template, final long timeOut, final TransactionID txnId) throws TransactionException;


    /**
     * This is a non-blocking read using the supplied template.
     * It will return immediately with either a null value or a tuple  that matches the supplied template.
     *
     * @param template the template fro matching
     * @return a mathcing tuple or null
     */
    public Tuple readIfExists(final Tuple template);


    /**
     * This is a non-blocking read using the supplied template.
     * It will return immediately with either a null value or a tuple  that matches the supplied template.
     *
     * @param template the template fro matching
     * @param txnId    the id of the transaction
     * @return a matching tuple or null
     * @throws TransactionException a TransactionException
     */
    public Tuple readIfExists(final Tuple template, final TransactionID txnId) throws TransactionException;


    /**
     * Sets the matcher that is used when reading or getting tuples. The matcher compares
     * a tuple in the space with the template.
     *
     * @param matcher matcher to use.
     * @see js.co.uk.tuplespace.matcher.FieldBasedMatcher
     */
    public void setMatcher(final Matcher<Tuple> matcher);

    /**
     * Lists all the tuples at the intstant of invocation.
     *
     * @return
     */
    public List<Tuple> listAllTuples();


    /**
     * The size of a space is defined as the number of tuples in the space at the moment that this method
     * is called. After the method has returned the  value may well be stale - other processes may have added or removed tuples.
     * The result is best viewd as being an approximation.
     *
     * @return the number of tuples in the space when this method is invoked.
     */
    public int size();

    /**
     * This returns the number of matches that are pending at the instant the method is called. The number of pending matches
     * is the number of templates that are in the space.
     *
     * @return the number of matches that are pending.
     */
    public int pendingMatchesCount();


}
