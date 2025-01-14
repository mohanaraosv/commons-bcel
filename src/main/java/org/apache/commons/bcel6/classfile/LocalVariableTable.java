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
 * This class represents colection of local variables in a
 * method. This attribute is contained in the <em>Code</em> attribute.
 *
 * @version $Id$
 * @see     Code
 * @see LocalVariable
 */
public class LocalVariableTable extends Attribute {

    private LocalVariable[] local_variable_table; // variables


    /**
     * Initialize from another object. Note that both objects use the same
     * references (shallow copy). Use copy() for a physical copy.
     */
    public LocalVariableTable(LocalVariableTable c) {
        this(c.getNameIndex(), c.getLength(), c.getLocalVariableTable(), c.getConstantPool());
    }


    /**
     * @param name_index Index in constant pool to `LocalVariableTable'
     * @param length Content length in bytes
     * @param local_variable_table Table of local variables
     * @param constant_pool Array of constants
     */
    public LocalVariableTable(int name_index, int length, LocalVariable[] local_variable_table,
            ConstantPool constant_pool) {
        super(Constants.ATTR_LOCAL_VARIABLE_TABLE, name_index, length, constant_pool);
        this.local_variable_table = local_variable_table;
    }


    /**
     * Construct object from input stream.
     * @param name_index Index in constant pool
     * @param length Content length in bytes
     * @param input Input stream
     * @param constant_pool Array of constants
     * @throws IOException
     */
    LocalVariableTable(int name_index, int length, DataInput input, ConstantPool constant_pool)
            throws IOException {
        this(name_index, length, (LocalVariable[]) null, constant_pool);
        int local_variable_table_length = input.readUnsignedShort();
        local_variable_table = new LocalVariable[local_variable_table_length];
        for (int i = 0; i < local_variable_table_length; i++) {
            local_variable_table[i] = new LocalVariable(input, constant_pool);
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
        v.visitLocalVariableTable(this);
    }


    /**
     * Dump local variable table attribute to file stream in binary format.
     *
     * @param file Output file stream
     * @throws IOException
     */
    @Override
    public final void dump( DataOutputStream file ) throws IOException {
        super.dump(file);
        file.writeShort(local_variable_table.length);
        for (LocalVariable variable : local_variable_table) {
            variable.dump(file);
        }
    }


    /**
     * @return Array of local variables of method.
     */
    public final LocalVariable[] getLocalVariableTable() {
        return local_variable_table;
    }


    /** 
     * 
     * @param index the variable slot
     * @param pc the current pc that this variable is alive
     * 
     * @return the LocalVariable that matches or null if not found
     */
    public final LocalVariable getLocalVariable( int index, int pc ) {
        for (LocalVariable variable : local_variable_table) {
            if (variable.getIndex() == index) {
                int start_pc = variable.getStartPC();
                int end_pc = start_pc + variable.getLength();
                if ((pc >= start_pc) && (pc <= end_pc)) {
                    return variable;
                }
            }
        }
        return null;
    }


    public final void setLocalVariableTable( LocalVariable[] local_variable_table ) {
        this.local_variable_table = local_variable_table;
    }


    /**
     * @return String representation.
     */
    @Override
    public final String toString() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < local_variable_table.length; i++) {
            buf.append(local_variable_table[i]);
            if (i < local_variable_table.length - 1) {
                buf.append('\n');
            }
        }
        return buf.toString();
    }


    /**
     * @return deep copy of this attribute
     */
    @Override
    public Attribute copy( ConstantPool _constant_pool ) {
        LocalVariableTable c = (LocalVariableTable) clone();
        c.local_variable_table = new LocalVariable[local_variable_table.length];
        for (int i = 0; i < local_variable_table.length; i++) {
            c.local_variable_table[i] = local_variable_table[i].copy();
        }
        c.setConstantPool(_constant_pool);
        return c;
    }


    public final int getTableLength() {
        return local_variable_table == null ? 0 : local_variable_table.length;
    }
}
