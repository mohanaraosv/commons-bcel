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
import java.io.IOException;

import org.apache.commons.bcel6.Constants;
import org.apache.commons.bcel6.generic.Type;
import org.apache.commons.bcel6.util.BCELComparator;

/**
 * This class represents the method info structure, i.e., the representation 
 * for a method in the class. See JVM specification for details.
 * A method has access flags, a name, a signature and a number of attributes.
 *
 * @version $Id$
 */
public final class Method extends FieldOrMethod {

    private static BCELComparator _cmp = new BCELComparator() {

        @Override
        public boolean equals( Object o1, Object o2 ) {
            Method THIS = (Method) o1;
            Method THAT = (Method) o2;
            return THIS.getName().equals(THAT.getName())
                    && THIS.getSignature().equals(THAT.getSignature());
        }


        @Override
        public int hashCode( Object o ) {
            Method THIS = (Method) o;
            return THIS.getSignature().hashCode() ^ THIS.getName().hashCode();
        }
    };

    // annotations defined on the parameters of a method
    private ParameterAnnotationEntry[] parameterAnnotationEntries;

    /**
     * Empty constructor, all attributes have to be defined via `setXXX'
     * methods. Use at your own risk.
     */
    public Method() {
    }


    /**
     * Initialize from another object. Note that both objects use the same
     * references (shallow copy). Use clone() for a physical copy.
     */
    public Method(Method c) {
        super(c);
    }


    /**
     * Construct object from file stream.
     * @param file Input stream
     * @throws IOException
     * @throws ClassFormatException
     */
    Method(DataInput file, ConstantPool constant_pool) throws IOException,
            ClassFormatException {
        super(file, constant_pool);
    }


    /**
     * @param access_flags Access rights of method
     * @param name_index Points to field name in constant pool
     * @param signature_index Points to encoded signature
     * @param attributes Collection of attributes
     * @param constant_pool Array of constants
     */
    public Method(int access_flags, int name_index, int signature_index, Attribute[] attributes,
            ConstantPool constant_pool) {
        super(access_flags, name_index, signature_index, attributes, constant_pool);
    }


    /**
     * Called by objects that are traversing the nodes of the tree implicitely
     * defined by the contents of a Java class. I.e., the hierarchy of methods,
     * fields, attributes, etc. spawns a tree of objects.
     *
     * @param v Visitor object
     */
    @Override
    public void accept( Visitor v ) {
        v.visitMethod(this);
    }


    /**
     * @return Code attribute of method, if any
     */
    public final Code getCode() {
        for (Attribute attribute : super.getAttributes()) {
            if (attribute instanceof Code) {
                return (Code) attribute;
            }
        }
        return null;
    }


    /**
     * @return ExceptionTable attribute of method, if any, i.e., list all
     * exceptions the method may throw not exception handlers!
     */
    public final ExceptionTable getExceptionTable() {
        for (Attribute attribute : super.getAttributes()) {
            if (attribute instanceof ExceptionTable) {
                return (ExceptionTable) attribute;
            }
        }
        return null;
    }


    /** @return LocalVariableTable of code attribute if any, i.e. the call is forwarded
     * to the Code atribute.
     */
    public final LocalVariableTable getLocalVariableTable() {
        Code code = getCode();
        if (code == null) {
            return null;
        }
        return code.getLocalVariableTable();
    }


    /** @return LineNumberTable of code attribute if any, i.e. the call is forwarded
     * to the Code atribute.
     */
    public final LineNumberTable getLineNumberTable() {
        Code code = getCode();
        if (code == null) {
            return null;
        }
        return code.getLineNumberTable();
    }


    /**
     * Return string representation close to declaration format,
     * `public static void main(String[] args) throws IOException', e.g.
     *
     * @return String representation of the method.
     */
    @Override
    public final String toString() {
        String access = Utility.accessToString(super.getAccessFlags());
        // Get name and signature from constant pool
        ConstantUtf8 c = (ConstantUtf8) super.getConstantPool().getConstant(super.getSignatureIndex(), Constants.CONSTANT_Utf8);
        String signature = c.getBytes();
        c = (ConstantUtf8) super.getConstantPool().getConstant(super.getNameIndex(), Constants.CONSTANT_Utf8);
        String name = c.getBytes();
        signature = Utility.methodSignatureToString(signature, name, access, true,
                getLocalVariableTable());
        StringBuilder buf = new StringBuilder(signature);
        for (Attribute attribute : super.getAttributes()) {
            if (!((attribute instanceof Code) || (attribute instanceof ExceptionTable))) {
                buf.append(" [").append(attribute).append("]");
            }
        }
        ExceptionTable e = getExceptionTable();
        if (e != null) {
            String str = e.toString();
            if (!str.equals("")) {
                buf.append("\n\t\tthrows ").append(str);
            }
        }
        return buf.toString();
    }


    /**
     * @return deep copy of this method
     */
    public final Method copy( ConstantPool _constant_pool ) {
        return (Method) copy_(_constant_pool);
    }


    /**
     * @return return type of method
     */
    public Type getReturnType() {
        return Type.getReturnType(getSignature());
    }


    /**
     * @return array of method argument types
     */
    public Type[] getArgumentTypes() {
        return Type.getArgumentTypes(getSignature());
    }


    /**
     * @return Comparison strategy object
     */
    public static BCELComparator getComparator() {
        return _cmp;
    }


    /**
     * @param comparator Comparison strategy object
     */
    public static void setComparator( BCELComparator comparator ) {
        _cmp = comparator;
    }


    /**
     * Return value as defined by given BCELComparator strategy.
     * By default two method objects are said to be equal when
     * their names and signatures are equal.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        return _cmp.equals(this, obj);
    }


    /**
     * Return value as defined by given BCELComparator strategy.
     * By default return the hashcode of the method's name XOR signature.
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return _cmp.hashCode(this);
    }

    /**
     * @return Annotations on the parameters of a method
     * @since 6.0
     */
    public ParameterAnnotationEntry[] getParameterAnnotationEntries() {
        if (parameterAnnotationEntries == null) {
            parameterAnnotationEntries = ParameterAnnotationEntry.createParameterAnnotationEntries(getAttributes());
        }
        return parameterAnnotationEntries;
    }
}
