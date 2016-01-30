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


/**
 * Defines the nature of a SpaceManager.
 * Its role is to manage the creation, access to and removal of Space objects.
 */
public interface SpaceManager {

    
    /**
     * Returns a Space of the given name.
     *
     * @param name  the name of the space
     * @return a Space
     */
    public Space getSpace(final String name);

    /**
     *  Removes the space with the supplied name
     *
     * @param name the name of the space to remove
     * @return true if the named space was removed succesfully - false otherwise
     */
    public Space removeSpace(final String name);

    /**
     * Removes all known spaces
     *
     */
    public void removeAllSpaces();


    /**
     *
     * @return   the number of spaces that this manager knows of
     */
    public int getSpaceCount();
}
