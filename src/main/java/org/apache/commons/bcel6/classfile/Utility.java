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
 */

package org.apache.commons.bcel6.classfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.FilterReader;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.bcel6.Constants;
import org.apache.commons.bcel6.util.ByteSequence;

/**
 * Utility functions that do not really belong to any class in particular.
 *
 * @version $Id$
 */
// @since 6.0 methods are no longer final
public abstract class Utility {

    private static int unwrap( ThreadLocal<Integer> tl ) {
        return tl.get().intValue();
    }


    private static void wrap( ThreadLocal<Integer> tl, int value ) {
        tl.set(Integer.valueOf(value));
    }

    private static ThreadLocal<Integer> consumed_chars = new ThreadLocal<Integer>() {

        @Override
        protected Integer initialValue() {
            return Integer.valueOf(0);
        }
    };/* How many chars have been consumed
     * during parsing in signatureToString().
     * Read by methodSignatureToString().
     * Set by side effect,but only internally.
     */
    private static boolean wide = false; /* The `WIDE' instruction is used in the
     * byte code to allow 16-bit wide indices
     * for local variables. This opcode
     * precedes an `ILOAD', e.g.. The opcode
     * immediately following takes an extra
     * byte which is combined with the
     * following byte to form a
     * 16-bit value.
     */


    /**
     * Convert bit field of flags into string such as `static final'.
     *
     * @param  access_flags Access flags
     * @return String representation of flags
     */
    public static String accessToString( int access_flags ) {
        return accessToString(access_flags, false);
    }


    /**
     * Convert bit field of flags into string such as `static final'.
     *
     * Special case: Classes compiled with new compilers and with the
     * `ACC_SUPER' flag would be said to be "synchronized". This is
     * because SUN used the same value for the flags `ACC_SUPER' and
     * `ACC_SYNCHRONIZED'. 
     *
     * @param  access_flags Access flags
     * @param  for_class access flags are for class qualifiers ?
     * @return String representation of flags
     */
    public static String accessToString( int access_flags, boolean for_class ) {
        StringBuilder buf = new StringBuilder();
        int p = 0;
        for (int i = 0; p < Constants.MAX_ACC_FLAG; i++) { // Loop through known flags
            p = pow2(i);
            if ((access_flags & p) != 0) {
                /* Special case: Classes compiled with new compilers and with the
                 * `ACC_SUPER' flag would be said to be "synchronized". This is
                 * because SUN used the same value for the flags `ACC_SUPER' and
                 * `ACC_SYNCHRONIZED'.
                 */
                if (for_class && ((p == Constants.ACC_SUPER) || (p == Constants.ACC_INTERFACE))) {
                    continue;
                }
                buf.append(Constants.getAccessName(i)).append(" ");
            }
        }
        return buf.toString().trim();
    }


    /**
     * @param access_flags the class flags
     * 
     * @return "class" or "interface", depending on the ACC_INTERFACE flag
     */
    public static String classOrInterface( int access_flags ) {
        return ((access_flags & Constants.ACC_INTERFACE) != 0) ? "interface" : "class";
    }


    /**
     * Disassemble a byte array of JVM byte codes starting from code line 
     * `index' and return the disassembled string representation. Decode only
     * `num' opcodes (including their operands), use -1 if you want to
     * decompile everything.
     *
     * @param  code byte code array
     * @param  constant_pool Array of constants
     * @param  index offset in `code' array
     * <EM>(number of opcodes, not bytes!)</EM>
     * @param  length number of opcodes to decompile, -1 for all
     * @param  verbose be verbose, e.g. print constant pool index
     * @return String representation of byte codes
     */
    public static String codeToString( byte[] code, ConstantPool constant_pool, int index,
            int length, boolean verbose ) {
        StringBuilder buf = new StringBuilder(code.length * 20); // Should be sufficient // CHECKSTYLE IGNORE MagicNumber
        ByteSequence stream = new ByteSequence(code);
        try {
            for (int i = 0; i < index; i++) {
                codeToString(stream, constant_pool, verbose);
            }
            for (int i = 0; stream.available() > 0; i++) {
                if ((length < 0) || (i < length)) {
                    String indices = fillup(stream.getIndex() + ":", 6, true, ' ');
                    buf.append(indices).append(codeToString(stream, constant_pool, verbose))
                            .append('\n');
                }
            }
        } catch (IOException e) {
            System.out.println(buf.toString());
            e.printStackTrace();
            throw new ClassFormatException("Byte code error: " + e, e);
        }
        return buf.toString();
    }


    public static String codeToString( byte[] code, ConstantPool constant_pool, int index, int length ) {
        return codeToString(code, constant_pool, index, length, true);
    }


    /**
     * Disassemble a stream of byte codes and return the
     * string representation.
     *
     * @param  bytes stream of bytes
     * @param  constant_pool Array of constants
     * @param  verbose be verbose, e.g. print constant pool index
     * @return String representation of byte code
     * 
     * @throws IOException if a failure from reading from the bytes argument occurs
     */
    public static String codeToString( ByteSequence bytes, ConstantPool constant_pool,
            boolean verbose ) throws IOException {
        short opcode = (short) bytes.readUnsignedByte();
        int default_offset = 0;
        int low;
        int high;
        int npairs;
        int index;
        int vindex;
        int constant;
        int[] match;
        int[] jump_table;
        int no_pad_bytes = 0;
        int offset;
        StringBuilder buf = new StringBuilder(Constants.getOpcodeName(opcode));
        /* Special case: Skip (0-3) padding bytes, i.e., the
         * following bytes are 4-byte-aligned
         */
        if ((opcode == Constants.TABLESWITCH) || (opcode == Constants.LOOKUPSWITCH)) {
            int remainder = bytes.getIndex() % 4;
            no_pad_bytes = (remainder == 0) ? 0 : 4 - remainder;
            for (int i = 0; i < no_pad_bytes; i++) {
                byte b;
                if ((b = bytes.readByte()) != 0) {
                    System.err.println("Warning: Padding byte != 0 in "
                            + Constants.getOpcodeName(opcode) + ":" + b);
                }
            }
            // Both cases have a field default_offset in common
            default_offset = bytes.readInt();
        }
        switch (opcode) {
            /* Table switch has variable length arguments.
             */
            case Constants.TABLESWITCH:
                low = bytes.readInt();
                high = bytes.readInt();
                offset = bytes.getIndex() - 12 - no_pad_bytes - 1;
                default_offset += offset;
                buf.append("\tdefault = ").append(default_offset).append(", low = ").append(low)
                        .append(", high = ").append(high).append("(");
                jump_table = new int[high - low + 1];
                for (int i = 0; i < jump_table.length; i++) {
                    jump_table[i] = offset + bytes.readInt();
                    buf.append(jump_table[i]);
                    if (i < jump_table.length - 1) {
                        buf.append(", ");
                    }
                }
                buf.append(")");
                break;
            /* Lookup switch has variable length arguments.
             */
            case Constants.LOOKUPSWITCH: {
                npairs = bytes.readInt();
                offset = bytes.getIndex() - 8 - no_pad_bytes - 1;
                match = new int[npairs];
                jump_table = new int[npairs];
                default_offset += offset;
                buf.append("\tdefault = ").append(default_offset).append(", npairs = ").append(
                        npairs).append(" (");
                for (int i = 0; i < npairs; i++) {
                    match[i] = bytes.readInt();
                    jump_table[i] = offset + bytes.readInt();
                    buf.append("(").append(match[i]).append(", ").append(jump_table[i]).append(")");
                    if (i < npairs - 1) {
                        buf.append(", ");
                    }
                }
                buf.append(")");
            }
                break;
            /* Two address bytes + offset from start of byte stream form the
             * jump target
             */
            case Constants.GOTO:
            case Constants.IFEQ:
            case Constants.IFGE:
            case Constants.IFGT:
            case Constants.IFLE:
            case Constants.IFLT:
            case Constants.JSR:
            case Constants.IFNE:
            case Constants.IFNONNULL:
            case Constants.IFNULL:
            case Constants.IF_ACMPEQ:
            case Constants.IF_ACMPNE:
            case Constants.IF_ICMPEQ:
            case Constants.IF_ICMPGE:
            case Constants.IF_ICMPGT:
            case Constants.IF_ICMPLE:
            case Constants.IF_ICMPLT:
            case Constants.IF_ICMPNE:
                buf.append("\t\t#").append((bytes.getIndex() - 1) + bytes.readShort());
                break;
            /* 32-bit wide jumps
             */
            case Constants.GOTO_W:
            case Constants.JSR_W:
                buf.append("\t\t#").append((bytes.getIndex() - 1) + bytes.readInt());
                break;
            /* Index byte references local variable (register)
             */
            case Constants.ALOAD:
            case Constants.ASTORE:
            case Constants.DLOAD:
            case Constants.DSTORE:
            case Constants.FLOAD:
            case Constants.FSTORE:
            case Constants.ILOAD:
            case Constants.ISTORE:
            case Constants.LLOAD:
            case Constants.LSTORE:
            case Constants.RET:
                if (wide) {
                    vindex = bytes.readUnsignedShort();
                    wide = false; // Clear flag
                } else {
                    vindex = bytes.readUnsignedByte();
                }
                buf.append("\t\t%").append(vindex);
                break;
            /*
             * Remember wide byte which is used to form a 16-bit address in the
             * following instruction. Relies on that the method is called again with
             * the following opcode.
             */
            case Constants.WIDE:
                wide = true;
                buf.append("\t(wide)");
                break;
            /* Array of basic type.
             */
            case Constants.NEWARRAY:
                buf.append("\t\t<").append(Constants.getTypeName(bytes.readByte())).append(">");
                break;
            /* Access object/class fields.
             */
            case Constants.GETFIELD:
            case Constants.GETSTATIC:
            case Constants.PUTFIELD:
            case Constants.PUTSTATIC:
                index = bytes.readUnsignedShort();
                buf.append("\t\t").append(
                        constant_pool.constantToString(index, Constants.CONSTANT_Fieldref)).append(
                        verbose ? " (" + index + ")" : "");
                break;
            /* Operands are references to classes in constant pool
             */
            case Constants.NEW:
            case Constants.CHECKCAST:
                buf.append("\t");
                //$FALL-THROUGH$
            case Constants.INSTANCEOF:
                index = bytes.readUnsignedShort();
                buf.append("\t<").append(
                        constant_pool.constantToString(index, Constants.CONSTANT_Class))
                        .append(">").append(verbose ? " (" + index + ")" : "");
                break;
            /* Operands are references to methods in constant pool
             */
            case Constants.INVOKESPECIAL:
            case Constants.INVOKESTATIC:
                index = bytes.readUnsignedShort();
                Constant c = constant_pool.getConstant(index);
                // With Java8 operand may be either a CONSTANT_Methodref
                // or a CONSTANT_InterfaceMethodref.   (markro)
                buf.append("\t").append(
                        constant_pool.constantToString(index, c.getTag()))
                        .append(verbose ? " (" + index + ")" : "");
                break;
            case Constants.INVOKEVIRTUAL:
                index = bytes.readUnsignedShort();
                buf.append("\t").append(
                        constant_pool.constantToString(index, Constants.CONSTANT_Methodref))
                        .append(verbose ? " (" + index + ")" : "");
                break;
            case Constants.INVOKEINTERFACE:
                index = bytes.readUnsignedShort();
                int nargs = bytes.readUnsignedByte(); // historical, redundant
                buf.append("\t").append(
                        constant_pool
                                .constantToString(index, Constants.CONSTANT_InterfaceMethodref))
                        .append(verbose ? " (" + index + ")\t" : "").append(nargs).append("\t")
                        .append(bytes.readUnsignedByte()); // Last byte is a reserved space
                break;
            case Constants.INVOKEDYNAMIC:
                index = bytes.readUnsignedShort();
                buf.append("\t").append(
                        constant_pool
                                .constantToString(index, Constants.CONSTANT_InvokeDynamic))
                        .append(verbose ? " (" + index + ")\t" : "")
                        .append(bytes.readUnsignedByte())  // Thrid byte is a reserved space
                        .append(bytes.readUnsignedByte()); // Last byte is a reserved space
                break;
            /* Operands are references to items in constant pool
             */
            case Constants.LDC_W:
            case Constants.LDC2_W:
                index = bytes.readUnsignedShort();
                buf.append("\t\t").append(
                        constant_pool.constantToString(index, constant_pool.getConstant(index)
                                .getTag())).append(verbose ? " (" + index + ")" : "");
                break;
            case Constants.LDC:
                index = bytes.readUnsignedByte();
                buf.append("\t\t").append(
                        constant_pool.constantToString(index, constant_pool.getConstant(index)
                                .getTag())).append(verbose ? " (" + index + ")" : "");
                break;
            /* Array of references.
             */
            case Constants.ANEWARRAY:
                index = bytes.readUnsignedShort();
                buf.append("\t\t<").append(
                        compactClassName(constant_pool.getConstantString(index,
                                Constants.CONSTANT_Class), false)).append(">").append(
                        verbose ? " (" + index + ")" : "");
                break;
            /* Multidimensional array of references.
             */
            case Constants.MULTIANEWARRAY: {
                index = bytes.readUnsignedShort();
                int dimensions = bytes.readUnsignedByte();
                buf.append("\t<").append(
                        compactClassName(constant_pool.getConstantString(index,
                                Constants.CONSTANT_Class), false)).append(">\t").append(dimensions)
                        .append(verbose ? " (" + index + ")" : "");
            }
                break;
            /* Increment local variable.
             */
            case Constants.IINC:
                if (wide) {
                    vindex = bytes.readUnsignedShort();
                    constant = bytes.readShort();
                    wide = false;
                } else {
                    vindex = bytes.readUnsignedByte();
                    constant = bytes.readByte();
                }
                buf.append("\t\t%").append(vindex).append("\t").append(constant);
                break;
            default:
                if (Constants.getNoOfOperands(opcode) > 0) {
                    for (int i = 0; i < Constants.getOperandTypeCount(opcode); i++) {
                        buf.append("\t\t");
                        switch (Constants.getOperandType(opcode, i)) {
                            case Constants.T_BYTE:
                                buf.append(bytes.readByte());
                                break;
                            case Constants.T_SHORT:
                                buf.append(bytes.readShort());
                                break;
                            case Constants.T_INT:
                                buf.append(bytes.readInt());
                                break;
                            default: // Never reached
                                System.err.println("Unreachable default case reached!");
                                System.exit(-1);
                        }
                    }
                }
        }
        return buf.toString();
    }


    public static String codeToString( ByteSequence bytes, ConstantPool constant_pool )
            throws IOException {
        return codeToString(bytes, constant_pool, true);
    }


    /**
     * Shorten long class names, <em>java/lang/String</em> becomes 
     * <em>String</em>.
     *
     * @param str The long class name
     * @return Compacted class name
     */
    public static String compactClassName( String str ) {
        return compactClassName(str, true);
    }


    /**
     * Shorten long class name <em>str</em>, i.e., chop off the <em>prefix</em>,
     * if the
     * class name starts with this string and the flag <em>chopit</em> is true.
     * Slashes <em>/</em> are converted to dots <em>.</em>.
     *
     * @param str The long class name
     * @param prefix The prefix the get rid off
     * @param chopit Flag that determines whether chopping is executed or not
     * @return Compacted class name
     */
    public static String compactClassName( String str, String prefix, boolean chopit ) {
        int len = prefix.length();
        str = str.replace('/', '.'); // Is `/' on all systems, even DOS
        if (chopit) {
            // If string starts with `prefix' and contains no further dots
            if (str.startsWith(prefix) && (str.substring(len).indexOf('.') == -1)) {
                str = str.substring(len);
            }
        }
        return str;
    }


    /**
     * Shorten long class names, <em>java/lang/String</em> becomes 
     * <em>java.lang.String</em>,
     * e.g.. If <em>chopit</em> is <em>true</em> the prefix <em>java.lang</em>
     * is also removed.
     *
     * @param str The long class name
     * @param chopit Flag that determines whether chopping is executed or not
     * @return Compacted class name
     */
    public static String compactClassName( String str, boolean chopit ) {
        return compactClassName(str, "java.lang.", chopit);
    }


    /**
     * @return `flag' with bit `i' set to 1
     */
    public static int setBit( int flag, int i ) {
        return flag | pow2(i);
    }


    /**
     * @return `flag' with bit `i' set to 0
     */
    public static int clearBit( int flag, int i ) {
        int bit = pow2(i);
        return (flag & bit) == 0 ? flag : flag ^ bit;
    }


    /**
     * @return true, if bit `i' in `flag' is set
     */
    public static boolean isSet( int flag, int i ) {
        return (flag & pow2(i)) != 0;
    }


    /**
     * Converts string containing the method return and argument types 
     * to a byte code method signature.
     *
     * @param  ret Return type of method
     * @param  argv Types of method arguments
     * @return Byte code representation of method signature
     * 
     * @throws ClassFormatException if the signature is for Void
     */
    public static String methodTypeToSignature( String ret, String[] argv )
            throws ClassFormatException {
        StringBuilder buf = new StringBuilder("(");
        String str;
        if (argv != null) {
            for (String element : argv) {
                str = getSignature(element);
                if (str.endsWith("V")) {
                    throw new ClassFormatException("Invalid type: " + element);
                }
                buf.append(str);
            }
        }
        str = getSignature(ret);
        buf.append(")").append(str);
        return buf.toString();
    }


    /**
     * @param  signature    Method signature
     * @return Array of argument types
     * @throws  ClassFormatException  
     */
    public static String[] methodSignatureArgumentTypes( String signature )
            throws ClassFormatException {
        return methodSignatureArgumentTypes(signature, true);
    }


    /**
     * @param  signature    Method signature
     * @param chopit Shorten class names ?
     * @return Array of argument types
     * @throws  ClassFormatException  
     */
    public static String[] methodSignatureArgumentTypes( String signature, boolean chopit ) 
            throws ClassFormatException {
        List<String> vec = new ArrayList<>();
        int index;
        try { // Read all declarations between for `(' and `)'
            if (signature.charAt(0) != '(') {
                throw new ClassFormatException("Invalid method signature: " + signature);
            }
            index = 1; // current string position
            while (signature.charAt(index) != ')') {
                vec.add(signatureToString(signature.substring(index), chopit));
                //corrected concurrent private static field acess
                index += unwrap(consumed_chars); // update position
            }
        } catch (StringIndexOutOfBoundsException e) { // Should never occur
            throw new ClassFormatException("Invalid method signature: " + signature, e);
        }
        return vec.toArray(new String[vec.size()]);
    }


    /**
     * @param  signature    Method signature
     * @return return type of method
     * @throws  ClassFormatException  
     */
    public static String methodSignatureReturnType( String signature ) throws ClassFormatException {
        return methodSignatureReturnType(signature, true);
    }


    /**
     * @param  signature    Method signature
     * @param chopit Shorten class names ?
     * @return return type of method
     * @throws  ClassFormatException  
     */
    public static String methodSignatureReturnType( String signature, boolean chopit ) throws ClassFormatException {
        int index;
        String type;
        try {
            // Read return type after `)'
            index = signature.lastIndexOf(')') + 1;
            type = signatureToString(signature.substring(index), chopit);
        } catch (StringIndexOutOfBoundsException e) { // Should never occur
            throw new ClassFormatException("Invalid method signature: " + signature, e);
        }
        return type;
    }


    /**
     * Converts method signature to string with all class names compacted.
     *
     * @param signature to convert
     * @param name of method
     * @param access flags of method
     * @return Human readable signature
     */
    public static String methodSignatureToString( String signature, String name, String access ) {
        return methodSignatureToString(signature, name, access, true);
    }


    public static String methodSignatureToString( String signature, String name, String access, boolean chopit ) {
        return methodSignatureToString(signature, name, access, chopit, null);
    }


    /**
     * A returntype signature represents the return value from a method.
     * It is a series of bytes in the following grammar:
     *
     * <pre>
     * &lt;return_signature&gt; ::= &lt;field_type&gt; | V
     * </pre>
     *
     * The character V indicates that the method returns no value. Otherwise, the
     * signature indicates the type of the return value.
     * An argument signature represents an argument passed to a method:
     *
     * <pre>
     * &lt;argument_signature&gt; ::= &lt;field_type&gt;
     * </pre>
     *
     * A method signature represents the arguments that the method expects, and
     * the value that it returns.
     * <pre>
     * &lt;method_signature&gt; ::= (&lt;arguments_signature&gt;) &lt;return_signature&gt;
     * &lt;arguments_signature&gt;::= &lt;argument_signature&gt;*
     * </pre>
     *
     * This method converts such a string into a Java type declaration like
     * `void main(String[])' and throws a `ClassFormatException' when the parsed 
     * type is invalid.
     *
     * @param  signature    Method signature
     * @param  name         Method name
     * @param  access       Method access rights
     * @param chopit
     * @param vars
     * @return Java type declaration
     * @throws  ClassFormatException  
     */
    public static String methodSignatureToString( String signature, String name,
            String access, boolean chopit, LocalVariableTable vars ) throws ClassFormatException {
        StringBuilder buf = new StringBuilder("(");
        String type;
        int index;
        int var_index = access.contains("static") ? 0 : 1;
        try { // Read all declarations between for `(' and `)'
            if (signature.charAt(0) != '(') {
                throw new ClassFormatException("Invalid method signature: " + signature);
            }
            index = 1; // current string position
            while (signature.charAt(index) != ')') {
                String param_type = signatureToString(signature.substring(index), chopit);
                buf.append(param_type);
                if (vars != null) {
                    LocalVariable l = vars.getLocalVariable(var_index, 0);
                    if (l != null) {
                        buf.append(" ").append(l.getName());
                    }
                } else {
                    buf.append(" arg").append(var_index);
                }
                if ("double".equals(param_type) || "long".equals(param_type)) {
                    var_index += 2;
                } else {
                    var_index++;
                }
                buf.append(", ");
                //corrected concurrent private static field acess
                index += unwrap(consumed_chars); // update position
            }
            index++; // update position
            // Read return type after `)'
            type = signatureToString(signature.substring(index), chopit);
        } catch (StringIndexOutOfBoundsException e) { // Should never occur
            throw new ClassFormatException("Invalid method signature: " + signature, e);
        }
        if (buf.length() > 1) {
            buf.setLength(buf.length() - 2);
        }
        buf.append(")");
        return access + ((access.length() > 0) ? " " : "") + // May be an empty string
                type + " " + name + buf.toString();
    }


    // Guess what this does
    private static int pow2( int n ) {
        return 1 << n;
    }


    /**
     * Replace all occurrences of <em>old</em> in <em>str</em> with <em>new</em>.
     *
     * @param str String to permute
     * @param old String to be replaced
     * @param new_ Replacement string
     * @return new String object
     */
    public static String replace( String str, String old, String new_ ) {
        int index;
        int old_index;
        try {
            if (str.contains(old)) { // `old' found in str
                StringBuilder buf = new StringBuilder();
                old_index = 0; // String start offset
                // While we have something to replace
                while ((index = str.indexOf(old, old_index)) != -1) {
                    buf.append(str.substring(old_index, index)); // append prefix
                    buf.append(new_); // append replacement
                    old_index = index + old.length(); // Skip `old'.length chars
                }
                buf.append(str.substring(old_index)); // append rest of string
                str = buf.toString();
            }
        } catch (StringIndexOutOfBoundsException e) { // Should not occur
            System.err.println(e);
        }
        return str;
    }


    /**
     * Converts signature to string with all class names compacted.
     *
     * @param signature to convert
     * @return Human readable signature
     */
    public static String signatureToString( String signature ) {
        return signatureToString(signature, true);
    }


    /**
     * The field signature represents the value of an argument to a function or 
     * the value of a variable. It is a series of bytes generated by the 
     * following grammar:
     *
     * <PRE>
     * &lt;field_signature&gt; ::= &lt;field_type&gt;
     * &lt;field_type&gt;      ::= &lt;base_type&gt;|&lt;object_type&gt;|&lt;array_type&gt;
     * &lt;base_type&gt;       ::= B|C|D|F|I|J|S|Z
     * &lt;object_type&gt;     ::= L&lt;fullclassname&gt;;
     * &lt;array_type&gt;      ::= [&lt;field_type&gt;
     *
     * The meaning of the base types is as follows:
     * B byte signed byte
     * C char character
     * D double double precision IEEE float
     * F float single precision IEEE float
     * I int integer
     * J long long integer
     * L&lt;fullclassname&gt;; ... an object of the given class
     * S short signed short
     * Z boolean true or false
     * [&lt;field sig&gt; ... array
     * </PRE>
     *
     * This method converts this string into a Java type declaration such as
     * `String[]' and throws a `ClassFormatException' when the parsed type is 
     * invalid.
     *
     * @param  signature  Class signature
     * @param chopit Flag that determines whether chopping is executed or not
     * @return Java type declaration
     * @throws ClassFormatException
     */
    public static String signatureToString( String signature, boolean chopit ) {
        //corrected concurrent private static field acess
        wrap(consumed_chars, 1); // This is the default, read just one char like `B'
        try {
            switch (signature.charAt(0)) {
                case 'B':
                    return "byte";
                case 'C':
                    return "char";
                case 'D':
                    return "double";
                case 'F':
                    return "float";
                case 'I':
                    return "int";
                case 'J':
                    return "long";
                case 'T': { // TypeVariableSignature
                    int index = signature.indexOf(';'); // Look for closing `;'
                    if (index < 0) {
                        throw new ClassFormatException("Invalid signature: " + signature);
                    }
                    //corrected concurrent private static field acess
                    wrap(consumed_chars, index + 1); // "Tblabla;" `T' and `;' are removed
                    return compactClassName(signature.substring(1, index), chopit);
                }
                case 'L': { // Full class name
                    // should this be a while loop? can there be more than
                    // one generic clause?  (markro)
                    int fromIndex = signature.indexOf('<'); // generic type?
                    if (fromIndex < 0) {
                        fromIndex = 0;
                    } else {
                        fromIndex = signature.indexOf('>', fromIndex);
                        if (fromIndex < 0) {
                            throw new ClassFormatException("Invalid signature: " + signature);
                        }
                    }
                    int index = signature.indexOf(';', fromIndex); // Look for closing `;'
                    if (index < 0) {
                        throw new ClassFormatException("Invalid signature: " + signature);
                    }
                    // check to see if there are any TypeArguments
                    int bracketIndex = signature.substring(0, index).indexOf('<');
                    if (bracketIndex < 0) {
                        // just a class identifier
                        wrap(consumed_chars, index + 1); // "Lblabla;" `L' and `;' are removed
                        return compactClassName(signature.substring(1, index), chopit);
                    }

                    // we have TypeArguments; build up partial result
                    // as we recurse for each TypeArgument
                    StringBuilder type = new StringBuilder(compactClassName(signature.substring(1, bracketIndex), chopit)).append("<");
                    int consumed_chars = bracketIndex + 1; // Shadows global var

                    // check for wildcards
                    if (signature.charAt(consumed_chars) == '+') {
                        type.append("? extends ");
                        consumed_chars++;
                    } else if (signature.charAt(consumed_chars) == '-') {
                        type.append("? super ");
                        consumed_chars++;
                    } else if (signature.charAt(consumed_chars) == '*') {
                        // must be at end of signature
                        if (signature.charAt(consumed_chars + 1) != '>') {
                            throw new ClassFormatException("Invalid signature: " + signature);
                        }
                        if (signature.charAt(consumed_chars + 2) != ';') {
                            throw new ClassFormatException("Invalid signature: " + signature);
                        }
                        wrap(Utility.consumed_chars, consumed_chars + 3); // remove final "*>;"
                        return type + "?>...";
                    }

                    // get the first TypeArgument
                    type.append(signatureToString(signature.substring(consumed_chars), chopit));
                    // update our consumed count by the number of characters the for type argument
                    consumed_chars = unwrap(Utility.consumed_chars) + consumed_chars;
                    wrap(Utility.consumed_chars, consumed_chars);

                    // are there more TypeArguments?
                    while (signature.charAt(consumed_chars) != '>') {
                        type.append(", ").append(signatureToString(signature.substring(consumed_chars), chopit));
                        // update our consumed count by the number of characters the for type argument
                        consumed_chars = unwrap(Utility.consumed_chars) + consumed_chars;
                        wrap(Utility.consumed_chars, consumed_chars);
                    }

                    if (signature.charAt(consumed_chars + 1) != ';') {
                        throw new ClassFormatException("Invalid signature: " + signature);
                    }
                    wrap(Utility.consumed_chars, consumed_chars + 2); // remove final ">;"
                    return type.append(">").toString();
                }
                case 'S':
                    return "short";
                case 'Z':
                    return "boolean";
                case '[': { // Array declaration
                    int n;
                    StringBuilder brackets;
                    String type;
                    int consumed_chars; // Shadows global var
                    brackets = new StringBuilder(); // Accumulate []'s
                    // Count opening brackets and look for optional size argument
                    for (n = 0; signature.charAt(n) == '['; n++) {
                        brackets.append("[]");
                    }
                    consumed_chars = n; // Remember value
                    // The rest of the string denotes a `<field_type>'
                    type = signatureToString(signature.substring(n), chopit);
                    //corrected concurrent private static field acess
                    //Utility.consumed_chars += consumed_chars; is replaced by:
                    int _temp = unwrap(Utility.consumed_chars) + consumed_chars;
                    wrap(Utility.consumed_chars, _temp);
                    return type + brackets.toString();
                }
                case 'V':
                    return "void";
                default:
                    throw new ClassFormatException("Invalid signature: `" + signature + "'");
            }
        } catch (StringIndexOutOfBoundsException e) { // Should never occur
            throw new ClassFormatException("Invalid signature: " + signature, e);
        }
    }


    /** Parse Java type such as "char", or "java.lang.String[]" and return the
     * signature in byte code format, e.g. "C" or "[Ljava/lang/String;" respectively.
     *
     * @param  type Java type
     * @return byte code signature
     */
    public static String getSignature( String type ) {
        StringBuilder buf = new StringBuilder();
        char[] chars = type.toCharArray();
        boolean char_found = false;
        boolean delim = false;
        int index = -1;
        loop: for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                    if (char_found) {
                        delim = true;
                    }
                    break;
                case '[':
                    if (!char_found) {
                        throw new RuntimeException("Illegal type: " + type);
                    }
                    index = i;
                    break loop;
                default:
                    char_found = true;
                    if (!delim) {
                        buf.append(chars[i]);
                    }
            }
        }
        int brackets = 0;
        if (index > 0) {
            brackets = countBrackets(type.substring(index));
        }
        type = buf.toString();
        buf.setLength(0);
        for (int i = 0; i < brackets; i++) {
            buf.append('[');
        }
        boolean found = false;
        for (int i = Constants.T_BOOLEAN; (i <= Constants.T_VOID) && !found; i++) {
            if (Constants.getTypeName(i).equals(type)) {
                found = true;
                buf.append(Constants.getShortTypeName(i));
            }
        }
        if (!found) {
            buf.append('L').append(type.replace('.', '/')).append(';');
        }
        return buf.toString();
    }


    private static int countBrackets( String brackets ) {
        char[] chars = brackets.toCharArray();
        int count = 0;
        boolean open = false;
        for (char c : chars) {
            switch (c) {
                case '[':
                    if (open) {
                        throw new RuntimeException("Illegally nested brackets:" + brackets);
                    }
                    open = true;
                    break;
                case ']':
                    if (!open) {
                        throw new RuntimeException("Illegally nested brackets:" + brackets);
                    }
                    open = false;
                    count++;
                    break;
                default:
                    // Don't care
                    break;
            }
        }
        if (open) {
            throw new RuntimeException("Illegally nested brackets:" + brackets);
        }
        return count;
    }


    /**
     * Return type of method signature as a byte value as defined in <em>Constants</em>
     *
     * @param  signature in format described above
     * @return type of method signature
     * @see    Constants
     * 
     * @throws ClassFormatException if signature is not a method signature
     */
    public static byte typeOfMethodSignature( String signature ) throws ClassFormatException {
        int index;
        try {
            if (signature.charAt(0) != '(') {
                throw new ClassFormatException("Invalid method signature: " + signature);
            }
            index = signature.lastIndexOf(')') + 1;
            return typeOfSignature(signature.substring(index));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ClassFormatException("Invalid method signature: " + signature, e);
        }
    }


    /**
     * Return type of signature as a byte value as defined in <em>Constants</em>
     *
     * @param  signature in format described above
     * @return type of signature
     * @see    Constants
     * 
     * @throws ClassFormatException if signature isn't a known type
     */
    public static byte typeOfSignature( String signature ) throws ClassFormatException {
        try {
            switch (signature.charAt(0)) {
                case 'B':
                    return Constants.T_BYTE;
                case 'C':
                    return Constants.T_CHAR;
                case 'D':
                    return Constants.T_DOUBLE;
                case 'F':
                    return Constants.T_FLOAT;
                case 'I':
                    return Constants.T_INT;
                case 'J':
                    return Constants.T_LONG;
                case 'L':
                case 'T':
                    return Constants.T_REFERENCE;
                case '[':
                    return Constants.T_ARRAY;
                case 'V':
                    return Constants.T_VOID;
                case 'Z':
                    return Constants.T_BOOLEAN;
                case 'S':
                    return Constants.T_SHORT;
                default:
                    throw new ClassFormatException("Invalid method signature: " + signature);
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new ClassFormatException("Invalid method signature: " + signature, e);
        }
    }


    /** Map opcode names to opcode numbers. E.g., return Constants.ALOAD for "aload"
     */
    public static short searchOpcode( String name ) {
        name = name.toLowerCase(Locale.ENGLISH);
        for (short i = 0; i < Constants.OPCODE_NAMES_LENGTH; i++) {
            if (Constants.getOpcodeName(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Convert (signed) byte to (unsigned) short value, i.e., all negative
     * values become positive.
     */
    private static short byteToShort( byte b ) {
        return (b < 0) ? (short) (256 + b) : (short) b;
    }


    /** Convert bytes into hexadecimal string
     *
     * @param bytes an array of bytes to convert to hexadecimal
     * 
     * @return bytes as hexadecimal string, e.g. 00 fa 12 ...
     */
    public static String toHexString( byte[] bytes ) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            short b = byteToShort(bytes[i]);
            String hex = Integer.toHexString(b);
            if (b < 0x10) {
                buf.append('0');
            }
            buf.append(hex);
            if (i < bytes.length - 1) {
                buf.append(' ');
            }
        }
        return buf.toString();
    }


    /**
     * Return a string for an integer justified left or right and filled up with
     * `fill' characters if necessary.
     *
     * @param i integer to format
     * @param length length of desired string
     * @param left_justify format left or right
     * @param fill fill character
     * @return formatted int
     */
    public static String format( int i, int length, boolean left_justify, char fill ) {
        return fillup(Integer.toString(i), length, left_justify, fill);
    }


    /**
     * Fillup char with up to length characters with char `fill' and justify it left or right.
     *
     * @param str string to format
     * @param length length of desired string
     * @param left_justify format left or right
     * @param fill fill character
     * @return formatted string
     */
    public static String fillup( String str, int length, boolean left_justify, char fill ) {
        int len = length - str.length();
        char[] buf = new char[(len < 0) ? 0 : len];
        for (int j = 0; j < buf.length; j++) {
            buf[j] = fill;
        }
        if (left_justify) {
            return str + new String(buf);
        }
        return new String(buf) + str;
    }


    static boolean equals( byte[] a, byte[] b ) {
        int size;
        if ((size = a.length) != b.length) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }


    public static void printArray( PrintStream out, Object[] obj ) {
        out.println(printArray(obj, true));
    }


    public static void printArray( PrintWriter out, Object[] obj ) {
        out.println(printArray(obj, true));
    }


    public static String printArray( Object[] obj ) {
        return printArray(obj, true);
    }


    public static String printArray( Object[] obj, boolean braces ) {
        return printArray(obj, braces, false);
    }


    public static String printArray( Object[] obj, boolean braces, boolean quote ) {
        if (obj == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        if (braces) {
            buf.append('{');
        }
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] != null) {
                buf.append(quote ? "\"" : "").append(obj[i]).append(quote ? "\"" : "");
            } else {
                buf.append("null");
            }
            if (i < obj.length - 1) {
                buf.append(", ");
            }
        }
        if (braces) {
            buf.append('}');
        }
        return buf.toString();
    }


    /** 
     * @param ch the character to test if it's part of an identifier
     * 
     * @return true, if character is one of (a, ... z, A, ... Z, 0, ... 9, _)
     */
    public static boolean isJavaIdentifierPart( char ch ) {
        return ((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z'))
                || ((ch >= '0') && (ch <= '9')) || (ch == '_');
    }


    /**
     * Encode byte array it into Java identifier string, i.e., a string
     * that only contains the following characters: (a, ... z, A, ... Z,
     * 0, ... 9, _, $).  The encoding algorithm itself is not too
     * clever: if the current byte's ASCII value already is a valid Java
     * identifier part, leave it as it is. Otherwise it writes the
     * escape character($) followed by:
     * 
     * <ul>
     *   <li> the ASCII value as a hexadecimal string, if the value is not in the range 200..247</li>
     *   <li>a Java identifier char not used in a lowercase hexadecimal string, if the value is in the range 200..247</li>
     * </ul>
     *
     * <p>This operation inflates the original byte array by roughly 40-50%</p>
     *
     * @param bytes the byte array to convert
     * @param compress use gzip to minimize string
     * 
     * @throws IOException if there's a gzip exception
     */
    public static String encode( byte[] bytes, boolean compress ) throws IOException {
        if (compress) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(baos);
            gos.write(bytes, 0, bytes.length);
            gos.close();
            baos.close();
            bytes = baos.toByteArray();
        }
        CharArrayWriter caw = new CharArrayWriter();
        JavaWriter jw = new JavaWriter(caw);
        for (byte b : bytes) {
            int in = b & 0x000000ff; // Normalize to unsigned
            jw.write(in);
        }
        jw.close();
        return caw.toString();
    }


    /**
     * Decode a string back to a byte array.
     *
     * @param s the string to convert
     * @param uncompress use gzip to uncompress the stream of bytes
     * 
     * @throws IOException if there's a gzip exception
     */
    public static byte[] decode( String s, boolean uncompress ) throws IOException {
        char[] chars = s.toCharArray();
        CharArrayReader car = new CharArrayReader(chars);
        JavaReader jr = new JavaReader(car);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int ch;
        while ((ch = jr.read()) >= 0) {
            bos.write(ch);
        }
        bos.close();
        car.close();
        jr.close();
        byte[] bytes = bos.toByteArray();
        if (uncompress) {
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
            byte[] tmp = new byte[bytes.length * 3]; // Rough estimate
            int count = 0;
            int b;
            while ((b = gis.read()) >= 0) {
                tmp[count++] = (byte) b;
            }
            bytes = new byte[count];
            System.arraycopy(tmp, 0, bytes, 0, count);
        }
        return bytes;
    }

    // A-Z, g-z, _, $
    private static final int FREE_CHARS = 48;
    private static int[] CHAR_MAP = new int[FREE_CHARS];
    private static int[] MAP_CHAR = new int[256]; // Reverse map
    private static final char ESCAPE_CHAR = '$';
    static {
        int j = 0;
        for (int i = 'A'; i <= 'Z'; i++) {
            CHAR_MAP[j] = i;
            MAP_CHAR[i] = j;
            j++;
        }
        for (int i = 'g'; i <= 'z'; i++) {
            CHAR_MAP[j] = i;
            MAP_CHAR[i] = j;
            j++;
        }
        CHAR_MAP[j] = '$';
        MAP_CHAR['$'] = j;
        j++;
        CHAR_MAP[j] = '_';
        MAP_CHAR['_'] = j;
    }

    /**
     * Decode characters into bytes.
     * Used by <a href="Utility.html#decode(java.lang.String, boolean)">decode()</a>
     */
    private static class JavaReader extends FilterReader {

        public JavaReader(Reader in) {
            super(in);
        }


        @Override
        public int read() throws IOException {
            int b = in.read();
            if (b != ESCAPE_CHAR) {
                return b;
            }
            int i = in.read();
            if (i < 0) {
                return -1;
            }
            if (((i >= '0') && (i <= '9')) || ((i >= 'a') && (i <= 'f'))) { // Normal escape
                int j = in.read();
                if (j < 0) {
                    return -1;
                }
                char[] tmp = {
                        (char) i, (char) j
                };
                int s = Integer.parseInt(new String(tmp), 16);
                return s;
            }
            return MAP_CHAR[i];
        }


        @Override
        public int read( char[] cbuf, int off, int len ) throws IOException {
            for (int i = 0; i < len; i++) {
                cbuf[off + i] = (char) read();
            }
            return len;
        }
    }

    /**
     * Encode bytes into valid java identifier characters.
     * Used by <a href="Utility.html#encode(byte[], boolean)">encode()</a>
     */
    private static class JavaWriter extends FilterWriter {

        public JavaWriter(Writer out) {
            super(out);
        }


        @Override
        public void write( int b ) throws IOException {
            if (isJavaIdentifierPart((char) b) && (b != ESCAPE_CHAR)) {
                out.write(b);
            } else {
                out.write(ESCAPE_CHAR); // Escape character
                // Special escape
                if (b >= 0 && b < FREE_CHARS) {
                    out.write(CHAR_MAP[b]);
                } else { // Normal escape
                    char[] tmp = Integer.toHexString(b).toCharArray();
                    if (tmp.length == 1) {
                        out.write('0');
                        out.write(tmp[0]);
                    } else {
                        out.write(tmp[0]);
                        out.write(tmp[1]);
                    }
                }
            }
        }


        @Override
        public void write( char[] cbuf, int off, int len ) throws IOException {
            for (int i = 0; i < len; i++) {
                write(cbuf[off + i]);
            }
        }


        @Override
        public void write( String str, int off, int len ) throws IOException {
            write(str.toCharArray(), off, len);
        }
    }


    /**
     * Escape all occurences of newline chars '\n', quotes \", etc.
     */
    public static String convertString( String label ) {
        char[] ch = label.toCharArray();
        StringBuilder buf = new StringBuilder();
        for (char element : ch) {
            switch (element) {
                case '\n':
                    buf.append("\\n");
                    break;
                case '\r':
                    buf.append("\\r");
                    break;
                case '\"':
                    buf.append("\\\"");
                    break;
                case '\'':
                    buf.append("\\'");
                    break;
                case '\\':
                    buf.append("\\\\");
                    break;
                default:
                    buf.append(element);
                    break;
            }
        }
        return buf.toString();
    }

}
