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

import java.io.Serializable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper class to allow Ts to be placed in a TimeoutQueue.
 *
 * @param <T> the generic type
 */
public class TimeoutEntry<T> implements Delayed, Serializable {


    /**
     * The item.
     */
    private final T item;

    /**
     * The expire time.
     */
    private  long expireTime;

    /**
     * The eternal.
     */
    private boolean eternal = false;

    /**
     * Instantiates a new timeout entry.
     *
     * @param item     the item to wrap in a TimeoutEntry
     * @param timeOut  the time out period
     * @param timeUnit the time unit
     */
    public TimeoutEntry(final T item, final long timeOut, final TimeUnit timeUnit) {
        this.item = item;
        if ((timeOut == Long.MAX_VALUE) && (timeUnit == TimeUnit.DAYS)) {
            eternal = true;
            expireTime = Long.MAX_VALUE;
        } else {
            expireTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(timeOut, timeUnit);

        }
    }

    /**
     * Getter for the item that this class wraps.
     *
     * @return Value for property 'item'.
     */
    public T getItem() {
        return item;
    }

    /**
     * Gets the delay expressed in the supplied {@link TimeUnit}
     *
     * @param unit the unit
     * @return the delay
     *         {@inheritDoc}
     */
    @Override
    public long getDelay(final TimeUnit unit) {
        if (eternal) return Long.MAX_VALUE;
        final long delay = expireTime - System.nanoTime();
        return unit.convert(delay, TimeUnit.NANOSECONDS);
    }

    /**
     * Compare to.
     *
     * @param other the Delayed to compare to
     * @return the int
     *         {@inheritDoc}
     */
    @Override
    public int compareTo(final Delayed other) {
        final long i = expireTime;
        @SuppressWarnings("unchecked")
        final long j = ((TimeoutEntry<T>) other).expireTime;
        if (i < j) return -1;
        if (i > j) return 1;
        return 0;
    }


}