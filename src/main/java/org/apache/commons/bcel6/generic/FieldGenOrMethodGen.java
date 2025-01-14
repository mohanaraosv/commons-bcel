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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.bcel6.Constants;
import org.apache.commons.bcel6.classfile.AccessFlags;
import org.apache.commons.bcel6.classfile.Attribute;

/**
 * Super class for FieldGen and MethodGen objects, since they have
 * some methods in common!
 *
 * @version $Id$
 */
public abstract class FieldGenOrMethodGen extends AccessFlags implements NamedAndTyped, Cloneable {

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @Deprecated
    protected String name;

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @Deprecated
    protected Type type;

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @Deprecated
    protected ConstantPoolGen cp;

    private final List<Attribute> attribute_vec = new ArrayList<>();

    // @since 6.0
    private final List<AnnotationEntryGen>       annotation_vec= new ArrayList<>();


    protected FieldGenOrMethodGen() {
    }


    /**
     * @since 6.0
     */
    protected FieldGenOrMethodGen(int access_flags) { // TODO could this be package protected?
        super(access_flags);
    }

    @Override
    public void setType( Type type ) { // TODO could be package-protected?
        if (type.getType() == Constants.T_ADDRESS) {
            throw new IllegalArgumentException("Type can not be " + type);
        }
        this.type = type;
    }


    @Override
    public Type getType() {
        return type;
    }


    /** @return name of method/field.
     */
    @Override
    public String getName() {
        return name;
    }


    @Override
    public void setName( String name ) { // TODO could be package-protected?
        this.name = name;
    }


    public ConstantPoolGen getConstantPool() {
        return cp;
    }


    public void setConstantPool( ConstantPoolGen cp ) { // TODO could be package-protected?
        this.cp = cp;
    }


    /**
     * Add an attribute to this method. Currently, the JVM knows about
     * the `Code', `ConstantValue', `Synthetic' and `Exceptions'
     * attributes. Other attributes will be ignored by the JVM but do no
     * harm.
     *
     * @param a attribute to be added
     */
    public void addAttribute( Attribute a ) {
        attribute_vec.add(a);
    }

    /**
     * @since 6.0
     */
    protected void addAnnotationEntry(AnnotationEntryGen ag) // TODO could this be package protected?
    {
        annotation_vec.add(ag);
    }


    /**
     * Remove an attribute.
     */
    public void removeAttribute( Attribute a ) {
        attribute_vec.remove(a);
    }

    /**
     * @since 6.0
     */
    protected void removeAnnotationEntry(AnnotationEntryGen ag) // TODO could this be package protected?
    {
        annotation_vec.remove(ag);
    }


    /**
     * Remove all attributes.
     */
    public void removeAttributes() {
        attribute_vec.clear();
    }

    /**
     * @since 6.0
     */
    protected void removeAnnotationEntries() // TODO could this be package protected?
    {
        annotation_vec.clear();
    }


    /**
     * @return all attributes of this method.
     */
    public Attribute[] getAttributes() {
        Attribute[] attributes = new Attribute[attribute_vec.size()];
        attribute_vec.toArray(attributes);
        return attributes;
    }

    public AnnotationEntryGen[] getAnnotationEntries() {
        AnnotationEntryGen[] annotations = new AnnotationEntryGen[annotation_vec.size()];
          annotation_vec.toArray(annotations);
          return annotations;
      }


    /** @return signature of method/field.
     */
    public abstract String getSignature();


    @Override
    public FieldGenOrMethodGen clone() {
        try {
            return (FieldGenOrMethodGen) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Clone Not Supported"); // never happens
        }
    }
}
