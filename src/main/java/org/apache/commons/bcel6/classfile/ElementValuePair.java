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
 * an annotation's element value pair
 * 
 * @version $Id: ElementValuePair
 * @since 6.0
 */
public class ElementValuePair
{
    private final ElementValue elementValue;

    private final ConstantPool constantPool;

    private final int elementNameIndex;

    public ElementValuePair(int elementNameIndex, ElementValue elementValue,
            ConstantPool constantPool)
    {
        this.elementValue = elementValue;
        this.elementNameIndex = elementNameIndex;
        this.constantPool = constantPool;
    }

    public String getNameString()
    {
        ConstantUtf8 c = (ConstantUtf8) constantPool.getConstant(
                elementNameIndex, Constants.CONSTANT_Utf8);
        return c.getBytes();
    }

    public final ElementValue getValue()
    {
        return elementValue;
    }

    public int getNameIndex()
    {
        return elementNameIndex;
    }

    public String toShortString()
    {
        StringBuilder result = new StringBuilder();
        result.append(getNameString()).append("=").append(
                getValue().toShortString());
        return result.toString();
    }

    protected void dump(DataOutputStream dos) throws IOException {
        dos.writeShort(elementNameIndex); // u2 name of the element
        elementValue.dump(dos);
    }
}
