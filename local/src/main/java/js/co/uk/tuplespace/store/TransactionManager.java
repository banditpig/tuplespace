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


import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * Manages the transactions for a TupleSpace.
 * @param <V> 
 */
public class TransactionManager<V> implements Transactional {


    private TimeoutQueue<Transaction<V>> txnTimeoutQueue;

    private Map<TransactionID, Transaction<V>> idToTxnMap = new ConcurrentHashMap<TransactionID, Transaction<V>>();
    /**
     * The txn removal queue.
     */
    private final BlockingQueue<Transaction<V>> txnRemovalQueue;
    /**
     * The transaction id.
     */
    private Long transactionID = 0L;


    /**
     * The parent collection thats uses the services of this .
     */
    private final TimeoutCollection<V> parentCollection;


    /**
     * Instantiates a new transaction manager.
     *
     * @param parentCollection thats uses this manager
     */

    public TransactionManager(final TimeoutCollection<V> parentCollection) {
        this.parentCollection = parentCollection;

        txnRemovalQueue = new LinkedBlockingQueue<Transaction<V>>();
        txnTimeoutQueue = new TimeoutQueue<Transaction<V>>(txnRemovalQueue);
        final Thread txnHeadReader = new Thread(new Runnable() {
            public void run() {
                //noinspection InfiniteLoopStatement
                while (true) {
                    try {

                        final Transaction<V> txn = txnRemovalQueue.take();

                        try {
                             abortTxn(txn.getTxnId());

                        } catch (final TransactionException e) {
                            //TODO need to get this exception out??
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }

                    }
                    catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        txnHeadReader.setDaemon(true);
        txnHeadReader.start();

    }

    /**
     * only true if its not been read under a  txn other than the supplied txn
     *
     * @param value the value to check for availability
     * @param txn   the transaction
     * @return true if its not been read under a  txn other than the supplied txn
     */
    public boolean isAvailable(final V value, final Transaction<V> txn) {


        for (final Transaction<V> transaction : txnTimeoutQueue.toEntryList()) {
            if (transaction != txn) {
                if (txn.getValuesRead().contains(value)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Not been read AT ALL
     *
     * @param value the value
     * @return true of the value has not been read at all
     */
    public boolean isAvailable(final V value) {

        for (final Transaction<V> transaction : txnTimeoutQueue.toEntryList()) {

            if (transaction.getValuesRead().contains(value)) {

                return false;
            }

        }

        return true;
    }


    /**
     * Begin txn.
     *
     * @param timeOut the time out
     * @return the long
     * @throws js.co.uk.tuplespace.store.TransactionException
     *          the transaction exception
     */
    public synchronized TransactionID beginTxn(final Long timeOut) throws TransactionException {
        //new id
        final Transaction<V> txn = new Transaction<V>(++transactionID, parentCollection);

        txnTimeoutQueue.add(txn, timeOut, TimeUnit.MILLISECONDS);
        idToTxnMap.put(txn.getTxnId(), txn);
        return txn.getTxnId();
    }


    /**
     * Abort txn.
     *
     *
     *
     *
     * @param txnId the id of the transaction
     * @throws js.co.uk.tuplespace.store.TransactionException
     *          the transaction exception
     */
    public synchronized Collection<TimeoutEntry<V>> abortTxn(final TransactionID txnId) throws TransactionException {
        final Transaction<V> txn = idToTxnMap.remove(txnId);
        final Collection<TimeoutEntry<V>> items = txn.abort();
        txnTimeoutQueue.remove(txn);
        return items;
    }

    //todo Do I really need to return a   Collection<TimeoutEntry<V>> from these methods???
    /**
     * Commit txn.
     *
     * @param txnId id of the transaction to commit
     * @throws js.co.uk.tuplespace.store.TransactionException
     *          the transaction exception
     */
    public synchronized Collection<TimeoutEntry<V>> commitTxn(final TransactionID txnId) throws TransactionException {
        final Transaction<V> txn = idToTxnMap.remove(txnId);
        if (txn == null) {
            throw new TransactionException("Transaction id " + txnId + " does not reference a valid transaction");
        }
        final Collection<TimeoutEntry<V>> items =  txn.commit();
        txnTimeoutQueue.remove(txn);
        return items;
    }

    /**
     * @param txnId the id of the transaction
     * @return the transaction associated with the supplied txnId
     * @throws TransactionException thrown if the txnId references an invalid transaction
     */
    public Transaction<V> getTransaction(final TransactionID txnId) throws TransactionException {
        final Transaction<V> txn = idToTxnMap.get(txnId);
        if (txn == null) {
            throw new TransactionException("Transaction id " + txnId + " does not reference a valid transaction");
        }
        return idToTxnMap.get(txnId);
    }
}
