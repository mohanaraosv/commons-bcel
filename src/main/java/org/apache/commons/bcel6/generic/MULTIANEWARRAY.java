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
package org.apache.commons.bcel6.generic;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.bcel6.ExceptionConstants;
import org.apache.commons.bcel6.classfile.ConstantPool;
import org.apache.commons.bcel6.util.ByteSequence;

/** 
 * MULTIANEWARRAY - Create new mutidimensional array of references
 * <PRE>Stack: ..., count1, [count2, ...] -&gt; ..., arrayref</PRE>
 *
 * @version $Id$
 */
public class MULTIANEWARRAY extends CPInstruction implements LoadClass, AllocationInstruction,
        ExceptionThrower {

    private short dimensions;


    /**
     * Empty constructor needed for the Class.newInstance() statement in
     * Instruction.readInstruction(). Not to be used otherwise.
     */
    MULTIANEWARRAY() {
    }


    public MULTIANEWARRAY(int index, short dimensions) {
        super(org.apache.commons.bcel6.Constants.MULTIANEWARRAY, index);
        if (dimensions < 1) {
            throw new ClassGenException("Invalid dimensions value: " + dimensions);
        }
        this.dimensions = dimensions;
        super.setLength(4);
    }


    /**
     * Dump instruction as byte code to stream out.
     * @param out Output stream
     */
    @Override
    public void dump( DataOutputStream out ) throws IOException {
        out.writeByte(super.getOpcode());
        out.writeShort(super.getIndex());
        out.writeByte(dimensions);
    }


    /**
     * Read needed data (i.e., no. dimension) from file.
     */
    @Override
    protected void initFromFile( ByteSequence bytes, boolean wide ) throws IOException {
        super.initFromFile(bytes, wide);
        dimensions = bytes.readByte();
        super.setLength(4);
    }


    /**
     * @return number of dimensions to be created
     */
    public final short getDimensions() {
        return dimensions;
    }


    /**
     * @return mnemonic for instruction
     */
    @Override
    public String toString( boolean verbose ) {
        return super.toString(verbose) + " " + super.getIndex() + " " + dimensions;
    }


    /**
     * @return mnemonic for instruction with symbolic references resolved
     */
    @Override
    public String toString( ConstantPool cp ) {
        return super.toString(cp) + " " + dimensions;
    }


    /**
     * Also works for instructions whose stack effect depends on the
     * constant pool entry they reference.
     * @return Number of words consumed from stack by this instruction
     */
    @Override
    public int consumeStack( ConstantPoolGen cpg ) {
        return dimensions;
    }


    @Override
    public Class<?>[] getExceptions() {
        return ExceptionConstants.createExceptions(ExceptionConstants.EXCS.EXCS_CLASS_AND_INTERFACE_RESOLUTION,
            ExceptionConstants.ILLEGAL_ACCESS_ERROR,
            ExceptionConstants.NEGATIVE_ARRAY_SIZE_EXCEPTION);
    }


    @Override
    public ObjectType getLoadClassType( ConstantPoolGen cpg ) {
        Type t = getType(cpg);
        if (t instanceof ArrayType) {
            t = ((ArrayType) t).getBasicType();
        }
        return (t instanceof ObjectType) ? (ObjectType) t : null;
    }


    /**
     * Call corresponding visitor method(s). The order is:
     * Call visitor methods of implemented interfaces first, then
     * call methods according to the class hierarchy in descending order,
     * i.e., the most specific visitXXX() call comes last.
     *
     * @param v Visitor object
     */
    @Override
    public void accept( Visitor v ) {
        v.visitLoadClass(this);
        v.visitAllocationInstruction(this);
        v.visitExceptionThrower(this);
        v.visitTypedInstruction(this);
        v.visitCPInstruction(this);
        v.visitMULTIANEWARRAY(this);
    }
}
