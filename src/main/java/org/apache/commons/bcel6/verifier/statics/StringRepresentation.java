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
package org.apache.commons.bcel6.verifier.statics;


import org.apache.commons.bcel6.classfile.Annotations;
import org.apache.commons.bcel6.classfile.BootstrapMethods;
import org.apache.commons.bcel6.classfile.Code;
import org.apache.commons.bcel6.classfile.CodeException;
import org.apache.commons.bcel6.classfile.ConstantClass;
import org.apache.commons.bcel6.classfile.ConstantDouble;
import org.apache.commons.bcel6.classfile.ConstantFieldref;
import org.apache.commons.bcel6.classfile.ConstantFloat;
import org.apache.commons.bcel6.classfile.ConstantInteger;
import org.apache.commons.bcel6.classfile.ConstantInterfaceMethodref;
import org.apache.commons.bcel6.classfile.ConstantLong;
import org.apache.commons.bcel6.classfile.ConstantMethodref;
import org.apache.commons.bcel6.classfile.ConstantNameAndType;
import org.apache.commons.bcel6.classfile.ConstantPool;
import org.apache.commons.bcel6.classfile.ConstantString;
import org.apache.commons.bcel6.classfile.ConstantUtf8;
import org.apache.commons.bcel6.classfile.ConstantValue;
import org.apache.commons.bcel6.classfile.Deprecated;
import org.apache.commons.bcel6.classfile.EnclosingMethod;
import org.apache.commons.bcel6.classfile.ExceptionTable;
import org.apache.commons.bcel6.classfile.Field;
import org.apache.commons.bcel6.classfile.InnerClass;
import org.apache.commons.bcel6.classfile.InnerClasses;
import org.apache.commons.bcel6.classfile.JavaClass;
import org.apache.commons.bcel6.classfile.LineNumber;
import org.apache.commons.bcel6.classfile.LineNumberTable;
import org.apache.commons.bcel6.classfile.LocalVariable;
import org.apache.commons.bcel6.classfile.LocalVariableTable;
import org.apache.commons.bcel6.classfile.LocalVariableTypeTable;
import org.apache.commons.bcel6.classfile.Method;
import org.apache.commons.bcel6.classfile.MethodParameters;
import org.apache.commons.bcel6.classfile.Node;
import org.apache.commons.bcel6.classfile.Signature;
import org.apache.commons.bcel6.classfile.SourceFile;
import org.apache.commons.bcel6.classfile.StackMap;
import org.apache.commons.bcel6.classfile.Synthetic;
import org.apache.commons.bcel6.classfile.Unknown;
import org.apache.commons.bcel6.verifier.exc.AssertionViolatedException;

/**
 * BCEL's Node classes (those from the classfile API that <B>accept()</B> Visitor
 * instances) have <B>toString()</B> methods that were not designed to be robust,
 * this gap is closed by this class.
 * When performing class file verification, it may be useful to output which
 * entity (e.g. a <B>Code</B> instance) is not satisfying the verifier's
 * constraints, but in this case it could be possible for the <B>toString()</B>
 * method to throw a RuntimeException.
 * A (new StringRepresentation(Node n)).toString() never throws any exception.
 * Note that this class also serves as a placeholder for more sophisticated message
 * handling in future versions of JustIce.
 *
 * @version $Id$
 */
public class StringRepresentation extends org.apache.commons.bcel6.classfile.EmptyVisitor {
    /** The string representation, created by a visitXXX() method, output by toString(). */
    private String tostring;
    /** The node we ask for its string representation. Not really needed; only for debug output. */
    private final Node n;

    /**
     * Creates a new StringRepresentation object which is the representation of n.
     *
     * @see #toString()
     */
    public StringRepresentation(Node n) {
        this.n = n;
        n.accept(this); // assign a string representation to field 'tostring' if we know n's class.
    }

    /**
     * Returns the String representation.
     */
    @Override
    public String toString() {
// The run-time check below is needed because we don't want to omit inheritance
// of "EmptyVisitor" and provide a thousand empty methods.
// However, in terms of performance this would be a better idea.
// If some new "Node" is defined in BCEL (such as some concrete "Attribute"), we
// want to know that this class has also to be adapted.
        if (tostring == null) {
            throw new AssertionViolatedException(
                "Please adapt '" + getClass() + "' to deal with objects of class '" + n.getClass() + "'.");
        }
        return tostring;
    }

    /**
     * Returns the String representation of the Node object obj;
     * this is obj.toString() if it does not throw any RuntimeException,
     * or else it is a string derived only from obj's class name.
     */
    private String toString(Node obj) {
        String ret;
        try {
            ret = obj.toString();
        }
        
        catch (RuntimeException e) {
            // including ClassFormatException, trying to convert the "signature" of a ReturnaddressType LocalVariable
            // (shouldn't occur, but people do crazy things)
            String s = obj.getClass().getName();
            s = s.substring(s.lastIndexOf(".") + 1);
            ret = "<<" + s + ">>";
        }
        return ret;
    }

    ////////////////////////////////
    // Visitor methods start here //
    ////////////////////////////////
    // We don't of course need to call some default implementation:
    // e.g. we could also simply output "Code" instead of a possibly
    // lengthy Code attribute's toString().
    @Override
    public void visitCode(Code obj) {
        //tostring = toString(obj);
        tostring = "<CODE>"; // We don't need real code outputs.
    }

    /**
     * @since 6.0
     */
    @Override
    public void visitAnnotation(Annotations obj)
    {
        //this is invoked whenever an annotation is found
        //when verifier is passed over a class
        tostring = toString(obj);
    }
    
    /**
     * @since 6.0
     */
    @Override
    public void visitLocalVariableTypeTable(LocalVariableTypeTable obj)
    {
        //this is invoked whenever a local variable type is found
        //when verifier is passed over a class
        tostring = toString(obj);
    }
    
    @Override
    public void visitCodeException(CodeException obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantClass(ConstantClass obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantDouble(ConstantDouble obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantFieldref(ConstantFieldref obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantFloat(ConstantFloat obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantInteger(ConstantInteger obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantLong(ConstantLong obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantMethodref(ConstantMethodref obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantNameAndType(ConstantNameAndType obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantPool(ConstantPool obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantString(ConstantString obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantUtf8(ConstantUtf8 obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitConstantValue(ConstantValue obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitDeprecated(Deprecated obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitExceptionTable(ExceptionTable obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitField(Field obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitInnerClass(InnerClass obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitInnerClasses(InnerClasses obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitJavaClass(JavaClass obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitLineNumber(LineNumber obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitLineNumberTable(LineNumberTable obj) {
        tostring = "<LineNumberTable: " + toString(obj) + ">";
    }

    @Override
    public void visitLocalVariable(LocalVariable obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitLocalVariableTable(LocalVariableTable obj) {
        tostring = "<LocalVariableTable: " + toString(obj) + ">";
    }

    @Override
    public void visitMethod(Method obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitSignature(Signature obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitSourceFile(SourceFile obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitStackMap(StackMap obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitSynthetic(Synthetic obj) {
        tostring = toString(obj);
    }

    @Override
    public void visitUnknown(Unknown obj) {
        tostring = toString(obj);
    }

    /**
     * @since 6.0
     */
    @Override
    public void visitEnclosingMethod(EnclosingMethod obj) {
        tostring = toString(obj);
    }

    /**
     * @since 6.0
     */
    @Override
    public void visitBootstrapMethods(BootstrapMethods obj) {
        tostring = toString(obj);
    }

    /**
     * @since 6.0
     */
    @Override
    public void visitMethodParameters(MethodParameters obj) {
        tostring = toString(obj);
    }
}
