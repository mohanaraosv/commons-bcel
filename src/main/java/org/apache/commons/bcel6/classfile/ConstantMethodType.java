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
 * and represents a reference to a method type.
 * 
 * @see     Constant
 * @since 6.0
 */
public final class ConstantMethodType extends Constant {

    private int descriptor_index;


    /**
     * Initialize from another object.
     */
    public ConstantMethodType(ConstantMethodType c) {
        this(c.getDescriptorIndex());
    }


    /**
     * Initialize instance from file data.
     *
     * @param file Input stream
     * @throws IOException
     */
    ConstantMethodType(DataInput file) throws IOException {
        this(file.readUnsignedShort());
    }


    public ConstantMethodType(int descriptor_index) {
        super(Constants.CONSTANT_MethodType);
        this.descriptor_index = descriptor_index;
    }


    /**
     * Called by objects that are traversing the nodes of the tree implicitly
     * defined by the contents of a Java class. I.e., the hierarchy of methods,
     * fields, attributes, etc. spawns a tree of objects.
     *
     * @param v Visitor object
     */
    @Override
    public void accept( Visitor v ) {
        // TODO Add .visitMethodType to Visitor interface
    }


    /**
     * Dump name and signature index to file stream in binary format.
     *
     * @param file Output file stream
     * @throws IOException
     */
    @Override
    public final void dump( DataOutputStream file ) throws IOException {
        file.writeByte(super.getTag());
        file.writeShort(descriptor_index);
    }


    public int getDescriptorIndex() {
        return descriptor_index;
    }


    public void setDescriptorIndex(int descriptor_index) {
        this.descriptor_index = descriptor_index;
    }


    /**
     * @return String representation
     */
    @Override
    public final String toString() {
        return super.toString() + "(descriptor_index = " + descriptor_index + ")";
    }
}
