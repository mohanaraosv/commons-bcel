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

import org.apache.commons.bcel6.classfile.ConstantDouble;
import org.apache.commons.bcel6.classfile.ConstantFloat;
import org.apache.commons.bcel6.classfile.ConstantInteger;
import org.apache.commons.bcel6.classfile.ConstantLong;
import org.apache.commons.bcel6.classfile.ConstantUtf8;
import org.apache.commons.bcel6.classfile.ElementValue;
import org.apache.commons.bcel6.classfile.SimpleElementValue;

/**
 * @since 6.0
 */
public class SimpleElementValueGen extends ElementValueGen
{
    // For primitive types and string type, this points to the value entry in
    // the cpGen
    // For 'class' this points to the class entry in the cpGen
    private int idx;

    // ctors for each supported type... type could be inferred but for now lets
    // force it to be passed
    /**
     * Protected ctor used for deserialization, doesn't *put* an entry in the
     * constant pool, assumes the one at the supplied index is correct.
     */
    protected SimpleElementValueGen(int type, int idx, ConstantPoolGen cpGen)
    {
        super(type, cpGen);
        this.idx = idx;
    }

    public SimpleElementValueGen(int type, ConstantPoolGen cpGen, int value)
    {
        super(type, cpGen);
        idx = getConstantPool().addInteger(value);
    }

    public SimpleElementValueGen(int type, ConstantPoolGen cpGen, long value)
    {
        super(type, cpGen);
        idx = getConstantPool().addLong(value);
    }

    public SimpleElementValueGen(int type, ConstantPoolGen cpGen, double value)
    {
        super(type, cpGen);
        idx = getConstantPool().addDouble(value);
    }

    public SimpleElementValueGen(int type, ConstantPoolGen cpGen, float value)
    {
        super(type, cpGen);
        idx = getConstantPool().addFloat(value);
    }

    public SimpleElementValueGen(int type, ConstantPoolGen cpGen, short value)
    {
        super(type, cpGen);
        idx = getConstantPool().addInteger(value);
    }

    public SimpleElementValueGen(int type, ConstantPoolGen cpGen, byte value)
    {
        super(type, cpGen);
        idx = getConstantPool().addInteger(value);
    }

    public SimpleElementValueGen(int type, ConstantPoolGen cpGen, char value)
    {
        super(type, cpGen);
        idx = getConstantPool().addInteger(value);
    }

    public SimpleElementValueGen(int type, ConstantPoolGen cpGen, boolean value)
    {
        super(type, cpGen);
        if (value) {
            idx = getConstantPool().addInteger(1);
        } else {
            idx = getConstantPool().addInteger(0);
        }
    }

    public SimpleElementValueGen(int type, ConstantPoolGen cpGen, String value)
    {
        super(type, cpGen);
        idx = getConstantPool().addUtf8(value);
    }

    /**
     * The boolean controls whether we copy info from the 'old' constant pool to
     * the 'new'. You need to use this ctor if the annotation is being copied
     * from one file to another.
     */
    public SimpleElementValueGen(SimpleElementValue value,
            ConstantPoolGen cpool, boolean copyPoolEntries)
    {
        super(value.getElementValueType(), cpool);
        if (!copyPoolEntries)
        {
            // J5ASSERT: Could assert value.stringifyValue() is the same as
            // cpool.getConstant(SimpleElementValuevalue.getIndex())
            idx = value.getIndex();
        }
        else
        {
            switch (value.getElementValueType())
            {
            case STRING:
                idx = cpool.addUtf8(value.getValueString());
                break;
            case PRIMITIVE_INT:
                idx = cpool.addInteger(value.getValueInt());
                break;
            case PRIMITIVE_BYTE:
                idx = cpool.addInteger(value.getValueByte());
                break;
            case PRIMITIVE_CHAR:
                idx = cpool.addInteger(value.getValueChar());
                break;
            case PRIMITIVE_LONG:
                idx = cpool.addLong(value.getValueLong());
                break;
            case PRIMITIVE_FLOAT:
                idx = cpool.addFloat(value.getValueFloat());
                break;
            case PRIMITIVE_DOUBLE:
                idx = cpool.addDouble(value.getValueDouble());
                break;
            case PRIMITIVE_BOOLEAN:
                if (value.getValueBoolean())
                {
                    idx = cpool.addInteger(1);
                }
                else
                {
                    idx = cpool.addInteger(0);
                }
                break;
            case PRIMITIVE_SHORT:
                idx = cpool.addInteger(value.getValueShort());
                break;
            default:
                throw new RuntimeException(
                        "SimpleElementValueGen class does not know how "
                                + "to copy this type " + super.getElementValueType());
            }
        }
    }

    /**
     * Return immutable variant
     */
    @Override
    public ElementValue getElementValue()
    {
        return new SimpleElementValue(super.getElementValueType(), idx, getConstantPool().getConstantPool());
    }

    public int getIndex()
    {
        return idx;
    }

    public String getValueString()
    {
        if (super.getElementValueType() != STRING) {
            throw new RuntimeException(
                    "Dont call getValueString() on a non STRING ElementValue");
        }
        ConstantUtf8 c = (ConstantUtf8) getConstantPool().getConstant(idx);
        return c.getBytes();
    }

    public int getValueInt()
    {
        if (super.getElementValueType() != PRIMITIVE_INT) {
            throw new RuntimeException(
                    "Dont call getValueString() on a non STRING ElementValue");
        }
        ConstantInteger c = (ConstantInteger) getConstantPool().getConstant(idx);
        return c.getBytes();
    }

    // Whatever kind of value it is, return it as a string
    @Override
    public String stringifyValue()
    {
        switch (super.getElementValueType())
        {
        case PRIMITIVE_INT:
            ConstantInteger c = (ConstantInteger) getConstantPool().getConstant(idx);
            return Integer.toString(c.getBytes());
        case PRIMITIVE_LONG:
            ConstantLong j = (ConstantLong) getConstantPool().getConstant(idx);
            return Long.toString(j.getBytes());
        case PRIMITIVE_DOUBLE:
            ConstantDouble d = (ConstantDouble) getConstantPool().getConstant(idx);
            return Double.toString(d.getBytes());
        case PRIMITIVE_FLOAT:
            ConstantFloat f = (ConstantFloat) getConstantPool().getConstant(idx);
            return Float.toString(f.getBytes());
        case PRIMITIVE_SHORT:
            ConstantInteger s = (ConstantInteger) getConstantPool().getConstant(idx);
            return Integer.toString(s.getBytes());
        case PRIMITIVE_BYTE:
            ConstantInteger b = (ConstantInteger) getConstantPool().getConstant(idx);
            return Integer.toString(b.getBytes());
        case PRIMITIVE_CHAR:
            ConstantInteger ch = (ConstantInteger) getConstantPool().getConstant(idx);
            return Integer.toString(ch.getBytes());
        case PRIMITIVE_BOOLEAN:
            ConstantInteger bo = (ConstantInteger) getConstantPool().getConstant(idx);
            if (bo.getBytes() == 0) {
                return "false";
            }
            return "true";
        case STRING:
            ConstantUtf8 cu8 = (ConstantUtf8) getConstantPool().getConstant(idx);
            return cu8.getBytes();
        default:
            throw new RuntimeException(
                    "SimpleElementValueGen class does not know how to stringify type "
                            + super.getElementValueType());
        }
    }

    @Override
    public void dump(DataOutputStream dos) throws IOException
    {
        dos.writeByte(super.getElementValueType()); // u1 kind of value
        switch (super.getElementValueType())
        {
        case PRIMITIVE_INT:
        case PRIMITIVE_BYTE:
        case PRIMITIVE_CHAR:
        case PRIMITIVE_FLOAT:
        case PRIMITIVE_LONG:
        case PRIMITIVE_BOOLEAN:
        case PRIMITIVE_SHORT:
        case PRIMITIVE_DOUBLE:
        case STRING:
            dos.writeShort(idx);
            break;
        default:
            throw new RuntimeException(
                    "SimpleElementValueGen doesnt know how to write out type "
                            + super.getElementValueType());
        }
    }
}
