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

/**
 * The Interface Transaction.
 * @param <V> 
 */
public interface Transactional<V> {
    /**
     * Start the transaction. Any other actions that are part of the transaction must
     * use the TransactionID that this method returns. If the transaction is neither
     * committed nor aborted before the timeOut then the transaction is aborted automatically
     * after timeOut milliseconds.
     *
     * @param timeOut the allowed time (in milliseconds) before the transaction is automatically aborted
     * @return a TransactionID to identify this transaction for subsequent use
     * @throws TransactionException a TransactionException
     */
    public TransactionID beginTxn(final Long timeOut) throws TransactionException;


    /**
     *  All actions done under this transaction are aborted.
     * @param txnId
     * @return  Collection<TimeoutEntry<V>>
     * @throws TransactionException
     */
    public Collection<TimeoutEntry<V>> abortTxn(final TransactionID txnId) throws TransactionException;

    /**
     * All actions done under this transaction are committed to the space as one atomic operation.
     * @param txnId
     * @return  Collection<TimeoutEntry<V>>
     * @throws TransactionException
     */
    public Collection<TimeoutEntry<V>> commitTxn(final TransactionID txnId) throws TransactionException;


}
