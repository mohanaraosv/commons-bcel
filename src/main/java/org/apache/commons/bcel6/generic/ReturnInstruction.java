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

import org.apache.commons.bcel6.Constants;
import org.apache.commons.bcel6.ExceptionConstants;

/**
 * Super class for the xRETURN family of instructions.
 *
 * @version $Id$
 */
public abstract class ReturnInstruction extends Instruction implements ExceptionThrower,
        TypedInstruction, StackConsumer {

    /**
     * Empty constructor needed for the Class.newInstance() statement in
     * Instruction.readInstruction(). Not to be used otherwise.
     */
    ReturnInstruction() {
    }


    /**
     * @param opcode of instruction
     */
    protected ReturnInstruction(short opcode) {
        super(opcode, (short) 1);
    }


    public Type getType() {
        final short _opcode = super.getOpcode();
        switch (_opcode) {
            case Constants.IRETURN:
                return Type.INT;
            case Constants.LRETURN:
                return Type.LONG;
            case Constants.FRETURN:
                return Type.FLOAT;
            case Constants.DRETURN:
                return Type.DOUBLE;
            case Constants.ARETURN:
                return Type.OBJECT;
            case Constants.RETURN:
                return Type.VOID;
            default: // Never reached
                throw new ClassGenException("Unknown type " + _opcode);
        }
    }


    @Override
    public Class<?>[] getExceptions() {
        return new Class[] {
            ExceptionConstants.ILLEGAL_MONITOR_STATE
        };
    }


    /** @return type associated with the instruction
     */
    @Override
    public Type getType( ConstantPoolGen cp ) {
        return getType();
    }
}
