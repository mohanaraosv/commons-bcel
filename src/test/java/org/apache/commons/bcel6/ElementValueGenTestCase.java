/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.commons.bcel6;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.bcel6.generic.ClassElementValueGen;
import org.apache.commons.bcel6.generic.ClassGen;
import org.apache.commons.bcel6.generic.ConstantPoolGen;
import org.apache.commons.bcel6.generic.ElementValueGen;
import org.apache.commons.bcel6.generic.EnumElementValueGen;
import org.apache.commons.bcel6.generic.ObjectType;
import org.apache.commons.bcel6.generic.SimpleElementValueGen;

public class ElementValueGenTestCase extends AbstractTestCase
{
    private ClassGen createClassGen(String classname)
    {
        return new ClassGen(classname, "java.lang.Object", "<generated>",
                Constants.ACC_PUBLIC | Constants.ACC_SUPER, null);
    }

    /**
     * Create primitive element values
     */
    public void testCreateIntegerElementValue()
    {
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        SimpleElementValueGen evg = new SimpleElementValueGen(
                ElementValueGen.PRIMITIVE_INT, cp, 555);
        // Creation of an element like that should leave a new entry in the
        // cpool
        assertTrue("Should have the same index in the constantpool but "
                + evg.getIndex() + "!=" + cp.lookupInteger(555),
                evg.getIndex() == cp.lookupInteger(555));
        checkSerialize(evg, cp);
    }

    public void testCreateFloatElementValue()
    {
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        SimpleElementValueGen evg = new SimpleElementValueGen(
                ElementValueGen.PRIMITIVE_FLOAT, cp, 111.222f);
        // Creation of an element like that should leave a new entry in the
        // cpool
        assertTrue("Should have the same index in the constantpool but "
                + evg.getIndex() + "!=" + cp.lookupFloat(111.222f), evg
                .getIndex() == cp.lookupFloat(111.222f));
        checkSerialize(evg, cp);
    }

    public void testCreateDoubleElementValue()
    {
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        SimpleElementValueGen evg = new SimpleElementValueGen(
                ElementValueGen.PRIMITIVE_DOUBLE, cp, 333.44);
        // Creation of an element like that should leave a new entry in the
        // cpool
        int idx = cp.lookupDouble(333.44);
        assertTrue("Should have the same index in the constantpool but "
                + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
        checkSerialize(evg, cp);
    }

    public void testCreateLongElementValue()
    {
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        SimpleElementValueGen evg = new SimpleElementValueGen(
                ElementValueGen.PRIMITIVE_LONG, cp, 3334455L);
        // Creation of an element like that should leave a new entry in the
        // cpool
        int idx = cp.lookupLong(3334455L);
        assertTrue("Should have the same index in the constantpool but "
                + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
        checkSerialize(evg, cp);
    }

    public void testCreateCharElementValue()
    {
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        SimpleElementValueGen evg = new SimpleElementValueGen(
                ElementValueGen.PRIMITIVE_CHAR, cp, 't');
        // Creation of an element like that should leave a new entry in the
        // cpool
        int idx = cp.lookupInteger('t');
        assertTrue("Should have the same index in the constantpool but "
                + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
        checkSerialize(evg, cp);
    }

    public void testCreateByteElementValue()
    {
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        SimpleElementValueGen evg = new SimpleElementValueGen(
                ElementValueGen.PRIMITIVE_CHAR, cp, (byte) 'z');
        // Creation of an element like that should leave a new entry in the
        // cpool
        int idx = cp.lookupInteger((byte) 'z');
        assertTrue("Should have the same index in the constantpool but "
                + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
        checkSerialize(evg, cp);
    }

    public void testCreateBooleanElementValue()
    {
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        SimpleElementValueGen evg = new SimpleElementValueGen(
                ElementValueGen.PRIMITIVE_BOOLEAN, cp, true);
        // Creation of an element like that should leave a new entry in the
        // cpool
        int idx = cp.lookupInteger(1); // 1 == true
        assertTrue("Should have the same index in the constantpool but "
                + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
        checkSerialize(evg, cp);
    }

    public void testCreateShortElementValue()
    {
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        SimpleElementValueGen evg = new SimpleElementValueGen(
                ElementValueGen.PRIMITIVE_SHORT, cp, (short) 42);
        // Creation of an element like that should leave a new entry in the
        // cpool
        int idx = cp.lookupInteger(42);
        assertTrue("Should have the same index in the constantpool but "
                + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
        checkSerialize(evg, cp);
    }

    // //
    // Create string element values
    public void testCreateStringElementValue()
    {
        // Create HelloWorld
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        SimpleElementValueGen evg = new SimpleElementValueGen(
                ElementValueGen.STRING, cp, "hello");
        // Creation of an element like that should leave a new entry in the
        // cpool
        assertTrue("Should have the same index in the constantpool but "
                + evg.getIndex() + "!=" + cp.lookupUtf8("hello"), evg
                .getIndex() == cp.lookupUtf8("hello"));
        checkSerialize(evg, cp);
    }

    // //
    // Create enum element value
    public void testCreateEnumElementValue()
    {
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        ObjectType enumType = new ObjectType("SimpleEnum"); // Supports rainbow
                                                            // :)
        EnumElementValueGen evg = new EnumElementValueGen(enumType, "Red", cp);
        // Creation of an element like that should leave a new entry in the
        // cpool
        assertTrue(
                "The new ElementValue value index should match the contents of the constantpool but "
                        + evg.getValueIndex() + "!=" + cp.lookupUtf8("Red"),
                evg.getValueIndex() == cp.lookupUtf8("Red"));
        // BCELBUG: Should the class signature or class name be in the constant
        // pool? (see note in ConstantPool)
        // assertTrue("The new ElementValue type index should match the contents
        // of the constantpool but "+
        // evg.getTypeIndex()+"!="+cp.lookupClass(enumType.getSignature()),
        // evg.getTypeIndex()==cp.lookupClass(enumType.getSignature()));
        checkSerialize(evg, cp);
    }

    // //
    // Create class element value
    public void testCreateClassElementValue()
    {
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();
        ObjectType classType = new ObjectType("java.lang.Integer");
        ClassElementValueGen evg = new ClassElementValueGen(classType, cp);
        assertTrue("Unexpected value for contained class: '"
                + evg.getClassString() + "'", evg.getClassString().contains("Integer"));
        checkSerialize(evg, cp);
    }

    private void checkSerialize(ElementValueGen evgBefore, ConstantPoolGen cpg)
    {
        try
        {
            String beforeValue = evgBefore.stringifyValue();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            evgBefore.dump(dos);
            dos.flush();
            dos.close();
            byte[] bs = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(bs);
            DataInputStream dis = new DataInputStream(bais);
            ElementValueGen evgAfter = ElementValueGen.readElementValue(dis,
                    cpg);
            dis.close();
            String afterValue = evgAfter.stringifyValue();
            if (!beforeValue.equals(afterValue))
            {
                fail("Deserialization failed: before='" + beforeValue
                        + "' after='" + afterValue + "'");
            }
        }
        catch (IOException ioe)
        {
            fail("Unexpected exception whilst checking serialization: " + ioe);
        }
    }
}
