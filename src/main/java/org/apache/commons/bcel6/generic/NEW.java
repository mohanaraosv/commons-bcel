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
 * NEW - Create new object
 * <PRE>Stack: ... -&gt; ..., objectref</PRE>
 *
 * @version $Id$
 */
public class NEW extends CPInstruction implements LoadClass, AllocationInstruction,
        ExceptionThrower, StackProducer {

    /**
     * Empty constructor needed for the Class.newInstance() statement in
     * Instruction.readInstruction(). Not to be used otherwise.
     */
    NEW() {
    }


    public NEW(int index) {
        super(org.apache.commons.bcel6.Constants.NEW, index);
    }


    @Override
    public Class<?>[] getExceptions() {
        return ExceptionConstants.createExceptions(ExceptionConstants.EXCS.EXCS_CLASS_AND_INTERFACE_RESOLUTION,
            ExceptionConstants.ILLEGAL_ACCESS_ERROR,
            ExceptionConstants.INSTANTIATION_ERROR);
    }


    @Override
    public ObjectType getLoadClassType( ConstantPoolGen cpg ) {
        return (ObjectType) getType(cpg);
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
        v.visitStackProducer(this);
        v.visitTypedInstruction(this);
        v.visitCPInstruction(this);
        v.visitNEW(this);
    }
}
