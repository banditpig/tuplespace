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

package js.co.uk.tuplespace.space;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple space manager. It lazily creates spaces.
 * This is meant to be used to manage in-process spaces.
 *
 */
public class TupleSpaceManager implements SpaceManager {

    private final Map<String, Space> spaceMap = new HashMap<String, Space>();

    /**
     * Returns a Space of the given name.
     *
     * @param name the name of the space
     * @return a Space
     */
    public synchronized Space getSpace(final String name) {

        if (spaceMap.containsKey(name)) {
            return spaceMap.get(name);
        }
        final TupleSpace space = new TupleSpace(name);
        spaceMap.put(name, space);
        return space;
    }

    /**
     * Removes the space with the supplied name
     *
     * @param name the name of the space to remove
     * @return true if the named space was removed successfully - false otherwise
     */
    public synchronized Space removeSpace(final String name) {

        final Space space = spaceMap.remove(name);
        if(space != null){
           space.purgeAllEntries();
            //TODO note that the the TupleSpace will have a thread still running and so will the collection it owns
            //maybe the Space interface needs a terminate method?
        }
        
        return space;
    }

    /**
     * Removes all known spaces
     */
    public synchronized void removeAllSpaces() {
        spaceMap.clear();
    }

    /**
     * @return the number of spaces that this manager knows of
     */
    public synchronized int getSpaceCount() {
        return spaceMap.size();
    }
}
