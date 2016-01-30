package js.co.uk.tuplespace.dist.hess.local;

import com.caucho.hessian.client.HessianProxyFactory;
import js.co.uk.tuplespace.events.SpaceChangeListener;
import js.co.uk.tuplespace.matcher.Matcher;
import js.co.uk.tuplespace.dist.hess.remote.HessianRemoteSpace;
import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.store.TimeoutEntry;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.Tuple;


import java.net.MalformedURLException;
import java.util.*;
import js.co.uk.tuplespace.space.SpaceCreator;

/**
 *
 * This is the client side way into Spaces
 *
 *
 */
public class HessianLocalSpaceCreator implements SpaceCreator<Space> {

    private static SpaceCreator<Space> creator;
    private static HessianProxyFactory factory = new HessianProxyFactory();
    private static String url;
    private static final Map<String, Space> spaces = new HashMap<String, Space>();

    /**
     * This should be initialised with the URL of the running
     * HessianRemoteSpaceCreator, ie the the Jetty server created by running
     * HessianRemoteSpaceCreator. Once that is done a local reference to a
     * remote space can be obtained by calling the static method createSpace
     * with a suitable name.
     *
     * @param urlForSpaces url of the HessianRemoteSpaceCreator
     */
    public HessianLocalSpaceCreator(final String urlForSpaces) {
        url = urlForSpaces;
        HessianProxyFactory factory = new HessianProxyFactory();


        try {
            creator = (js.co.uk.tuplespace.space.SpaceCreator) factory.create(js.co.uk.tuplespace.space.SpaceCreator.class, url + "/spaceManager");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad url : " + url + "/spaceManager");
        }

    }

    public synchronized Space createSpace(final String name) {



        try {
            if (spaces.containsKey(name)) {
                return spaces.get(name);

            }
            //i.e this is calling method createSpace in HessianRemoteSpaceCreator (which is a Servlet)
            creator.createSpace(name);
            //remoteSpace will be a proxy to the servlet at   .../name
            final HessianRemoteSpace remoteSpace = (HessianRemoteSpace) factory.create(url + "/" + name);
            Space local = wrapRemoteSpace(remoteSpace);
            spaces.put(name, local);
            return local;

        } catch (final MalformedURLException e) {
            throw new RuntimeException("Bad url : " + url + "/" + name);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private static Space wrapRemoteSpace(final HessianRemoteSpace remote) {

        Space local = new js.co.uk.tuplespace.space.Space() {
            @Override
            public void addSpaceChangeListener(final SpaceChangeListener listener) {
                remote.addSpaceChangeListener(listener);
            }

            @Override
            public void removeSpaceChangeListener(final SpaceChangeListener listener) {
                remote.removeSpaceChangeListener(listener);
            }

            @Override
            public String getName() {
                return remote.getName();
            }

            @Override
            public Tuple get(final Tuple tuple) {
                return remote.get(tuple);
            }

            @Override
            public Tuple get(final Tuple tuple, final TransactionID transactionID) throws TransactionException {
                return remote.getWithTxn(tuple, transactionID);
            }

            @Override
            public Tuple get(final Tuple tuple, final long l) {
                return remote.getWithTimeout(tuple, l);
            }

            @Override
            public Tuple get(final Tuple tuple, final long l, final TransactionID transactionID) throws TransactionException {
                return remote.getWithTimeoutAndTxn(tuple, l, transactionID);
            }

            @Override
            public void purgeAllEntries() {
                remote.purgeAllEntries();
            }

            @Override
            public void put(final Tuple tuple) {
                remote.put(tuple);
            }

            @Override
            public void put(final Tuple tuple, final TransactionID transactionID) throws TransactionException {
                remote.putWithTxn(tuple, transactionID);
            }

            @Override
            public void put(final Tuple tuple, int i) {
                remote.putWithTimeout(tuple, i);
            }

            @Override
            public void put(final Tuple tuple, final int i, final TransactionID transactionID) throws TransactionException {
                remote.putWithTimeoutAndTxn(tuple, i, transactionID);
            }

            @Override
            public Tuple read(final Tuple tuple) {
                return remote.read(tuple);
            }

            @Override
            public Tuple read(final Tuple tuple, final long l) {
                return remote.readWithTimeout(tuple, l);
            }

            @Override
            public Tuple read(final Tuple tuple, final long l, final TransactionID transactionID) throws TransactionException {
                return remote.readWithTimeoutAndTxn(tuple, l, transactionID);
            }

            @Override
            public Tuple readIfExists(final Tuple tuple) {
                return remote.readIfExists(tuple);
            }

            @Override
            public Tuple readIfExists(final Tuple tuple, final TransactionID transactionID) throws TransactionException {
                return remote.readIfExistsWithTxn(tuple, transactionID);
            }

            @Override
            public void setMatcher(final Matcher<Tuple> tupleMatcher) {
                remote.setMatcher(tupleMatcher);
            }

            @Override
            public List<Tuple> listAllTuples() {
                return remote.listAllTuples();
            }

            @Override
            public int size() {
                return remote.size();
            }

            @Override
            public int pendingMatchesCount() {
                return remote.pendingMatchesCount();
            }

            @Override
            public TransactionID beginTxn(final Long aLong) throws TransactionException {
                return remote.beginTxn(aLong);
            }

            @Override
            public Collection<TimeoutEntry<Tuple>> abortTxn(final TransactionID transactionID) throws TransactionException {
                return remote.abortTxn(transactionID);
            }

            @Override
            public Collection<TimeoutEntry<Tuple>> commitTxn(final TransactionID transactionID) throws TransactionException {

                return remote.commitTxn(transactionID);
            }

            @Override
            public Tuple read(final Tuple tuple, final TransactionID transactionID) throws TransactionException {
                return remote.read(tuple, transactionID);
            }
        };

        return local;
    }
}
