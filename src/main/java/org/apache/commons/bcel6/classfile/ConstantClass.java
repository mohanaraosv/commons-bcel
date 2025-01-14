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

import org.apache.commons.bcel6.Constants;

/** 
 * This class is derived from the abstract {@link Constant}
 * and represents a reference to a (external) class.
 *
 * @version $Id$
 * @see     Constant
 */
public final class ConstantClass extends Constant implements ConstantObject {

    private int name_index; // Identical to ConstantString except for the name


    /**
     * Initialize from another object.
     */
    public ConstantClass(ConstantClass c) {
        this(c.getNameIndex());
    }


    /**
     * Initialize instance from file data.
     *
     * @param file Input stream
     * @throws IOException
     */
    ConstantClass(DataInput file) throws IOException {
        this(file.readUnsignedShort());
    }


    /**
     * @param name_index Name index in constant pool.  Should refer to a
     * ConstantUtf8.
     */
    public ConstantClass(int name_index) {
        super(Constants.CONSTANT_Class);
        this.name_index = name_index;
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
        v.visitConstantClass(this);
    }


    /** 
     * Dump constant class to file stream in binary format.
     *
     * @param file Output file stream
     * @throws IOException
     */
    @Override
    public final void dump( DataOutputStream file ) throws IOException {
        file.writeByte(super.getTag());
        file.writeShort(name_index);
    }


    /**
     * @return Name index in constant pool of class name.
     */
    public final int getNameIndex() {
        return name_index;
    }


    /**
     * @param name_index the name index in the constant pool of this Constant Class
     */
    public final void setNameIndex( int name_index ) {
        this.name_index = name_index;
    }


    /** @return String object
     */
    @Override
    public Object getConstantValue( ConstantPool cp ) {
        Constant c = cp.getConstant(name_index, Constants.CONSTANT_Utf8);
        return ((ConstantUtf8) c).getBytes();
    }


    /** @return dereferenced string
     */
    public String getBytes( ConstantPool cp ) {
        return (String) getConstantValue(cp);
    }


    /**
     * @return String representation.
     */
    @Override
    public final String toString() {
        return super.toString() + "(name_index = " + name_index + ")";
    }
}
