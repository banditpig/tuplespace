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
 * Wrapper class to put Tuples into a Channel

 */
public class ChannelMessage implements Tuple{
    
    
    private String channelName;
    private Integer index;
    private Tuple tuple;

    /**
     * Creates a ChannelMessage with the supplied values
     * @param channelName
     * @param index
     * @param message
     */
    public ChannelMessage(final String  channelName, final Integer index, final Tuple tuple) {
        this.channelName = channelName;
        this.index = index;
        this.tuple = tuple;
    }

    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        return "ChannelMessage{" + "channelName=" + channelName + ", index=" + index + ", message=" + tuple + '}';
    }

    /**
     * 
     * @return
     */
    public Tuple getTuple() {
        return tuple;
    }
    
    
    
}
