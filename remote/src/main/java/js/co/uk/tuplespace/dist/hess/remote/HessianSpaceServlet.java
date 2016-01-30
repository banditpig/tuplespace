package js.co.uk.tuplespace.dist.hess.remote;

import com.caucho.hessian.server.HessianServlet;
import js.co.uk.tuplespace.events.SpaceChangeListener;
import js.co.uk.tuplespace.matcher.Matcher;
import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.space.TupleSpace;
import js.co.uk.tuplespace.store.TimeoutEntry;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.Tuple;

import java.util.Collection;
import java.util.List;

/**
 * This servlet is the web tier way in to a space. It is backed by a Space to which it
 * delegates.
 * It does not implement the Space interface because of the way that Hessian mangles method names the result being
 * that the mangled method names in the Space interface were either not unique or 
 * not meaningful in a client API. Hence this class has unique method names that 
 * map onto Space methods. However this is 'transparent' because the LocalSpaceCreator
 * wraps a HessianRemoteSpace into a Space.
 * 
 * (I think this is more complex than it should be!)
 * 
 * 
 */
public class HessianSpaceServlet extends HessianServlet implements HessianRemoteSpace {

    private Space space;
    /**
     * A named HessianSpaceServlet that delegates to a private Space.
     * 
     * @param name
     */
    public HessianSpaceServlet(final String name)
    {
        super();
        space = new TupleSpace(name);
    }

    /**
     * 
     * @param listener
     */
    @Override
    public void addSpaceChangeListener(final SpaceChangeListener listener) {
        space.addSpaceChangeListener(listener);

    }

    /**
     * 
     * @param listener
     */
    @Override
    public void removeSpaceChangeListener(final SpaceChangeListener listener) {
        space.removeSpaceChangeListener(listener);

    }

    @Override
    public String getName() {
        return space.getName();
    }
    @Override
    public Tuple get(final Tuple tuple) {
        return space.get(tuple);

    }
    @Override
    public Tuple getWithTxn(final Tuple tuple, final TransactionID transactionID) throws TransactionException {
        return space.get(tuple, transactionID);
    }
    @Override
    public Tuple getWithTimeout(final Tuple tuple, final long l) {
        return space.get(tuple, l);
    }
    @Override
    public Tuple getWithTimeoutAndTxn(final Tuple tuple, long l, final TransactionID transactionID) throws TransactionException {
        return space.get(tuple, l, transactionID);
    }
    @Override
    public void purgeAllEntries() {
         space.purgeAllEntries();
    }
    @Override
    public void put(final Tuple tuple) {
        space.put(tuple);
    }
    @Override
    public void putWithTxn(final Tuple tuple, final TransactionID transactionID) throws TransactionException {
        space.put(tuple, transactionID);
    }
    @Override
    public void putWithTimeout(final Tuple tuple, int i) {
        space.put(tuple, i);
    }
    @Override
    public void putWithTimeoutAndTxn(final Tuple tuple, final int i, final TransactionID transactionID) throws TransactionException {
        space.put(tuple, i, transactionID);
    }
    @Override
    public Tuple read(final Tuple tuple) {
        return space.read(tuple);
    }
    @Override
    public Tuple read(final Tuple template, final TransactionID txnId) throws TransactionException {
        return space.read(template, txnId);
    }
    @Override
    public Tuple readWithTimeout(final Tuple tuple, final long l) {
        return space.read(tuple, l);
    }
    @Override
    public Tuple readWithTimeoutAndTxn(final Tuple tuple, final long l, final TransactionID transactionID) throws TransactionException {
        return space.read(tuple, l, transactionID);
    }
    @Override
    public Tuple readIfExists(final Tuple tuple) {
        return space.readIfExists(tuple) ;
    }
    @Override
    public Tuple readIfExistsWithTxn(final Tuple tuple, final TransactionID transactionID) throws TransactionException {
        return space.readIfExists(tuple, transactionID);
    }
    @Override
    public void setMatcher(final Matcher<Tuple> tupleMatcher) {
            space.setMatcher(tupleMatcher);
    }
    @Override
    public List<Tuple> listAllTuples() {
        return space.listAllTuples();
    }
    @Override
    public int size() {
        return space.size();
    }
    @Override
    public int pendingMatchesCount() {
        return 0;
    }
    /**
     * 
     * @param aLong
     * @return
     * @throws TransactionException
     */
    @Override
    public TransactionID beginTxn(final Long aLong) throws TransactionException {
        return space.beginTxn(aLong);
    }
    /**
     * 
     * @param transactionID
     * @return
     * @throws TransactionException
     */
    @Override
    public Collection<TimeoutEntry<Tuple>>  abortTxn(final TransactionID transactionID) throws TransactionException {
           return space.abortTxn(transactionID);

    }
    /**
     * 
     * @param transactionID
     * @return
     * @throws TransactionException
     */
    @Override
    public Collection<TimeoutEntry<Tuple>>  commitTxn(final TransactionID transactionID) throws TransactionException {
        return space.commitTxn(transactionID);

    }

   

  
}
