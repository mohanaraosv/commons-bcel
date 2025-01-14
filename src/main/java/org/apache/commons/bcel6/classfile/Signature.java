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

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.bcel6.Constants;

/**
 * This class is derived from <em>Attribute</em> and represents a reference
 * to a GJ attribute.
 *
 * @version $Id$
 * @see     Attribute
 */
public final class Signature extends Attribute {

    private int signature_index;


    /**
     * Initialize from another object. Note that both objects use the same
     * references (shallow copy). Use clone() for a physical copy.
     */
    public Signature(Signature c) {
        this(c.getNameIndex(), c.getLength(), c.getSignatureIndex(), c.getConstantPool());
    }


    /**
     * Construct object from file stream.
     * @param name_index Index in constant pool to CONSTANT_Utf8
     * @param length Content length in bytes
     * @param input Input stream
     * @param constant_pool Array of constants
     * @throws IOException
     */
    Signature(int name_index, int length, DataInput input, ConstantPool constant_pool)
            throws IOException {
        this(name_index, length, input.readUnsignedShort(), constant_pool);
    }


    /**
     * @param name_index Index in constant pool to CONSTANT_Utf8
     * @param length Content length in bytes
     * @param signature_index Index in constant pool to CONSTANT_Utf8
     * @param constant_pool Array of constants
     */
    public Signature(int name_index, int length, int signature_index, ConstantPool constant_pool) {
        super(Constants.ATTR_SIGNATURE, name_index, length, constant_pool);
        this.signature_index = signature_index;
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
        //System.err.println("Visiting non-standard Signature object");
        v.visitSignature(this);
    }


    /**
     * Dump source file attribute to file stream in binary format.
     *
     * @param file Output file stream
     * @throws IOException
     */
    @Override
    public final void dump( DataOutputStream file ) throws IOException {
        super.dump(file);
        file.writeShort(signature_index);
    }


    /**
     * @return Index in constant pool of source file name.
     */
    public final int getSignatureIndex() {
        return signature_index;
    }


    /**
     * @param signature_index the index info the constant pool of this signature
     */
    public final void setSignatureIndex( int signature_index ) {
        this.signature_index = signature_index;
    }


    /**
     * @return GJ signature.
     */
    public final String getSignature() {
        ConstantUtf8 c = (ConstantUtf8) super.getConstantPool().getConstant(signature_index,
                Constants.CONSTANT_Utf8);
        return c.getBytes();
    }

    /**
     * Extends ByteArrayInputStream to make 'unreading' chars possible.
     */
    private static final class MyByteArrayInputStream extends ByteArrayInputStream {

        MyByteArrayInputStream(String data) {
            super(data.getBytes());
        }


        final String getData() {
            return new String(buf);
        }


        final void unread() {
            if (pos > 0) {
                pos--;
            }
        }
    }


    private static boolean identStart( int ch ) {
        return ch == 'T' || ch == 'L';
    }


    private static void matchIdent( MyByteArrayInputStream in, StringBuilder buf ) {
        int ch;
        if ((ch = in.read()) == -1) {
            throw new RuntimeException("Illegal signature: " + in.getData()
                    + " no ident, reaching EOF");
        }
        //System.out.println("return from ident:" + (char)ch);
        if (!identStart(ch)) {
            StringBuilder buf2 = new StringBuilder();
            int count = 1;
            while (Character.isJavaIdentifierPart((char) ch)) {
                buf2.append((char) ch);
                count++;
                ch = in.read();
            }
            if (ch == ':') { // Ok, formal parameter
                in.skip("Ljava/lang/Object".length());
                buf.append(buf2);
                ch = in.read();
                in.unread();
                //System.out.println("so far:" + buf2 + ":next:" +(char)ch);
            } else {
                for (int i = 0; i < count; i++) {
                    in.unread();
                }
            }
            return;
        }
        StringBuilder buf2 = new StringBuilder();
        ch = in.read();
        do {
            buf2.append((char) ch);
            ch = in.read();
            //System.out.println("within ident:"+ (char)ch);
        } while ((ch != -1) && (Character.isJavaIdentifierPart((char) ch) || (ch == '/')));
        buf.append(buf2.toString().replace('/', '.'));
        //System.out.println("regular return ident:"+ (char)ch + ":" + buf2);
        if (ch != -1) {
            in.unread();
        }
    }


    private static void matchGJIdent( MyByteArrayInputStream in, StringBuilder buf ) {
        int ch;
        matchIdent(in, buf);
        ch = in.read();
        if ((ch == '<') || ch == '(') { // Parameterized or method
            //System.out.println("Enter <");
            buf.append((char) ch);
            matchGJIdent(in, buf);
            while (((ch = in.read()) != '>') && (ch != ')')) { // List of parameters
                if (ch == -1) {
                    throw new RuntimeException("Illegal signature: " + in.getData()
                            + " reaching EOF");
                }
                //System.out.println("Still no >");
                buf.append(", ");
                in.unread();
                matchGJIdent(in, buf); // Recursive call
            }
            //System.out.println("Exit >");
            buf.append((char) ch);
        } else {
            in.unread();
        }
        ch = in.read();
        if (identStart(ch)) {
            in.unread();
            matchGJIdent(in, buf);
        } else if (ch == ')') {
            in.unread();
            return;
        } else if (ch != ';') {
            throw new RuntimeException("Illegal signature: " + in.getData() + " read " + (char) ch);
        }
    }


    public static String translate( String s ) {
        //System.out.println("Sig:" + s);
        StringBuilder buf = new StringBuilder();
        matchGJIdent(new MyByteArrayInputStream(s), buf);
        return buf.toString();
    }


    // @since 6.0 is no longer final
    public static boolean isFormalParameterList( String s ) {
        return s.startsWith("<") && (s.indexOf(':') > 0);
    }


    // @since 6.0 is no longer final
    public static boolean isActualParameterList( String s ) {
        return s.startsWith("L") && s.endsWith(">;");
    }


    /**
     * @return String representation
     */
    @Override
    public final String toString() {
        String s = getSignature();
        return "Signature: " + s;
    }


    /**
     * @return deep copy of this attribute
     */
    @Override
    public Attribute copy( ConstantPool _constant_pool ) {
        return (Attribute) clone();
    }
}
