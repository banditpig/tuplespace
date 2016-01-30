/*
 * ******************************************************************************
 *  * Copyright (c) 2012. Mike Houghton.
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
package js.co.uk.tuplespace.comms.channel;

import java.util.logging.Level;
import java.util.logging.Logger;
import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.Tuple;

/**
 * Channel implementation that creates a named channel in a given space. Channel
 * inserts and removals are all transaction based so a timeout for these
 * transactions is required in the constructor. If a timeout is not supplied
 * then a default value of 10 seconds is used. The channel can, optionally, be
 * bounded - this bound is an upper limit to the number of messages that are
 * allowed in the channel. Any attempt to insert into a channel that has reached
 * its limit will block until there is room. However this blocking may be
 * interrupted if the waiting period has exceeded the timeout period allocated
 * to the channel.
 *
 */
public class BasicChannel implements Channel<Tuple> {

    private static final long DEFAULT_TXN_TIMEOUT = 10000; //10 seconds
    private final long txnTimeout;
    private final Space space;
    private final String name;
    private final Status statusTemplateNotEmpty;
    private final Status statusTemplateNotFull;
    private final Status statusTemplateDontCare;
    private final boolean bounded;

    /**
     * Creates a bounded channel with the supplied values.
     * 
     * @param space the space that has the channel
     * @param name the name
     * @param txnTimeout timeout for transactions
     * @param channelLimit the maximum number of entries that this channel
     * allows
     */
    public BasicChannel(final Space space, final String name, final long txnTimeout, final Integer channelLimit) {

        if ((channelLimit != null) && channelLimit <= 0) {
            throw new IllegalArgumentException("Bounded channel must have a limit of at least 1, not the supplied value of " + channelLimit);
        }
        this.space = space;
        this.name = name;
        this.txnTimeout = txnTimeout;
        this.bounded = channelLimit == null ? false : true;
        //TODO should I check for an existing channel of that name?
        final Status status = new Status(name);
        status.setLimit(channelLimit);
        space.put(status);

        statusTemplateNotEmpty = Status.createMatchTemplateForNotEmpty(name);
        statusTemplateNotFull = Status.createMatchTemplateForNotFull(name);
        statusTemplateDontCare = Status.createMatchTemplateForDontCare(name);
    }

    /**
     * An unbounded channel in the supplied space named with the given name and
     * transaction timeouts
     *
     * @param space the space that has the channel
     * @param name the  name
     * @param txnTimeout timeout for transactions
     */
    public BasicChannel(final Space space, final String name, final long txnTimeout) {

        this(space, name, txnTimeout, null);
    }

     /**
     * An unbounded channel in the supplied space named with the given name. The 
     * timeout for transactions is the default value.
     *
     * @param space the space that has the channel
     * @param name the  name
    
     */
    public BasicChannel(final Space space, final String name) {
        this(space, name, DEFAULT_TXN_TIMEOUT, null);
    }

    /**
     * A channel bounded with a limit, having the given name in the supplied spacer and using 
     * the  default value for transaction timeouts.
     * @param space
     * @param name
     * @param channelLimit 
     */
    public BasicChannel(final Space space, final String name, final Integer channelLimit) {
        this(space, name, DEFAULT_TXN_TIMEOUT, channelLimit);
    }

    /**
     *
     * Inserts the supplied tuple at the end of the channel. If the channel is bounded then this method will block
     * until there is room or a transaction timeout occurs, in which case a ChannelTimeoutException will be thrown.
     *
     * @param tuple
     * @throws ChannelTimeoutException
     */
    @Override
    public void insert(final Tuple tuple) throws ChannelTimeoutException{


        try {

            final Integer index = getIndexForNewMessage();
            final ChannelMessage message = new ChannelMessage(name, index, tuple);
            space.put(message);

        } catch (TransactionException ex) {
            Logger.getLogger(BasicChannel.class.getName()).log(Level.SEVERE, null, ex);
            throw new ChannelTimeoutException("Txn timeout while waiting to insert into channel " + name, ex.getCause());
        }

    }

    /**
     * Removes the head of the channel. This will block whilst the channel is
     * empty and will throw a ChannelTimeoutException if the channel remains
     * empty during the timeout period set for this channel.
     *
     * @return
     * @throws ChannelTimeoutException
     */
    @Override
    public Tuple remove() throws ChannelTimeoutException {

        try {

            final TransactionID txn = space.beginTxn(txnTimeout);
            //wait for a not empty channel, (but this is within a txn so might timeout)
            final Status status = (Status) space.get(statusTemplateNotEmpty, txn);

            //get mesage at the head and make head point to the  next position
            //then replace it
            final Tuple removedTuple = removeFromPosition(status.getHead(), txn);
            status.incHead();


            if (status.tailHeadDifference() == 0) {
                //it's empty
                status.setEmpty(true);
                status.setFull(false);

            }
            space.put(status, txn);
            space.commitTxn(txn);

            return removedTuple;

        } catch (TransactionException ex) {
            Logger.getLogger(BasicChannel.class.getName()).log(Level.SEVERE, "Txn timeout while waiting to remove", ex);
            throw new ChannelTimeoutException("Txn timeout while waiting to remove head of channel " + name, ex.getCause());
        }


    }

    /**
     *
     * @return @throws TransactionException
     */
    private int getIndexForNewMessage() throws TransactionException {

        final TransactionID txn = space.beginTxn(txnTimeout);
        final Status status = (Status) space.get(statusTemplateNotFull, txn);

        //get the  tail of this channel  
        final int pos = status.getTail();
        //inc tail to point to the  next empty position and put it back under txn
        status.incTail();
        status.setEmpty(false);



        if (bounded && status.tailHeadDifference() == status.getLimit()) {
            //bounded channel is full, not just not empty
            status.setFull(true);

        }
        space.put(status, txn);
        space.commitTxn(txn);

        return pos;

    }

    /**
     *
     * @param position
     * @param txn
     * @return
     * @throws TransactionException
     */
    private Tuple removeFromPosition(final Integer position, final TransactionID txn) throws TransactionException {

        final ChannelMessage template = new ChannelMessage(name, position, null);
        //this won't block properly because the txn will timeout if theres nothing to read!!
        //actually that might be better!
        final ChannelMessage message = (ChannelMessage) space.get(template, txn);
        return message.getTuple();
    }
}
