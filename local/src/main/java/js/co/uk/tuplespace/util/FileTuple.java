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

package js.co.uk.tuplespace.util;

import js.co.uk.tuplespace.tuple.Tuple;

import java.io.*;
import java.util.Arrays;

/**
 * First pass at representing a file object as a tuple.
 */
public class FileTuple implements Tuple {


    /**
     * The bytes.
     */
    private byte[] bytes;

    /**
     * The name.
     */
    private final String name;

    /**
     * The name of the tuple - does not have to be the same as the file name.
     *
     * @param name name of the file
     */
    public FileTuple(final String name) {
        this.name = name;

    }

    /**
     * Opens a file with the supplied name and reads the contents into an internal byte array.
     * The supplied file name is discarded after creating the file for reading.
     *
     * @param fName name of the file
     * @return the number of bytes read when reading the file into a byte array
     * @throws IOException any possible IOException
     */
    public int setFileData(final String fName) throws IOException {
        final File file = new File(fName);
        bytes = getBytesFromFile(file);
        return bytes.length;
    }

    /**
     * Sets the internal byte array to the supplied byte array.
     *
     * @param bytes the byte array
     * @throws IOException any IO exception that occurs
     */
    public void setFileData(final byte[] bytes) throws IOException {
        this.bytes = bytes;

    }

    /**
     * Simplistically two FileTuples are equal if they have the same name.   Probably to costly to compare
     * the byte arrays. Custom extensions of this class could provide a more detailed implementation
     * of equals if needed.
     *
     * @param o the object for comparison
     * @return true or false
     */
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileTuple fileTuple = (FileTuple) o;

        return !(name != null ? !name.equals(fileTuple.name) : fileTuple.name != null);

    }

    /**
     * Hash code.
     *
     * @return the hashcode
     */
    public int hashCode() {
        int result;
        result = (bytes != null ? Arrays.hashCode(bytes) : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    /**
     * Write bytes to file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeBytesToFile() throws IOException {

        final FileOutputStream fos = new FileOutputStream(name);
        fos.write(bytes, 0, bytes.length);
        fos.close();
    }


    /**
     * Gets the bytes.
     *
     * @return the byte array
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Gets the name of the tuple.
     *
     * @return the name of the tuple
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the bytes from file.
     *
     * @param file the file
     * @return the bytes from file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private byte[] getBytesFromFile(final File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }


}
