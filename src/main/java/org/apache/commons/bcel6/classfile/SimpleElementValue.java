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

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.bcel6.Constants;

/**
 * @since 6.0
 */
public class SimpleElementValue extends ElementValue
{
    private int index;

    public SimpleElementValue(int type, int index, ConstantPool cpool)
    {
        super(type, cpool);
        this.index = index;
    }

    /**
     * @return Value entry index in the cpool
     */
    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public String getValueString()
    {
        if (super.getType() != STRING) {
            throw new RuntimeException(
                    "Dont call getValueString() on a non STRING ElementValue");
        }
        ConstantUtf8 c = (ConstantUtf8) super.getConstantPool().getConstant(getIndex(),
                Constants.CONSTANT_Utf8);
        return c.getBytes();
    }

    public int getValueInt()
    {
        if (super.getType() != PRIMITIVE_INT) {
            throw new RuntimeException(
                    "Dont call getValueString() on a non STRING ElementValue");
        }
        ConstantInteger c = (ConstantInteger) super.getConstantPool().getConstant(getIndex(),
                Constants.CONSTANT_Integer);
        return c.getBytes();
    }

    public byte getValueByte()
    {
        if (super.getType() != PRIMITIVE_BYTE) {
            throw new RuntimeException(
                    "Dont call getValueByte() on a non BYTE ElementValue");
        }
        ConstantInteger c = (ConstantInteger) super.getConstantPool().getConstant(getIndex(),
                Constants.CONSTANT_Integer);
        return (byte) c.getBytes();
    }

    public char getValueChar()
    {
        if (super.getType() != PRIMITIVE_CHAR) {
            throw new RuntimeException(
                    "Dont call getValueChar() on a non CHAR ElementValue");
        }
        ConstantInteger c = (ConstantInteger) super.getConstantPool().getConstant(getIndex(),
                Constants.CONSTANT_Integer);
        return (char) c.getBytes();
    }

    public long getValueLong()
    {
        if (super.getType() != PRIMITIVE_LONG) {
            throw new RuntimeException(
                    "Dont call getValueLong() on a non LONG ElementValue");
        }
        ConstantLong j = (ConstantLong) super.getConstantPool().getConstant(getIndex());
        return j.getBytes();
    }

    public float getValueFloat()
    {
        if (super.getType() != PRIMITIVE_FLOAT) {
            throw new RuntimeException(
                    "Dont call getValueFloat() on a non FLOAT ElementValue");
        }
        ConstantFloat f = (ConstantFloat) super.getConstantPool().getConstant(getIndex());
        return f.getBytes();
    }

    public double getValueDouble()
    {
        if (super.getType() != PRIMITIVE_DOUBLE) {
            throw new RuntimeException(
                    "Dont call getValueDouble() on a non DOUBLE ElementValue");
        }
        ConstantDouble d = (ConstantDouble) super.getConstantPool().getConstant(getIndex());
        return d.getBytes();
    }

    public boolean getValueBoolean()
    {
        if (super.getType() != PRIMITIVE_BOOLEAN) {
            throw new RuntimeException(
                    "Dont call getValueBoolean() on a non BOOLEAN ElementValue");
        }
        ConstantInteger bo = (ConstantInteger) super.getConstantPool().getConstant(getIndex());
        return bo.getBytes() != 0;
    }

    public short getValueShort()
    {
        if (super.getType() != PRIMITIVE_SHORT) {
            throw new RuntimeException(
                    "Dont call getValueShort() on a non SHORT ElementValue");
        }
        ConstantInteger s = (ConstantInteger) super.getConstantPool().getConstant(getIndex());
        return (short) s.getBytes();
    }

    @Override
    public String toString()
    {
        return stringifyValue();
    }

    // Whatever kind of value it is, return it as a string
    @Override
    public String stringifyValue()
    {
        ConstantPool cpool = super.getConstantPool();
        final int _type = super.getType();
        switch (_type)
        {
        case PRIMITIVE_INT:
            ConstantInteger c = (ConstantInteger) cpool.getConstant(getIndex(),
                    Constants.CONSTANT_Integer);
            return Integer.toString(c.getBytes());
        case PRIMITIVE_LONG:
            ConstantLong j = (ConstantLong) cpool.getConstant(getIndex(),
                    Constants.CONSTANT_Long);
            return Long.toString(j.getBytes());
        case PRIMITIVE_DOUBLE:
            ConstantDouble d = (ConstantDouble) cpool.getConstant(getIndex(),
                    Constants.CONSTANT_Double);
            return Double.toString(d.getBytes());
        case PRIMITIVE_FLOAT:
            ConstantFloat f = (ConstantFloat) cpool.getConstant(getIndex(),
                    Constants.CONSTANT_Float);
            return Float.toString(f.getBytes());
        case PRIMITIVE_SHORT:
            ConstantInteger s = (ConstantInteger) cpool.getConstant(getIndex(),
                    Constants.CONSTANT_Integer);
            return Integer.toString(s.getBytes());
        case PRIMITIVE_BYTE:
            ConstantInteger b = (ConstantInteger) cpool.getConstant(getIndex(),
                    Constants.CONSTANT_Integer);
            return Integer.toString(b.getBytes());
        case PRIMITIVE_CHAR:
            ConstantInteger ch = (ConstantInteger) cpool.getConstant(
                    getIndex(), Constants.CONSTANT_Integer);
            return String.valueOf((char)ch.getBytes());
        case PRIMITIVE_BOOLEAN:
            ConstantInteger bo = (ConstantInteger) cpool.getConstant(
                    getIndex(), Constants.CONSTANT_Integer);
            if (bo.getBytes() == 0) {
                return "false";
            }
            return "true";
        case STRING:
            ConstantUtf8 cu8 = (ConstantUtf8) cpool.getConstant(getIndex(),
                    Constants.CONSTANT_Utf8);
            return cu8.getBytes();
        default:
            throw new RuntimeException(
                    "SimpleElementValue class does not know how to stringify type "
                            + _type);
        }
    }

    @Override
    public void dump(DataOutputStream dos) throws IOException
    {
        final int _type = super.getType();
        dos.writeByte(_type); // u1 kind of value
        switch (_type)
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
            dos.writeShort(getIndex());
            break;
        default:
            throw new RuntimeException(
                    "SimpleElementValue doesnt know how to write out type "
                            + _type);
        }
    }
}
