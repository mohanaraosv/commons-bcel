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

/** 
 * JSR - Jump to subroutine
 *
 * @version $Id$
 */
public class JSR extends JsrInstruction implements VariableLengthInstruction {

    /**
     * Empty constructor needed for the Class.newInstance() statement in
     * Instruction.readInstruction(). Not to be used otherwise.
     */
    JSR() {
    }


    public JSR(InstructionHandle target) {
        super(org.apache.commons.bcel6.Constants.JSR, target);
    }


    /**
     * Dump instruction as byte code to stream out.
     * @param out Output stream
     */
    @Override
    public void dump( DataOutputStream out ) throws IOException {
        super.setIndex(getTargetOffset());
        if (super.getOpcode() == org.apache.commons.bcel6.Constants.JSR) {
            super.dump(out);
        } else { // JSR_W
            super.setIndex(getTargetOffset());
            out.writeByte(super.getOpcode());
            out.writeInt(super.getIndex());
        }
    }


    @Override
    protected int updatePosition( int offset, int max_offset ) {
        int i = getTargetOffset(); // Depending on old position value
        setPosition(getPosition() + offset); // Position may be shifted by preceding expansions
        if (Math.abs(i) >= (Short.MAX_VALUE - max_offset)) { // to large for short (estimate)
            super.setOpcode(org.apache.commons.bcel6.Constants.JSR_W);
            short old_length = (short) super.getLength();
            super.setLength(5);
            return super.getLength() - old_length;
        }
        return 0;
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
        v.visitStackProducer(this);
        v.visitVariableLengthInstruction(this);
        v.visitBranchInstruction(this);
        v.visitJsrInstruction(this);
        v.visitJSR(this);
    }
}
