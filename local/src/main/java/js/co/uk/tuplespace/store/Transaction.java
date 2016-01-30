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
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Class for a transaction. It maintains details of values read, written and taken.
 * It is not meant to be used directly and usually a Transaction is created and handled by a Space and references to
 * Transactions are pushed around using a {@link js.co.uk.tuplespace.store.TransactionID}
 * 
 * @param <V> 
 */
public class Transaction<V>  {


    private final transient BlockingQueue<V> timeoutQueue = new LinkedBlockingQueue<V>();


    //needs timeout
    private final transient TimeoutQueue<V> takenQue = new TimeoutQueue<V>(timeoutQueue);

    //needs timeout
    private final transient TimeoutQueue<V> valuesQueue = new TimeoutQueue<V>(timeoutQueue);


    private final transient CopyOnWriteArrayList<V> valuesRead = new CopyOnWriteArrayList<V>();

    private transient TimeoutCollection<V> parentCollection;

    private final TransactionID txnId;


    /**
     * Creates a Transaction
     *
     * @param txnId the long value of this Transaction
     *  @param  parentCollection the parent collection
     */
    protected Transaction(final Long txnId, final TimeoutCollection<V> parentCollection) {
        this.txnId = new TransactionID(txnId);
        
        this.parentCollection = parentCollection;
    }

    /**
     * Gets the list of items that have been read under this transaction
     * @return  the items read
     */
    protected CopyOnWriteArrayList<V> getValuesRead() {
        return valuesRead;
    }

    /**
     * Gets the list of values that have been written under this transaction
     * @return     the values
     */
    protected List<V> getValues() {
        return valuesQueue.toEntryList();
    }

    /**
     *   Adds a value to the values  that have been read under this transaction
     * @param value the value
     */
    protected void addValueToRead(final V value) {
        valuesRead.add(value);
    }

    /**
     * When a value is removed from the main space its {@link TimeoutEntry} is added to the values already taken
     * @param timeoutEntry the timeoutEntry
     */
    protected void addValueToTaken(final TimeoutEntry<V> timeoutEntry) {
        takenQue.add(timeoutEntry);
    }

    /**
     * When a value is written under a transaction it is added to the values already written
     * @param valueAsTimeout  the value wrapped in a TimeoutEntry
     */
    protected void addValueToValues(final TimeoutEntry<V> valueAsTimeout) {
        valuesQueue.add(valueAsTimeout);
    }


    /**
     * Gets the Long value of this Transaction
     *
     * @return the Long value
     */
    public TransactionID getTxnId() {
        return txnId;
    }

    /**
     * @param o  the object to check against for equality
     * @return true or false
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        return txnId.equals(that.txnId);

    }

    /**
     * @return  the hash code
     */
    public int hashCode() {
        return txnId.hashCode();
    }


    /**
     * 
     * @return
     */
    public String toString() {
        return "Transaction{" +
                "valuesRead=" + valuesRead +
                ", timeoutQueue=" + timeoutQueue.size() +
                ", takenQue=" + takenQue.size() +
                ", valuesQueue=" + valuesQueue.size() +
                ", parentCollection=" + parentCollection +
                ", txnId=" + txnId +
                '}';
    }

    /**
     * @return 
     * @throws TransactionException any TransactionException
     */
    protected Collection<TimeoutEntry<V>> abort() throws TransactionException {

        //put back any  gets
        final Collection<TimeoutEntry<V>> items =takenQue.toTimeoutCollection();
        parentCollection.addAllTimeouts(items);
        tidyUp();
        return items;
    }

    /**
     * @return 
     * @throws TransactionException any TransactionException
     */
    protected Collection<TimeoutEntry<V>> commit() throws TransactionException {

        final Collection<TimeoutEntry<V>> items = valuesQueue.toTimeoutCollection();
        parentCollection.addAllTimeouts(items);
        tidyUp();
        return items;
    }

    /**
     *
     */
    private void tidyUp() {

        //empty
        takenQue.clear();
        valuesRead.clear();
        valuesQueue.clear();

        //stop reading
        takenQue.terminate();
        valuesQueue.terminate();


    }

}
