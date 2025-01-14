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

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.bcel6.Constants;

/** 
 * Abstract super class for fields and methods.
 *
 * @version $Id$
 */
public abstract class FieldOrMethod extends AccessFlags implements Cloneable, Node {

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @java.lang.Deprecated
    protected int name_index; // Points to field name in constant pool 

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @java.lang.Deprecated
    protected int signature_index; // Points to encoded signature

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @java.lang.Deprecated
    protected Attribute[] attributes; // Collection of attributes

    // @since 6.0
    private AnnotationEntry[] annotationEntries; // annotations defined on the field or method 

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @java.lang.Deprecated
    protected ConstantPool constant_pool;

    private String signatureAttributeString = null;
    private boolean searchedForSignatureAttribute = false;

    FieldOrMethod() {
    }


    /**
     * Initialize from another object. Note that both objects use the same
     * references (shallow copy). Use clone() for a physical copy.
     */
    protected FieldOrMethod(FieldOrMethod c) {
        this(c.getAccessFlags(), c.getNameIndex(), c.getSignatureIndex(), c.getAttributes(), c
                .getConstantPool());
    }


    /**
     * Construct object from file stream.
     * @param file Input stream
     * @throws IOException
     * @throws ClassFormatException
     */
    protected FieldOrMethod(DataInput file, ConstantPool constant_pool) throws IOException, ClassFormatException {
        this(file.readUnsignedShort(), file.readUnsignedShort(), file.readUnsignedShort(), null,
                constant_pool);
        int attributes_count = file.readUnsignedShort();
        attributes = new Attribute[attributes_count];
        for (int i = 0; i < attributes_count; i++) {
            attributes[i] = Attribute.readAttribute(file, constant_pool);
        }
    }


    /**
     * @param access_flags Access rights of method
     * @param name_index Points to field name in constant pool
     * @param signature_index Points to encoded signature
     * @param attributes Collection of attributes
     * @param constant_pool Array of constants
     */
    protected FieldOrMethod(int access_flags, int name_index, int signature_index,
            Attribute[] attributes, ConstantPool constant_pool) {
        super(access_flags);
        this.name_index = name_index;
        this.signature_index = signature_index;
        this.constant_pool = constant_pool;
        setAttributes(attributes);
    }


    /**
     * Dump object to file stream on binary format.
     *
     * @param file Output file stream
     * @throws IOException
     */
    public final void dump( DataOutputStream file ) throws IOException {
        file.writeShort(super.getAccessFlags());
        file.writeShort(name_index);
        file.writeShort(signature_index);
        file.writeShort(attributes.length);
        for (Attribute attribute : attributes) {
            attribute.dump(file);
        }
    }


    /**
     * @return Collection of object attributes.
     */
    public final Attribute[] getAttributes() {
        return attributes;
    }


    /**
     * @param attributes Collection of object attributes.
     */
    public final void setAttributes( Attribute[] attributes ) {
        this.attributes = attributes;
    }


    /**
     * @return Constant pool used by this object.
     */
    public final ConstantPool getConstantPool() {
        return constant_pool;
    }


    /**
     * @param constant_pool Constant pool to be used for this object.
     */
    public final void setConstantPool( ConstantPool constant_pool ) {
        this.constant_pool = constant_pool;
    }


    /**
     * @return Index in constant pool of object's name.
     */
    public final int getNameIndex() {
        return name_index;
    }


    /**
     * @param name_index Index in constant pool of object's name.
     */
    public final void setNameIndex( int name_index ) {
        this.name_index = name_index;
    }


    /**
     * @return Index in constant pool of field signature.
     */
    public final int getSignatureIndex() {
        return signature_index;
    }


    /**
     * @param signature_index Index in constant pool of field signature.
     */
    public final void setSignatureIndex( int signature_index ) {
        this.signature_index = signature_index;
    }


    /**
     * @return Name of object, i.e., method name or field name
     */
    public final String getName() {
        ConstantUtf8 c;
        c = (ConstantUtf8) constant_pool.getConstant(name_index, Constants.CONSTANT_Utf8);
        return c.getBytes();
    }


    /**
     * @return String representation of object's type signature (java style)
     */
    public final String getSignature() {
        ConstantUtf8 c;
        c = (ConstantUtf8) constant_pool.getConstant(signature_index, Constants.CONSTANT_Utf8);
        return c.getBytes();
    }


    /**
     * @return deep copy of this field
     */
    protected FieldOrMethod copy_( ConstantPool _constant_pool ) {
        FieldOrMethod c = null;

        try {
          c = (FieldOrMethod)clone();
        } catch(CloneNotSupportedException e) {
            // ignored, but will cause NPE ...
        }

        c.constant_pool    = constant_pool;
        c.attributes       = new Attribute[attributes.length];

        for (int i = 0; i < attributes.length; i++) {
            c.attributes[i] = attributes[i].copy(constant_pool);
        }

        return c;
    }

    /**
     * @return Annotations on the field or method
     * @since 6.0
     */
    public AnnotationEntry[] getAnnotationEntries() {
        if (annotationEntries == null) {
            annotationEntries = AnnotationEntry.createAnnotationEntries(getAttributes());
        }
        
        return annotationEntries;
    }

    /**
     * Hunts for a signature attribute on the member and returns its contents.  So where the 'regular' signature
     * may be (Ljava/util/Vector;)V the signature attribute may in fact say 'Ljava/lang/Vector&lt;Ljava/lang/String&gt;;'
     * Coded for performance - searches for the attribute only when requested - only searches for it once.
     * @since 6.0
     */
    public final String getGenericSignature()
    {
        if (!searchedForSignatureAttribute)
        {
            boolean found = false;
            for (int i = 0; !found && i < attributes.length; i++)
            {
                if (attributes[i] instanceof Signature)
                {
                    signatureAttributeString = ((Signature) attributes[i])
                            .getSignature();
                    found = true;
                }
            }
            searchedForSignatureAttribute = true;
        }
        return signatureAttributeString;
    }
}
