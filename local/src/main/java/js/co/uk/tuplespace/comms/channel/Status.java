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

import js.co.uk.tuplespace.tuple.Tuple;

/**
 * This is used to encapsulate the details of a Channel. It holds the numeric
 * values for the head and tail and also the name of the channel. For matching
 * purposes it also has booleans indication empty and full.
 *
 */
public class Status implements Tuple {

    private Integer head = 1;
    private Integer tail = 1;
    private final String name;
    private Boolean full = null;
    private Boolean empty = null;
    private Integer limit = null;

    public static Status createMatchTemplateForNotFull(final String name) {

        final Status status = new Status(name);
        status.setHead(null);
        status.setTail(null);
        status.setEmpty(null);
        status.setLimit(null);
        status.setFull(false);

        return status;

    }

    public static Status createMatchTemplateForNotEmpty(final String name) {

        final Status status = new Status(name);
        status.setHead(null);
        status.setTail(null);
        status.setFull(null);
        status.setLimit(null);
        status.setEmpty(false);
        return status;

    }

    static Status createMatchTemplateForDontCare(String name) {
        final Status status = new Status(name);
        status.setHead(null);
        status.setTail(null);
        status.setFull(null);
        status.setEmpty(null);
        status.setLimit(null);
        return status;
    }

    /**
     * Create a Status with the supplied name
     *
     * @param name
     */
    public Status(final String name) {

        this.name = name;
        empty = true;
        full = false;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
       
    }

    public Boolean isFull() {
        return full;
    }

    public void setFull(final Boolean full) {
        this.full = full;
    }

    public Boolean isEmpty() {
        return empty;
    }

    public void setEmpty(final Boolean empty) {
        this.empty = empty;
    }

    /**
     * Calculates tail - head
     *
     * @return
     */
    public int tailHeadDifference() {
        return tail - head;
    }

    /**
     * Adds one to the head value
     */
    public void incHead() {
        head++;
    }

    /**
     * Adds one to the tail value
     */
    public void incTail() {
        tail++;
    }

    public Integer getHead() {
        return head;
    }

    public void setHead(final Integer head) {
        this.head = head;
    }

    public Integer getTail() {
        return tail;
    }

    public void setTail(final Integer tail) {
        this.tail = tail;
    }

    @Override
    public String toString() {
        return "Status{" + "head=" + head + ", tail=" + tail + ", name=" + name + ", full=" + full + ", empty=" + empty + ", limit=" + limit + '}';
    }

    
}
