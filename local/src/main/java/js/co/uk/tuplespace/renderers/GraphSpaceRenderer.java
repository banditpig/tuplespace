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
package js.co.uk.tuplespace.renderers;


import javax.swing.JFrame;

import com.touchgraph.graphlayout.GLPanel;
import js.co.uk.tuplespace.events.EventHint;
import js.co.uk.tuplespace.events.SpaceChange;
import js.co.uk.tuplespace.events.SpaceChangeEvent;
import js.co.uk.tuplespace.events.SpaceRenderer;
import js.co.uk.tuplespace.store.TransactionID;
/**
 * 
 * Experimental view of a space. Bug in the adding into the the graphView, it doesn't handle 
 * duplicates.
 */
public class GraphSpaceRenderer implements SpaceRenderer {

    private GLPanel graphView;

    public GraphSpaceRenderer() {

        graphView = new GLPanel();
        JFrame fr = new JFrame();
        fr.add("Center", graphView);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setSize(500, 500);
        fr.setVisible(true);
    }

    /**
     * Handles an incoming change to the space
     *
     * @param spaceChangeEvent
     */

    public void spaceChanged(SpaceChange spaceChangeEvent) {

        final EventHint hint = spaceChangeEvent.getEventHint();
        final TransactionID txnID = spaceChangeEvent.getTransactionID();

        if (spaceChangeEvent instanceof SpaceChangeEvent) {
            if (txnID == null) {

                //its a simple action without a transaction
                if (spaceChangeEvent.getEventHint() == EventHint.TUPLE_ADDED) {
                    graphView.addTupleNode(spaceChangeEvent.getSpaceName(), spaceChangeEvent.getTuple().toString());
                } else if (spaceChangeEvent.getEventHint() == EventHint.TUPLE_REMOVED && spaceChangeEvent.getTuple() != null) {
                    graphView.removeTupleNode(spaceChangeEvent.getSpaceName(), spaceChangeEvent.getTuple().toString());
                } else if (spaceChangeEvent.getEventHint() == EventHint.TUPLE_TIMEDOUT) {
                    graphView.removeTupleNode(spaceChangeEvent.getSpaceName(), spaceChangeEvent.getTuple().toString());
                } else if (spaceChangeEvent.getEventHint() == EventHint.SPACE_CREATED) {
                    graphView.addSpaceNode(spaceChangeEvent.getSpaceName());
                } else if (spaceChangeEvent.getEventHint() == EventHint.SPACE_PURGED) {
                    graphView.removeAllNodesAndEdgesForSpace(spaceChangeEvent.getSpaceName());
                }
            } else {
                //have a transaction
                if (spaceChangeEvent.getEventHint() == EventHint.TUPLE_ADDED) {
                    graphView.addTupleNode(spaceChangeEvent.getSpaceName() + txnID, spaceChangeEvent.getTuple().toString());
                } else if (spaceChangeEvent.getEventHint() == EventHint.TUPLE_REMOVED && spaceChangeEvent.getTuple() != null) {
                    graphView.removeTupleNode(spaceChangeEvent.getSpaceName() + txnID, spaceChangeEvent.getTuple().toString());
                } else if (spaceChangeEvent.getEventHint() == EventHint.TUPLE_TIMEDOUT) {
                    //THIS WONT HAPPEN??  :)
                    graphView.removeTupleNode(spaceChangeEvent.getSpaceName() + txnID, spaceChangeEvent.getTuple().toString());
                }
            }
        } else {

            if (hint == EventHint.TXN_CREATED) {
                graphView.addSpaceNode(spaceChangeEvent.getSpaceName() + txnID);

            } else if (hint == EventHint.TXN_ABORTED) {
                graphView.abortTxn(spaceChangeEvent.getSpaceName() + txnID, spaceChangeEvent.getSpaceName());
            } else if (hint == EventHint.TXN_COMMITTED) {
                graphView.commitTxn(spaceChangeEvent.getSpaceName() + txnID, spaceChangeEvent.getSpaceName());
            }


        }

    }
}
