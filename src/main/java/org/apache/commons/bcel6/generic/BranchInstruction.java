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

import org.apache.commons.bcel6.util.ByteSequence;

/** 
 * Abstract super class for branching instructions like GOTO, IFEQ, etc..
 * Branch instructions may have a variable length, namely GOTO, JSR, 
 * LOOKUPSWITCH and TABLESWITCH.
 *
 * @see InstructionList
 * @version $Id$
 */
public abstract class BranchInstruction extends Instruction implements InstructionTargeter {

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @Deprecated
    protected int index; // Branch target relative to this instruction

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @Deprecated
    protected InstructionHandle target; // Target object in instruction list

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @Deprecated
    protected int position; // Byte code offset


    /**
     * Empty constructor needed for the Class.newInstance() statement in
     * Instruction.readInstruction(). Not to be used otherwise.
     */
    BranchInstruction() {
    }


    /** Common super constructor
     * @param opcode Instruction opcode
     * @param target instruction to branch to
     */
    protected BranchInstruction(short opcode, InstructionHandle target) {
        super(opcode, (short) 3);
        setTarget(target);
    }


    /**
     * Dump instruction as byte code to stream out.
     * @param out Output stream
     */
    @Override
    public void dump( DataOutputStream out ) throws IOException {
        out.writeByte(super.getOpcode());
        index = getTargetOffset();
        if (!isValidShort(index)) {
            throw new ClassGenException("Branch target offset too large for short: " + index);
        }
        out.writeShort(index); // May be negative, i.e., point backwards
    }


    /**
     * @param _target branch target
     * @return the offset to  `target' relative to this instruction
     */
    protected int getTargetOffset( InstructionHandle _target ) {
        if (_target == null) {
            throw new ClassGenException("Target of " + super.toString(true)
                    + " is invalid null handle");
        }
        int t = _target.getPosition();
        if (t < 0) {
            throw new ClassGenException("Invalid branch target position offset for "
                    + super.toString(true) + ":" + t + ":" + _target);
        }
        return t - position;
    }


    /**
     * @return the offset to this instruction's target
     */
    protected int getTargetOffset() {
        return getTargetOffset(target);
    }


    /**
     * Called by InstructionList.setPositions when setting the position for every
     * instruction. In the presence of variable length instructions `setPositions'
     * performs multiple passes over the instruction list to calculate the
     * correct (byte) positions and offsets by calling this function.
     *
     * @param offset additional offset caused by preceding (variable length) instructions
     * @param max_offset the maximum offset that may be caused by these instructions
     * @return additional offset caused by possible change of this instruction's length
     */
    protected int updatePosition( int offset, int max_offset ) {
        position += offset;
        return 0;
    }


    /**
     * Long output format:
     *
     * &lt;position in byte code&gt;
     * &lt;name of opcode&gt; "["&lt;opcode number&gt;"]" 
     * "("&lt;length of instruction&gt;")"
     * "&lt;"&lt;target instruction&gt;"&gt;" "@"&lt;branch target offset&gt;
     *
     * @param verbose long/short format switch
     * @return mnemonic for instruction
     */
    @Override
    public String toString( boolean verbose ) {
        String s = super.toString(verbose);
        String t = "null";
        if (verbose) {
            if (target != null) {
                if (target.getInstruction() == this) {
                    t = "<points to itself>";
                } else if (target.getInstruction() == null) {
                    t = "<null instruction!!!?>";
                } else {
                    // I'm more interested in the address of the target then
                    // the instruction located there.
                    //t = target.getInstruction().toString(false); // Avoid circles
                    t = "" + target.getPosition();
                }
            }
        } else {
            if (target != null) {
                index = target.getPosition();
                // index = getTargetOffset();  crashes if positions haven't been set
                // t = "" + (index + position);
                t = "" + index;
            }
        }
        return s + " -> " + t;
    }


    /**
     * Read needed data (e.g. index) from file. Conversion to a InstructionHandle
     * is done in InstructionList(byte[]).
     *
     * @param bytes input stream
     * @param wide wide prefix?
     * @see InstructionList
     */
    @Override
    protected void initFromFile( ByteSequence bytes, boolean wide ) throws IOException {
        super.setLength(3);
        index = bytes.readShort();
    }


    /**
     * @return target offset in byte code
     */
    public final int getIndex() {
        return index;
    }


    /**
     * @return target of branch instruction
     */
    public InstructionHandle getTarget() {
        return target;
    }


    /**
     * Set branch target
     * @param target branch target
     */
    public void setTarget( InstructionHandle target ) {
        notifyTarget(this.target, target, this);
        this.target = target;
    }


    /**
     * Used by BranchInstruction, LocalVariableGen, CodeExceptionGen, LineNumberGen
     */
    static void notifyTarget( InstructionHandle old_ih, InstructionHandle new_ih,
            InstructionTargeter t ) {
        if (old_ih != null) {
            old_ih.removeTargeter(t);
        }
        if (new_ih != null) {
            new_ih.addTargeter(t);
        }
    }


    /**
     * @param old_ih old target
     * @param new_ih new target
     */
    @Override
    public void updateTarget( InstructionHandle old_ih, InstructionHandle new_ih ) {
        if (target == old_ih) {
            setTarget(new_ih);
        } else {
            throw new ClassGenException("Not targeting " + old_ih + ", but " + target);
        }
    }


    /**
     * @return true, if ih is target of this instruction
     */
    @Override
    public boolean containsTarget( InstructionHandle ih ) {
        return target == ih;
    }


    /**
     * Inform target that it's not targeted anymore.
     */
    @Override
    void dispose() {
        setTarget(null);
        index = -1;
        position = -1;
    }


    /**
     * @return the position
     * @since 6.0
     */
    protected int getPosition() {
        return position;
    }


    /**
     * @param position the position to set
     * @return the new position
     * @since 6.0
     */
    protected void setPosition(int position) {
        this.position = position;
    }


    /**
     * @param index the index to set
     * @since 6.0
     */
    protected void setIndex(int index) {
        this.index = index;
    }

}
