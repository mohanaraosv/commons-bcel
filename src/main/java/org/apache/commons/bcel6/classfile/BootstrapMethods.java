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
 */

package org.apache.commons.bcel6.classfile;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.bcel6.Constants;

/**
 * This class represents a BootstrapMethods attribute.
 *
 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.23">
 * The class File Format : The BootstrapMethods Attribute</a>
 * @since 6.0
 */
public class BootstrapMethods extends Attribute {

    private BootstrapMethod[] bootstrap_methods;  // TODO this could be made final (setter is not used)

    /**
     * Initialize from another object. Note that both objects use the same
     * references (shallow copy). Use clone() for a physical copy.
     */
    public BootstrapMethods(BootstrapMethods c) {
        this(c.getNameIndex(), c.getLength(), c.getBootstrapMethods(), c.getConstantPool());
    }


    /**
     * @param name_index Index in constant pool to CONSTANT_Utf8
     * @param length Content length in bytes
     * @param bootstrap_methods array of bootstrap methods
     * @param constant_pool Array of constants
     */
    public BootstrapMethods(int name_index, int length, BootstrapMethod[] bootstrap_methods, ConstantPool constant_pool) {
        super(Constants.ATTR_BOOTSTRAP_METHODS, name_index, length, constant_pool);
        this.bootstrap_methods = bootstrap_methods;
    }

    /**
     * Construct object from Input stream.
     *
     * @param name_index Index in constant pool to CONSTANT_Utf8
     * @param length Content length in bytes
     * @param input Input stream
     * @param constant_pool Array of constants
     * @throws IOException
     */
    BootstrapMethods(int name_index, int length, DataInput input, ConstantPool constant_pool) throws IOException {
        this(name_index, length, (BootstrapMethod[]) null, constant_pool);

        int num_bootstrap_methods = input.readUnsignedShort();
        bootstrap_methods = new BootstrapMethod[num_bootstrap_methods];
        for (int i = 0; i < num_bootstrap_methods; i++) {
            bootstrap_methods[i] = new BootstrapMethod(input);
        }
    }

    /**
     * @return array of bootstrap method "records"
     */
    public final BootstrapMethod[] getBootstrapMethods() {
        return bootstrap_methods;
    }

    /**
     * @param bootstrap_methods the array of bootstrap methods
     */
    public final void setBootstrapMethods(BootstrapMethod[] bootstrap_methods) {
        this.bootstrap_methods = bootstrap_methods;
    }

    /**
     * @param v Visitor object
     */
    @Override
    public void accept(Visitor v) {
        v.visitBootstrapMethods(this);
    }

    /**
     * @return deep copy of this attribute
     */
    @Override
    public BootstrapMethods copy(ConstantPool _constant_pool) {
        BootstrapMethods c = (BootstrapMethods) clone();
        c.bootstrap_methods = new BootstrapMethod[bootstrap_methods.length];

        for (int i = 0; i < bootstrap_methods.length; i++) {
            c.bootstrap_methods[i] = bootstrap_methods[i].copy();
        }
        c.setConstantPool(_constant_pool);
        return c;
    }

    /**
     * Dump bootstrap methods attribute to file stream in binary format.
     *
     * @param file Output file stream
     * @throws IOException
     */
    @Override
    public final void dump(DataOutputStream file) throws IOException {
        super.dump(file);

        file.writeShort(bootstrap_methods.length);
        for (BootstrapMethod bootstrap_method : bootstrap_methods) {
            bootstrap_method.dump(file);
        }
    }

    /**
     * @return String representation.
     */
    @Override
    public final String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("BootstrapMethods(");
        buf.append(bootstrap_methods.length);
        buf.append("):\n");
        for (int i = 0; i < bootstrap_methods.length; i++) {
            buf.append("  ").append(i).append(": ");
            buf.append(bootstrap_methods[i].toString(super.getConstantPool())).append("\n");
        }
        return buf.toString();
    }
}
