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

import org.apache.commons.bcel6.classfile.ConstantPool;

/**
 * Super class for the GET/PUTxxx family of instructions.
 *
 * @version $Id$
 */
public abstract class FieldInstruction extends FieldOrMethod {

    /**
     * Empty constructor needed for the Class.newInstance() statement in
     * Instruction.readInstruction(). Not to be used otherwise.
     */
    FieldInstruction() {
    }


    /**
     * @param index to constant pool
     */
    protected FieldInstruction(short opcode, int index) {
        super(opcode, index);
    }


    /**
     * @return mnemonic for instruction with symbolic references resolved
     */
    @Override
    public String toString( ConstantPool cp ) {
        return org.apache.commons.bcel6.Constants.getOpcodeName(super.getOpcode()) + " "
                + cp.constantToString(super.getIndex(), org.apache.commons.bcel6.Constants.CONSTANT_Fieldref);
    }


    /** @return size of field (1 or 2)
     */
    protected int getFieldSize( ConstantPoolGen cpg ) {
        return Type.size(Type.getTypeSize(getSignature(cpg)));
    }


    /** @return return type of referenced field
     */
    @Override
    public Type getType( ConstantPoolGen cpg ) {
        return getFieldType(cpg);
    }


    /** @return type of field
     */
    public Type getFieldType( ConstantPoolGen cpg ) {
        return Type.getType(getSignature(cpg));
    }


    /** @return name of referenced field.
     */
    public String getFieldName( ConstantPoolGen cpg ) {
        return getName(cpg);
    }
}
