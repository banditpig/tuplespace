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
package js.co.uk.tuplespace.events;

/**
 *
 * A SpaceRenderer is responsible for creating a dynamic view of a space. As the space is modified so 
 * too is the view. This view could be a simple text panel or a sophisticated graphical display.  
 * 
 */
public interface SpaceRenderer extends SpaceChangeListener{
    
}
