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

import org.apache.commons.bcel6.Constants;

/** 
 * Instances of this class may be used, e.g., to generate typed
 * versions of instructions. Its main purpose is to be used as the
 * byte code generating backend of a compiler. You can subclass it to
 * add your own create methods.
 * <p>
 * Note: The static createXXX methods return singleton instances
 * from the {@link InstructionConstants} class.
 *
 * @version $Id$
 * @see Constants
 * @see InstructionConstants
 */
public class InstructionFactory {

    // N.N. These must agree with the order of Constants.T_CHAR through T_LONG
    private static final String[] short_names = {
            "C", "F", "D", "B", "S", "I", "L"
    };

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @Deprecated
    protected ClassGen cg;

    /**
     * @deprecated will be made private; do not access directly, use getter/setter
     */
    @Deprecated
    protected ConstantPoolGen cp;


    public InstructionFactory(ClassGen cg, ConstantPoolGen cp) {
        this.cg = cg;
        this.cp = cp;
    }


    /** Initialize with ClassGen object
     */
    public InstructionFactory(ClassGen cg) {
        this(cg, cg.getConstantPool());
    }


    /** Initialize just with ConstantPoolGen object
     */
    public InstructionFactory(ConstantPoolGen cp) {
        this(null, cp);
    }


    /** Create an invoke instruction. (Except for invokedynamic.)
     *
     * @param class_name name of the called class
     * @param name name of the called method
     * @param ret_type return type of method
     * @param arg_types argument types of method
     * @param kind how to invoke, i.e., INVOKEINTERFACE, INVOKESTATIC, INVOKEVIRTUAL,
     * or INVOKESPECIAL
     * @see Constants
     */
    public InvokeInstruction createInvoke( String class_name, String name, Type ret_type,
            Type[] arg_types, short kind ) {
        int index;
        int nargs = 0;
        String signature = Type.getMethodSignature(ret_type, arg_types);
        for (Type arg_type : arg_types) {
            nargs += arg_type.getSize();
        }
        if (kind == Constants.INVOKEINTERFACE) {
            index = cp.addInterfaceMethodref(class_name, name, signature);
        } else {
            index = cp.addMethodref(class_name, name, signature);
        }
        switch (kind) {
            case Constants.INVOKESPECIAL:
                return new INVOKESPECIAL(index);
            case Constants.INVOKEVIRTUAL:
                return new INVOKEVIRTUAL(index);
            case Constants.INVOKESTATIC:
                return new INVOKESTATIC(index);
            case Constants.INVOKEINTERFACE:
                return new INVOKEINTERFACE(index, nargs + 1);
            default:
                throw new RuntimeException("Oops: Unknown invoke kind:" + kind);
        }
    }

    /** Create an invokedynamic instruction.
     *
     * @param bootstrap_index index into the bootstrap_methods array
     * @param name name of the called method
     * @param ret_type return type of method
     * @param arg_types argument types of method
     * @see Constants
     */
/*
 * createInvokeDynamic only needed if instrumention code wants to generate
 * a new invokedynamic instruction.  I don't think we need.  (markro)
 *
    public InvokeInstruction createInvokeDynamic( int bootstrap_index, String name, Type ret_type,
            Type[] arg_types) {
        int index;
        int nargs = 0;
        String signature = Type.getMethodSignature(ret_type, arg_types);
        for (int i = 0; i < arg_types.length; i++) {
            nargs += arg_types[i].getSize();
        }
        // UNDONE - needs to be added to ConstantPoolGen
        //index = cp.addInvokeDynamic(bootstrap_index, name, signature);
        index = 0;
        return new INVOKEDYNAMIC(index);
    }
 */

    /** Create a call to the most popular System.out.println() method.
     *
     * @param s the string to print
     */
    public InstructionList createPrintln( String s ) {
        InstructionList il = new InstructionList();
        int out = cp.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;");
        int println = cp.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V");
        il.append(new GETSTATIC(out));
        il.append(new PUSH(cp, s));
        il.append(new INVOKEVIRTUAL(println));
        return il;
    }


    /** Uses PUSH to push a constant value onto the stack.
     * @param value must be of type Number, Boolean, Character or String
     */
    public Instruction createConstant( Object value ) {
        PUSH push;
        if (value instanceof Number) {
            push = new PUSH(cp, (Number) value);
        } else if (value instanceof String) {
            push = new PUSH(cp, (String) value);
        } else if (value instanceof Boolean) {
            push = new PUSH(cp, (Boolean) value);
        } else if (value instanceof Character) {
            push = new PUSH(cp, (Character) value);
        } else {
            throw new ClassGenException("Illegal type: " + value.getClass());
        }
        return push.getInstruction();
    }

    private static class MethodObject {

        final Type[] arg_types;
        final Type result_type;
        final String class_name;
        final String name;


        MethodObject(String c, String n, Type r, Type[] a) {
            class_name = c;
            name = n;
            result_type = r;
            arg_types = a;
        }
    }


    private InvokeInstruction createInvoke( MethodObject m, short kind ) {
        return createInvoke(m.class_name, m.name, m.result_type, m.arg_types, kind);
    }

    private static final MethodObject[] append_mos = {
            new MethodObject("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] {
                Type.STRING
            }),
            new MethodObject("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] {
                Type.OBJECT
            }),
            null,
            null, // indices 2, 3
            new MethodObject("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] {
                Type.BOOLEAN
            }),
            new MethodObject("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] {
                Type.CHAR
            }),
            new MethodObject("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] {
                Type.FLOAT
            }),
            new MethodObject("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] {
                Type.DOUBLE
            }),
            new MethodObject("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] {
                Type.INT
            }),
            new MethodObject("java.lang.StringBuffer", "append", Type.STRINGBUFFER, // No append(byte)
                    new Type[] {
                        Type.INT
                    }),
            new MethodObject("java.lang.StringBuffer", "append", Type.STRINGBUFFER, // No append(short)
                    new Type[] {
                        Type.INT
                    }),
            new MethodObject("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] {
                Type.LONG
            })
    };


    private static boolean isString( Type type ) {
        return (type instanceof ObjectType) && 
              ((ObjectType) type).getClassName().equals("java.lang.String");
    }


    public Instruction createAppend( Type type ) {
        byte t = type.getType();
        if (isString(type)) {
            return createInvoke(append_mos[0], Constants.INVOKEVIRTUAL);
        }
        switch (t) {
            case Constants.T_BOOLEAN:
            case Constants.T_CHAR:
            case Constants.T_FLOAT:
            case Constants.T_DOUBLE:
            case Constants.T_BYTE:
            case Constants.T_SHORT:
            case Constants.T_INT:
            case Constants.T_LONG:
                return createInvoke(append_mos[t], Constants.INVOKEVIRTUAL);
            case Constants.T_ARRAY:
            case Constants.T_OBJECT:
                return createInvoke(append_mos[1], Constants.INVOKEVIRTUAL);
            default:
                throw new RuntimeException("Oops: No append for this type? " + type);
        }
    }


    /** Create a field instruction.
     *
     * @param class_name name of the accessed class
     * @param name name of the referenced field
     * @param type  type of field
     * @param kind how to access, i.e., GETFIELD, PUTFIELD, GETSTATIC, PUTSTATIC
     * @see Constants
     */
    public FieldInstruction createFieldAccess( String class_name, String name, Type type, short kind ) {
        int index;
        String signature = type.getSignature();
        index = cp.addFieldref(class_name, name, signature);
        switch (kind) {
            case Constants.GETFIELD:
                return new GETFIELD(index);
            case Constants.PUTFIELD:
                return new PUTFIELD(index);
            case Constants.GETSTATIC:
                return new GETSTATIC(index);
            case Constants.PUTSTATIC:
                return new PUTSTATIC(index);
            default:
                throw new RuntimeException("Oops: Unknown getfield kind:" + kind);
        }
    }


    /** Create reference to `this'
     */
    public static Instruction createThis() {
        return new ALOAD(0);
    }


    /** Create typed return
     */
    public static ReturnInstruction createReturn( Type type ) {
        switch (type.getType()) {
            case Constants.T_ARRAY:
            case Constants.T_OBJECT:
                return InstructionConstants.ARETURN;
            case Constants.T_INT:
            case Constants.T_SHORT:
            case Constants.T_BOOLEAN:
            case Constants.T_CHAR:
            case Constants.T_BYTE:
                return InstructionConstants.IRETURN;
            case Constants.T_FLOAT:
                return InstructionConstants.FRETURN;
            case Constants.T_DOUBLE:
                return InstructionConstants.DRETURN;
            case Constants.T_LONG:
                return InstructionConstants.LRETURN;
            case Constants.T_VOID:
                return InstructionConstants.RETURN;
            default:
                throw new RuntimeException("Invalid type: " + type);
        }
    }


    private static ArithmeticInstruction createBinaryIntOp( char first, String op ) {
        switch (first) {
            case '-':
                return InstructionConstants.ISUB;
            case '+':
                return InstructionConstants.IADD;
            case '%':
                return InstructionConstants.IREM;
            case '*':
                return InstructionConstants.IMUL;
            case '/':
                return InstructionConstants.IDIV;
            case '&':
                return InstructionConstants.IAND;
            case '|':
                return InstructionConstants.IOR;
            case '^':
                return InstructionConstants.IXOR;
            case '<':
                return InstructionConstants.ISHL;
            case '>':
                return op.equals(">>>") ? InstructionConstants.IUSHR : InstructionConstants.ISHR;
            default:
                throw new RuntimeException("Invalid operand " + op);
        }
    }


    private static ArithmeticInstruction createBinaryLongOp( char first, String op ) {
        switch (first) {
            case '-':
                return InstructionConstants.LSUB;
            case '+':
                return InstructionConstants.LADD;
            case '%':
                return InstructionConstants.LREM;
            case '*':
                return InstructionConstants.LMUL;
            case '/':
                return InstructionConstants.LDIV;
            case '&':
                return InstructionConstants.LAND;
            case '|':
                return InstructionConstants.LOR;
            case '^':
                return InstructionConstants.LXOR;
            case '<':
                return InstructionConstants.LSHL;
            case '>':
                return op.equals(">>>") ? InstructionConstants.LUSHR : InstructionConstants.LSHR;
            default:
                throw new RuntimeException("Invalid operand " + op);
        }
    }


    private static ArithmeticInstruction createBinaryFloatOp( char op ) {
        switch (op) {
            case '-':
                return InstructionConstants.FSUB;
            case '+':
                return InstructionConstants.FADD;
            case '*':
                return InstructionConstants.FMUL;
            case '/':
                return InstructionConstants.FDIV;
            case '%':
                return InstructionConstants.FREM;
            default:
                throw new RuntimeException("Invalid operand " + op);
        }
    }


    private static ArithmeticInstruction createBinaryDoubleOp( char op ) {
        switch (op) {
            case '-':
                return InstructionConstants.DSUB;
            case '+':
                return InstructionConstants.DADD;
            case '*':
                return InstructionConstants.DMUL;
            case '/':
                return InstructionConstants.DDIV;
            case '%':
                return InstructionConstants.DREM;
            default:
                throw new RuntimeException("Invalid operand " + op);
        }
    }


    /**
     * Create binary operation for simple basic types, such as int and float.
     *
     * @param op operation, such as "+", "*", "&lt;&lt;", etc.
     */
    public static ArithmeticInstruction createBinaryOperation( String op, Type type ) {
        char first = op.charAt(0);
        switch (type.getType()) {
            case Constants.T_BYTE:
            case Constants.T_SHORT:
            case Constants.T_INT:
            case Constants.T_CHAR:
                return createBinaryIntOp(first, op);
            case Constants.T_LONG:
                return createBinaryLongOp(first, op);
            case Constants.T_FLOAT:
                return createBinaryFloatOp(first);
            case Constants.T_DOUBLE:
                return createBinaryDoubleOp(first);
            default:
                throw new RuntimeException("Invalid type " + type);
        }
    }


    /**
     * @param size size of operand, either 1 (int, e.g.) or 2 (double)
     */
    public static StackInstruction createPop( int size ) {
        return (size == 2) ? InstructionConstants.POP2 : InstructionConstants.POP;
    }


    /**
     * @param size size of operand, either 1 (int, e.g.) or 2 (double)
     */
    public static StackInstruction createDup( int size ) {
        return (size == 2) ? InstructionConstants.DUP2 : InstructionConstants.DUP;
    }


    /**
     * @param size size of operand, either 1 (int, e.g.) or 2 (double)
     */
    public static StackInstruction createDup_2( int size ) {
        return (size == 2) ? InstructionConstants.DUP2_X2 : InstructionConstants.DUP_X2;
    }


    /**
     * @param size size of operand, either 1 (int, e.g.) or 2 (double)
     */
    public static StackInstruction createDup_1( int size ) {
        return (size == 2) ? InstructionConstants.DUP2_X1 : InstructionConstants.DUP_X1;
    }


    /**
     * @param index index of local variable
     */
    public static LocalVariableInstruction createStore( Type type, int index ) {
        switch (type.getType()) {
            case Constants.T_BOOLEAN:
            case Constants.T_CHAR:
            case Constants.T_BYTE:
            case Constants.T_SHORT:
            case Constants.T_INT:
                return new ISTORE(index);
            case Constants.T_FLOAT:
                return new FSTORE(index);
            case Constants.T_DOUBLE:
                return new DSTORE(index);
            case Constants.T_LONG:
                return new LSTORE(index);
            case Constants.T_ARRAY:
            case Constants.T_OBJECT:
                return new ASTORE(index);
            default:
                throw new RuntimeException("Invalid type " + type);
        }
    }


    /**
     * @param index index of local variable
     */
    public static LocalVariableInstruction createLoad( Type type, int index ) {
        switch (type.getType()) {
            case Constants.T_BOOLEAN:
            case Constants.T_CHAR:
            case Constants.T_BYTE:
            case Constants.T_SHORT:
            case Constants.T_INT:
                return new ILOAD(index);
            case Constants.T_FLOAT:
                return new FLOAD(index);
            case Constants.T_DOUBLE:
                return new DLOAD(index);
            case Constants.T_LONG:
                return new LLOAD(index);
            case Constants.T_ARRAY:
            case Constants.T_OBJECT:
                return new ALOAD(index);
            default:
                throw new RuntimeException("Invalid type " + type);
        }
    }


    /**
     * @param type type of elements of array, i.e., array.getElementType()
     */
    public static ArrayInstruction createArrayLoad( Type type ) {
        switch (type.getType()) {
            case Constants.T_BOOLEAN:
            case Constants.T_BYTE:
                return InstructionConstants.BALOAD;
            case Constants.T_CHAR:
                return InstructionConstants.CALOAD;
            case Constants.T_SHORT:
                return InstructionConstants.SALOAD;
            case Constants.T_INT:
                return InstructionConstants.IALOAD;
            case Constants.T_FLOAT:
                return InstructionConstants.FALOAD;
            case Constants.T_DOUBLE:
                return InstructionConstants.DALOAD;
            case Constants.T_LONG:
                return InstructionConstants.LALOAD;
            case Constants.T_ARRAY:
            case Constants.T_OBJECT:
                return InstructionConstants.AALOAD;
            default:
                throw new RuntimeException("Invalid type " + type);
        }
    }


    /**
     * @param type type of elements of array, i.e., array.getElementType()
     */
    public static ArrayInstruction createArrayStore( Type type ) {
        switch (type.getType()) {
            case Constants.T_BOOLEAN:
            case Constants.T_BYTE:
                return InstructionConstants.BASTORE;
            case Constants.T_CHAR:
                return InstructionConstants.CASTORE;
            case Constants.T_SHORT:
                return InstructionConstants.SASTORE;
            case Constants.T_INT:
                return InstructionConstants.IASTORE;
            case Constants.T_FLOAT:
                return InstructionConstants.FASTORE;
            case Constants.T_DOUBLE:
                return InstructionConstants.DASTORE;
            case Constants.T_LONG:
                return InstructionConstants.LASTORE;
            case Constants.T_ARRAY:
            case Constants.T_OBJECT:
                return InstructionConstants.AASTORE;
            default:
                throw new RuntimeException("Invalid type " + type);
        }
    }


    /** Create conversion operation for two stack operands, this may be an I2C, instruction, e.g.,
     * if the operands are basic types and CHECKCAST if they are reference types.
     */
    public Instruction createCast( Type src_type, Type dest_type ) {
        if ((src_type instanceof BasicType) && (dest_type instanceof BasicType)) {
            byte dest = dest_type.getType();
            byte src = src_type.getType();
            if (dest == Constants.T_LONG
                    && (src == Constants.T_CHAR || src == Constants.T_BYTE || src == Constants.T_SHORT)) {
                src = Constants.T_INT;
            }
            String name = "org.apache.commons.bcel6.generic." + short_names[src - Constants.T_CHAR] + "2"
                    + short_names[dest - Constants.T_CHAR];
            Instruction i = null;
            try {
                i = (Instruction) java.lang.Class.forName(name).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Could not find instruction: " + name, e);
            }
            return i;
        } else if ((src_type instanceof ReferenceType) && (dest_type instanceof ReferenceType)) {
            if (dest_type instanceof ArrayType) {
                return new CHECKCAST(cp.addArrayClass((ArrayType) dest_type));
            }
            return new CHECKCAST(cp.addClass(((ObjectType) dest_type).getClassName()));
        } else {
            throw new RuntimeException("Can not cast " + src_type + " to " + dest_type);
        }
    }


    public GETFIELD createGetField( String class_name, String name, Type t ) {
        return new GETFIELD(cp.addFieldref(class_name, name, t.getSignature()));
    }


    public GETSTATIC createGetStatic( String class_name, String name, Type t ) {
        return new GETSTATIC(cp.addFieldref(class_name, name, t.getSignature()));
    }


    public PUTFIELD createPutField( String class_name, String name, Type t ) {
        return new PUTFIELD(cp.addFieldref(class_name, name, t.getSignature()));
    }


    public PUTSTATIC createPutStatic( String class_name, String name, Type t ) {
        return new PUTSTATIC(cp.addFieldref(class_name, name, t.getSignature()));
    }


    public CHECKCAST createCheckCast( ReferenceType t ) {
        if (t instanceof ArrayType) {
            return new CHECKCAST(cp.addArrayClass((ArrayType) t));
        }
        return new CHECKCAST(cp.addClass((ObjectType) t));
    }


    public INSTANCEOF createInstanceOf( ReferenceType t ) {
        if (t instanceof ArrayType) {
            return new INSTANCEOF(cp.addArrayClass((ArrayType) t));
        }
        return new INSTANCEOF(cp.addClass((ObjectType) t));
    }


    public NEW createNew( ObjectType t ) {
        return new NEW(cp.addClass(t));
    }


    public NEW createNew( String s ) {
        return createNew(ObjectType.getInstance(s));
    }


    /** Create new array of given size and type.
     * @return an instruction that creates the corresponding array at runtime, i.e. is an AllocationInstruction
     */
    public Instruction createNewArray( Type t, short dim ) {
        if (dim == 1) {
            if (t instanceof ObjectType) {
                return new ANEWARRAY(cp.addClass((ObjectType) t));
            } else if (t instanceof ArrayType) {
                return new ANEWARRAY(cp.addArrayClass((ArrayType) t));
            } else {
                return new NEWARRAY(t.getType());
            }
        }
        ArrayType at;
        if (t instanceof ArrayType) {
            at = (ArrayType) t;
        } else {
            at = new ArrayType(t, dim);
        }
        return new MULTIANEWARRAY(cp.addArrayClass(at), dim);
    }


    /** Create "null" value for reference types, 0 for basic types like int
     */
    public static Instruction createNull( Type type ) {
        switch (type.getType()) {
            case Constants.T_ARRAY:
            case Constants.T_OBJECT:
                return InstructionConstants.ACONST_NULL;
            case Constants.T_INT:
            case Constants.T_SHORT:
            case Constants.T_BOOLEAN:
            case Constants.T_CHAR:
            case Constants.T_BYTE:
                return InstructionConstants.ICONST_0;
            case Constants.T_FLOAT:
                return InstructionConstants.FCONST_0;
            case Constants.T_DOUBLE:
                return InstructionConstants.DCONST_0;
            case Constants.T_LONG:
                return InstructionConstants.LCONST_0;
            case Constants.T_VOID:
                return InstructionConstants.NOP;
            default:
                throw new RuntimeException("Invalid type: " + type);
        }
    }


    /** Create branch instruction by given opcode, except LOOKUPSWITCH and TABLESWITCH.
     * For those you should use the SWITCH compound instruction.
     */
    public static BranchInstruction createBranchInstruction( short opcode, InstructionHandle target ) {
        switch (opcode) {
            case Constants.IFEQ:
                return new IFEQ(target);
            case Constants.IFNE:
                return new IFNE(target);
            case Constants.IFLT:
                return new IFLT(target);
            case Constants.IFGE:
                return new IFGE(target);
            case Constants.IFGT:
                return new IFGT(target);
            case Constants.IFLE:
                return new IFLE(target);
            case Constants.IF_ICMPEQ:
                return new IF_ICMPEQ(target);
            case Constants.IF_ICMPNE:
                return new IF_ICMPNE(target);
            case Constants.IF_ICMPLT:
                return new IF_ICMPLT(target);
            case Constants.IF_ICMPGE:
                return new IF_ICMPGE(target);
            case Constants.IF_ICMPGT:
                return new IF_ICMPGT(target);
            case Constants.IF_ICMPLE:
                return new IF_ICMPLE(target);
            case Constants.IF_ACMPEQ:
                return new IF_ACMPEQ(target);
            case Constants.IF_ACMPNE:
                return new IF_ACMPNE(target);
            case Constants.GOTO:
                return new GOTO(target);
            case Constants.JSR:
                return new JSR(target);
            case Constants.IFNULL:
                return new IFNULL(target);
            case Constants.IFNONNULL:
                return new IFNONNULL(target);
            case Constants.GOTO_W:
                return new GOTO_W(target);
            case Constants.JSR_W:
                return new JSR_W(target);
            default:
                throw new RuntimeException("Invalid opcode: " + opcode);
        }
    }


    public void setClassGen( ClassGen c ) {
        cg = c;
    }


    public ClassGen getClassGen() {
        return cg;
    }


    public void setConstantPool( ConstantPoolGen c ) {
        cp = c;
    }


    public ConstantPoolGen getConstantPool() {
        return cp;
    }
}
