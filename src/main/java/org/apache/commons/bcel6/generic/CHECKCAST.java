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

import org.apache.commons.bcel6.ExceptionConstants;

/** 
 * CHECKCAST - Check whether object is of given type
 * <PRE>Stack: ..., objectref -&gt; ..., objectref</PRE>
 *
 * @version $Id$
 */
public class CHECKCAST extends CPInstruction implements LoadClass, ExceptionThrower, StackProducer,
        StackConsumer {

    /**
     * Empty constructor needed for the Class.newInstance() statement in
     * Instruction.readInstruction(). Not to be used otherwise.
     */
    CHECKCAST() {
    }


    /** Check whether object is of given type
     * @param index index to class in constant pool
     */
    public CHECKCAST(int index) {
        super(org.apache.commons.bcel6.Constants.CHECKCAST, index);
    }


    /** @return exceptions this instruction may cause
     */
    @Override
    public Class<?>[] getExceptions() {
        return ExceptionConstants.createExceptions(ExceptionConstants.EXCS.EXCS_CLASS_AND_INTERFACE_RESOLUTION,
            ExceptionConstants.CLASS_CAST_EXCEPTION);
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
        v.visitExceptionThrower(this);
        v.visitStackProducer(this);
        v.visitStackConsumer(this);
        v.visitTypedInstruction(this);
        v.visitCPInstruction(this);
        v.visitCHECKCAST(this);
    }
}
