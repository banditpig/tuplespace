package js.co.uk.tuplespace.dist.hess.remote;

import js.co.uk.tuplespace.events.SpaceChangeListener;
import js.co.uk.tuplespace.matcher.Matcher;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.store.Transactional;
import js.co.uk.tuplespace.tuple.Tuple;

import java.util.List;
import js.co.uk.tuplespace.space.Space;

/**
 * 
 * This does not extend Space as the names are mangled by Hessian, see  SpaceServlet comments
 */
public interface HessianRemoteSpace extends Transactional {


    /**
     * Adds a SpaceChangeListener
     * @param listener
     */
    public void addSpaceChangeListener(final SpaceChangeListener listener);


    /**
     * Removes a SpaceChangeListener
     * 
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
     * @throws js.co.uk.tuplespace.store.TransactionException a TransactionException
     */
    public Tuple getWithTxn(final Tuple template, final TransactionID txnId) throws TransactionException;


    /**
     * Attempts to match a tuple in the space to the supplied template. The operation will block for up to timeOut milliseconds.
     * The 'take' operation is destructive in that a matched tuple is removed from the space.
     *
     * @param template the template
     * @param timeOut  how long to wait for a match.
     * @return the tuple that matches the template.
     */
    public Tuple getWithTimeout(final Tuple template, final long timeOut);


    /**
     * Attempts to match a tuple in the space to the supplied template. The operation will block for up to timeOut milliseconds.
     * The 'take' operation is destructive in that a matched tuple is removed from the space.
     *
     * @param template the template
     * @param timeOut  how long to wait for a match.
     * @param txnId    the id of the transaction
     * @return the tuple that matches the template.
     * @throws TransactionException a TransactionException
     */
    public Tuple getWithTimeoutAndTxn(final Tuple template, final long timeOut, final TransactionID txnId) throws
            TransactionException;


    /**
     * Purge all entries. All templates and tuples are removed.
     */
    public void purgeAllEntries();


    /**
     * Puts the tuple into the space with no timeout.
     * (In practice the timeout is Integer.MAX_VALUE milliseconds)
     *
     * @param tuple the tuple
     */
    public void put(final Tuple tuple);

    /**
     * Puts the tuple into the space, under a transaction,  with no timeout.
     * (In practice the timeout is Integer.MAX_VALUE milliseconds)
     *
     * @param tuple the tuple
     * @param txnId the id of the transaction
     * @throws TransactionException a TransactionException
     */
    public void putWithTxn(final Tuple tuple, final TransactionID txnId) throws TransactionException;


    /**
     * Puts the tuple into the space and the tuple will be purged after it has
     * been in the space for timeOut milliseconds.
     *
     * @param tuple   the tuple
     * @param timeOut the time out
     */
    public void putWithTimeout(final Tuple tuple, final int timeOut);

    /**
     * Puts the tuple into the space, under a transaction,  and the tuple will be purged after it has
     * been in the space for timeOut milliseconds.
     *
     * @param tuple   the tuple
     * @param timeOut the time out
     * @param txnId   the id of the transaction
     * @throws TransactionException a TransactionException
     */

    public void putWithTimeoutAndTxn(final Tuple tuple, final int timeOut, final TransactionID txnId) throws
            TransactionException;


    /**
     * Attempts to match a tuple in the space to the supplied template. The operation will block indefinately <p>
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
     * Attempts to match a tuple in the space to the supplied template. The operation will block for up to timeOut milliseconds. <p>
     * The template is matched using the space's current matcher. See {@link js.co.uk.tuplespace.matcher.Matcher}
     * Similar to the 'get' operations except that 'gets' removes the tuple and read does not.
     *
     * @param template the template
     * @param timeOut  how long to wait for a match.
     * @return the tuple that matches the template.
     */
    public Tuple readWithTimeout(final Tuple template, final long timeOut);

    /**
     * Attempts to match a tuple in the space to the supplied template. The operation will block for up to timeOut milliseconds. <p>
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
    public Tuple readWithTimeoutAndTxn(final Tuple template, final long timeOut, final TransactionID txnId)
            throws TransactionException;


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
     * @return a mathcing tuple or null
     * @throws TransactionException a TransactionException
     */
    public Tuple readIfExistsWithTxn(final Tuple template, final TransactionID txnId) throws
            TransactionException;


    /**
     * Sets the matcher that is used when reading or getting tuples. The matcher compares
     * a tuple in the space with the template.
     *
     * @param matcher matcher to use.
     * @see js.co.uk.tuplespace.matcher.FieldBasedMatcher
     */
    public void setMatcher(final Matcher<Tuple> matcher);

    /**
     * Lists all the tuples at the instant of invocation.
     *
     * @return
     */
    public List<Tuple> listAllTuples();


    /**
     * The size of a space is defined as the number of tuples in the space at the moment that this method
     * is called. After the method has returned the  value may well be stale - other processes may have added or removed tuples.
     * The result is best viewed as being an approximation.
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

