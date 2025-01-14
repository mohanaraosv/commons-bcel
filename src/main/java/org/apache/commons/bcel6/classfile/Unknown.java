/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.commons.bcel6.classfile;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.bcel6.Constants;

/**
 * This class represents a reference to an unknown (i.e.,
 * application-specific) attribute of a class.  It is instantiated from the
 * {@link Attribute#readAttribute(java.io.DataInput, ConstantPool)} method.
 * Applications that need to read in application-specific attributes should create an
 * {@link UnknownAttributeReader} implementation and attach it via
 * {@link Attribute#addAttributeReader(String, UnknownAttributeReader)}.

 *
 * @version $Id$
 * @see Attribute
 * @see UnknownAttributeReader
 */
public final class Unknown extends Attribute {

    private byte[] bytes;
    private final String name;
    private static final Map<String, Unknown> unknown_attributes = new HashMap<>();


    /** @return array of unknown attributes, but just one for each kind.
     */
    static Unknown[] getUnknownAttributes() {
        Unknown[] unknowns = new Unknown[unknown_attributes.size()];
        unknown_attributes.values().toArray(unknowns);
        unknown_attributes.clear();
        return unknowns;
    }


    /**
     * Initialize from another object. Note that both objects use the same
     * references (shallow copy). Use clone() for a physical copy.
     */
    public Unknown(Unknown c) {
        this(c.getNameIndex(), c.getLength(), c.getBytes(), c.getConstantPool());
    }


    /**
     * Create a non-standard attribute.
     *
     * @param name_index Index in constant pool
     * @param length Content length in bytes
     * @param bytes Attribute contents
     * @param constant_pool Array of constants
     */
    public Unknown(int name_index, int length, byte[] bytes, ConstantPool constant_pool) {
        super(Constants.ATTR_UNKNOWN, name_index, length, constant_pool);
        this.bytes = bytes;
        name = ((ConstantUtf8) constant_pool.getConstant(name_index, Constants.CONSTANT_Utf8))
                .getBytes();
        unknown_attributes.put(name, this);
    }


    /**
     * Construct object from input stream.
     * 
     * @param name_index Index in constant pool
     * @param length Content length in bytes
     * @param input Input stream
     * @param constant_pool Array of constants
     * @throws IOException
     */
    Unknown(int name_index, int length, DataInput input, ConstantPool constant_pool)
            throws IOException {
        this(name_index, length, (byte[]) null, constant_pool);
        if (length > 0) {
            bytes = new byte[length];
            input.readFully(bytes);
        }
    }


    /**
     * Called by objects that are traversing the nodes of the tree implicitely
     * defined by the contents of a Java class. I.e., the hierarchy of methods,
     * fields, attributes, etc. spawns a tree of objects.
     *
     * @param v Visitor object
     */
    @Override
    public void accept( Visitor v ) {
        v.visitUnknown(this);
    }


    /**
     * Dump unknown bytes to file stream.
     *
     * @param file Output file stream
     * @throws IOException
     */
    @Override
    public final void dump( DataOutputStream file ) throws IOException {
        super.dump(file);
        if (super.getLength() > 0) {
            file.write(bytes, 0, super.getLength());
        }
    }


    /**
     * @return data bytes.
     */
    public final byte[] getBytes() {
        return bytes;
    }


    /**
     * @return name of attribute.
     */
    @Override
    public final String getName() {
        return name;
    }


    /**
     * @param bytes the bytes to set
     */
    public final void setBytes( byte[] bytes ) {
        this.bytes = bytes;
    }


    /**
     * @return String representation.
     */
    @Override
    public final String toString() {
        if (super.getLength() == 0 || bytes == null) {
            return "(Unknown attribute " + name + ")";
        }
        String hex;
        if (super.getLength() > 10) {
            byte[] tmp = new byte[10];
            System.arraycopy(bytes, 0, tmp, 0, 10);
            hex = Utility.toHexString(tmp) + "... (truncated)";
        } else {
            hex = Utility.toHexString(bytes);
        }
        return "(Unknown attribute " + name + ": " + hex + ")";
    }


    /**
     * @return deep copy of this attribute
     */
    @Override
    public Attribute copy( ConstantPool _constant_pool ) {
        Unknown c = (Unknown) clone();
        if (bytes != null) {
            c.bytes = new byte[bytes.length];
            System.arraycopy(bytes, 0, c.bytes, 0, bytes.length);
        }
        c.setConstantPool(_constant_pool);
        return c;
    }
}
