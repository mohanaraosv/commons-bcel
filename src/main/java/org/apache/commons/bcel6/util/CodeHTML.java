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
package org.apache.commons.bcel6.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;

import org.apache.commons.bcel6.Constants;
import org.apache.commons.bcel6.classfile.Attribute;
import org.apache.commons.bcel6.classfile.Code;
import org.apache.commons.bcel6.classfile.CodeException;
import org.apache.commons.bcel6.classfile.ConstantFieldref;
import org.apache.commons.bcel6.classfile.ConstantInterfaceMethodref;
import org.apache.commons.bcel6.classfile.ConstantInvokeDynamic;
import org.apache.commons.bcel6.classfile.ConstantMethodref;
import org.apache.commons.bcel6.classfile.ConstantNameAndType;
import org.apache.commons.bcel6.classfile.ConstantPool;
import org.apache.commons.bcel6.classfile.LocalVariable;
import org.apache.commons.bcel6.classfile.LocalVariableTable;
import org.apache.commons.bcel6.classfile.Method;
import org.apache.commons.bcel6.classfile.Utility;

/**
 * Convert code into HTML file.
 *
 * @version $Id$
 * 
 */
final class CodeHTML {

    private final String class_name; // name of current class
//    private Method[] methods; // Methods to print
    private final PrintWriter file; // file to write to
    private BitSet goto_set;
    private final ConstantPool constant_pool;
    private final ConstantHTML constant_html;
    private static boolean wide = false;


    CodeHTML(String dir, String class_name, Method[] methods, ConstantPool constant_pool,
            ConstantHTML constant_html) throws IOException {
        this.class_name = class_name;
//        this.methods = methods;
        this.constant_pool = constant_pool;
        this.constant_html = constant_html;
        file = new PrintWriter(new FileOutputStream(dir + class_name + "_code.html"));
        file.println("<HTML><BODY BGCOLOR=\"#C0C0C0\">");
        for (int i = 0; i < methods.length; i++) {
            writeMethod(methods[i], i);
        }
        file.println("</BODY></HTML>");
        file.close();
    }


    /**
     * Disassemble a stream of byte codes and return the
     * string representation.
     *
     * @param  stream data input stream
     * @return String representation of byte code
     */
    private String codeToHTML( ByteSequence bytes, int method_number ) throws IOException {
        short opcode = (short) bytes.readUnsignedByte();
        String name;
        String signature;
        int default_offset = 0;
        int low;
        int high;
        int index;
        int class_index;
        int vindex;
        int constant;
        int[] jump_table;
        int no_pad_bytes = 0;
        int offset;
        StringBuilder buf = new StringBuilder(256); // CHECKSTYLE IGNORE MagicNumber
        buf.append("<TT>").append(Constants.getOpcodeName(opcode)).append("</TT></TD><TD>");
        /* Special case: Skip (0-3) padding bytes, i.e., the
         * following bytes are 4-byte-aligned
         */
        if ((opcode == Constants.TABLESWITCH) || (opcode == Constants.LOOKUPSWITCH)) {
            int remainder = bytes.getIndex() % 4;
            no_pad_bytes = (remainder == 0) ? 0 : 4 - remainder;
            for (int i = 0; i < no_pad_bytes; i++) {
                bytes.readByte();
            }
            // Both cases have a field default_offset in common
            default_offset = bytes.readInt();
        }
        switch (opcode) {
            case Constants.TABLESWITCH:
                low = bytes.readInt();
                high = bytes.readInt();
                offset = bytes.getIndex() - 12 - no_pad_bytes - 1;
                default_offset += offset;
                buf.append("<TABLE BORDER=1><TR>");
                // Print switch indices in first row (and default)
                jump_table = new int[high - low + 1];
                for (int i = 0; i < jump_table.length; i++) {
                    jump_table[i] = offset + bytes.readInt();
                    buf.append("<TH>").append(low + i).append("</TH>");
                }
                buf.append("<TH>default</TH></TR>\n<TR>");
                // Print target and default indices in second row
            for (int element : jump_table) {
                buf.append("<TD><A HREF=\"#code").append(method_number).append("@").append(
                        element).append("\">").append(element).append("</A></TD>");
            }
                buf.append("<TD><A HREF=\"#code").append(method_number).append("@").append(
                        default_offset).append("\">").append(default_offset).append(
                        "</A></TD></TR>\n</TABLE>\n");
                break;
            /* Lookup switch has variable length arguments.
             */
            case Constants.LOOKUPSWITCH:
                int npairs = bytes.readInt();
                offset = bytes.getIndex() - 8 - no_pad_bytes - 1;
                jump_table = new int[npairs];
                default_offset += offset;
                buf.append("<TABLE BORDER=1><TR>");
                // Print switch indices in first row (and default)
                for (int i = 0; i < npairs; i++) {
                    int match = bytes.readInt();
                    jump_table[i] = offset + bytes.readInt();
                    buf.append("<TH>").append(match).append("</TH>");
                }
                buf.append("<TH>default</TH></TR>\n<TR>");
                // Print target and default indices in second row
                for (int i = 0; i < npairs; i++) {
                    buf.append("<TD><A HREF=\"#code").append(method_number).append("@").append(
                            jump_table[i]).append("\">").append(jump_table[i]).append("</A></TD>");
                }
                buf.append("<TD><A HREF=\"#code").append(method_number).append("@").append(
                        default_offset).append("\">").append(default_offset).append(
                        "</A></TD></TR>\n</TABLE>\n");
                break;
            /* Two address bytes + offset from start of byte stream form the
             * jump target.
             */
            case Constants.GOTO:
            case Constants.IFEQ:
            case Constants.IFGE:
            case Constants.IFGT:
            case Constants.IFLE:
            case Constants.IFLT:
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
            case Constants.JSR:
                index = bytes.getIndex() + bytes.readShort() - 1;
                buf.append("<A HREF=\"#code").append(method_number).append("@").append(index)
                        .append("\">").append(index).append("</A>");
                break;
            /* Same for 32-bit wide jumps
             */
            case Constants.GOTO_W:
            case Constants.JSR_W:
                int windex = bytes.getIndex() + bytes.readInt() - 1;
                buf.append("<A HREF=\"#code").append(method_number).append("@").append(windex)
                        .append("\">").append(windex).append("</A>");
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
                    vindex = bytes.readShort();
                    wide = false; // Clear flag
                } else {
                    vindex = bytes.readUnsignedByte();
                }
                buf.append("%").append(vindex);
                break;
            /*
             * Remember wide byte which is used to form a 16-bit address in the
             * following instruction. Relies on that the method is called again with
             * the following opcode.
             */
            case Constants.WIDE:
                wide = true;
                buf.append("(wide)");
                break;
            /* Array of basic type.
             */
            case Constants.NEWARRAY:
                buf.append("<FONT COLOR=\"#00FF00\">").append(Constants.getTypeName(bytes.readByte())).append(
                        "</FONT>");
                break;
            /* Access object/class fields.
             */
            case Constants.GETFIELD:
            case Constants.GETSTATIC:
            case Constants.PUTFIELD:
            case Constants.PUTSTATIC:
                index = bytes.readShort();
                ConstantFieldref c1 = (ConstantFieldref) constant_pool.getConstant(index,
                        Constants.CONSTANT_Fieldref);
                class_index = c1.getClassIndex();
                name = constant_pool.getConstantString(class_index, Constants.CONSTANT_Class);
                name = Utility.compactClassName(name, false);
                index = c1.getNameAndTypeIndex();
                String field_name = constant_pool.constantToString(index, Constants.CONSTANT_NameAndType);
                if (name.equals(class_name)) { // Local field
                    buf.append("<A HREF=\"").append(class_name).append("_methods.html#field")
                            .append(field_name).append("\" TARGET=Methods>").append(field_name)
                            .append("</A>\n");
                } else {
                    buf.append(constant_html.referenceConstant(class_index)).append(".").append(
                            field_name);
                }
                break;
            /* Operands are references to classes in constant pool
             */
            case Constants.CHECKCAST:
            case Constants.INSTANCEOF:
            case Constants.NEW:
                index = bytes.readShort();
                buf.append(constant_html.referenceConstant(index));
                break;
            /* Operands are references to methods in constant pool
             */
            case Constants.INVOKESPECIAL:
            case Constants.INVOKESTATIC:
            case Constants.INVOKEVIRTUAL:
            case Constants.INVOKEINTERFACE:
            case Constants.INVOKEDYNAMIC:
                int m_index = bytes.readShort();
                String str;
                if (opcode == Constants.INVOKEINTERFACE) { // Special treatment needed
                    bytes.readUnsignedByte(); // Redundant
                    bytes.readUnsignedByte(); // Reserved
//                    int nargs = bytes.readUnsignedByte(); // Redundant
//                    int reserved = bytes.readUnsignedByte(); // Reserved
                    ConstantInterfaceMethodref c = (ConstantInterfaceMethodref) constant_pool
                            .getConstant(m_index, Constants.CONSTANT_InterfaceMethodref);
                    class_index = c.getClassIndex();
                    index = c.getNameAndTypeIndex();
                    name = Class2HTML.referenceClass(class_index);
                } else if (opcode == Constants.INVOKEDYNAMIC) { // Special treatment needed
                    bytes.readUnsignedByte(); // Reserved
                    bytes.readUnsignedByte(); // Reserved
                    ConstantInvokeDynamic c = (ConstantInvokeDynamic) constant_pool
                            .getConstant(m_index, Constants.CONSTANT_InvokeDynamic);
                    index = c.getNameAndTypeIndex();
                    name = "#" + c.getBootstrapMethodAttrIndex();
                } else {
                    // UNDONE: Java8 now allows INVOKESPECIAL and INVOKESTATIC to
                    // reference EITHER a Methodref OR an InterfaceMethodref.
                    // Not sure if that affects this code or not.  (markro)
                    ConstantMethodref c = (ConstantMethodref) constant_pool.getConstant(m_index,
                            Constants.CONSTANT_Methodref);
                    class_index = c.getClassIndex();
                    index = c.getNameAndTypeIndex();
                name = Class2HTML.referenceClass(class_index);
                }
                str = Class2HTML.toHTML(constant_pool.constantToString(constant_pool.getConstant(
                        index, Constants.CONSTANT_NameAndType)));
                // Get signature, i.e., types
                ConstantNameAndType c2 = (ConstantNameAndType) constant_pool.getConstant(index,
                        Constants.CONSTANT_NameAndType);
                signature = constant_pool.constantToString(c2.getSignatureIndex(), Constants.CONSTANT_Utf8);
                String[] args = Utility.methodSignatureArgumentTypes(signature, false);
                String type = Utility.methodSignatureReturnType(signature, false);
                buf.append(name).append(".<A HREF=\"").append(class_name).append("_cp.html#cp")
                        .append(m_index).append("\" TARGET=ConstantPool>").append(str).append(
                                "</A>").append("(");
                // List arguments
                for (int i = 0; i < args.length; i++) {
                    buf.append(Class2HTML.referenceType(args[i]));
                    if (i < args.length - 1) {
                        buf.append(", ");
                    }
                }
                // Attach return type
                buf.append("):").append(Class2HTML.referenceType(type));
                break;
            /* Operands are references to items in constant pool
             */
            case Constants.LDC_W:
            case Constants.LDC2_W:
                index = bytes.readShort();
                buf.append("<A HREF=\"").append(class_name).append("_cp.html#cp").append(index)
                        .append("\" TARGET=\"ConstantPool\">").append(
                                Class2HTML.toHTML(constant_pool.constantToString(index,
                                        constant_pool.getConstant(index).getTag()))).append("</a>");
                break;
            case Constants.LDC:
                index = bytes.readUnsignedByte();
                buf.append("<A HREF=\"").append(class_name).append("_cp.html#cp").append(index)
                        .append("\" TARGET=\"ConstantPool\">").append(
                                Class2HTML.toHTML(constant_pool.constantToString(index,
                                        constant_pool.getConstant(index).getTag()))).append("</a>");
                break;
            /* Array of references.
             */
            case Constants.ANEWARRAY:
                index = bytes.readShort();
                buf.append(constant_html.referenceConstant(index));
                break;
            /* Multidimensional array of references.
             */
            case Constants.MULTIANEWARRAY:
                index = bytes.readShort();
                int dimensions = bytes.readByte();
                buf.append(constant_html.referenceConstant(index)).append(":").append(dimensions)
                        .append("-dimensional");
                break;
            /* Increment local variable.
             */
            case Constants.IINC:
                if (wide) {
                    vindex = bytes.readShort();
                    constant = bytes.readShort();
                    wide = false;
                } else {
                    vindex = bytes.readUnsignedByte();
                    constant = bytes.readByte();
                }
                buf.append("%").append(vindex).append(" ").append(constant);
                break;
            default:
                if (Constants.getNoOfOperands(opcode) > 0) {
                    for (int i = 0; i < Constants.getOperandTypeCount(opcode); i++) {
                        switch (Constants.getOperandType(opcode, i)) {
                            case Constants.T_BYTE:
                                buf.append(bytes.readUnsignedByte());
                                break;
                            case Constants.T_SHORT: // Either branch or index
                                buf.append(bytes.readShort());
                                break;
                            case Constants.T_INT:
                                buf.append(bytes.readInt());
                                break;
                            default: // Never reached
                                throw new IllegalStateException(
                                    "Unreachable default case reached! "+Constants.getOperandType(opcode, i));
                        }
                        buf.append("&nbsp;");
                    }
                }
        }
        buf.append("</TD>");
        return buf.toString();
    }


    /**
     * Find all target addresses in code, so that they can be marked
     * with &lt;A NAME = ...&gt;. Target addresses are kept in an BitSet object.
     */
    private void findGotos( ByteSequence bytes, Code code ) throws IOException {
        int index;
        goto_set = new BitSet(bytes.available());
        int opcode;
        /* First get Code attribute from method and the exceptions handled
         * (try .. catch) in this method. We only need the line number here.
         */
        if (code != null) {
            CodeException[] ce = code.getExceptionTable();
            for (CodeException cex : ce) {
                goto_set.set(cex.getStartPC());
                goto_set.set(cex.getEndPC());
                goto_set.set(cex.getHandlerPC());
            }
            // Look for local variables and their range
            Attribute[] attributes = code.getAttributes();
            for (Attribute attribute : attributes) {
                if (attribute.getTag() == Constants.ATTR_LOCAL_VARIABLE_TABLE) {
                    LocalVariable[] vars = ((LocalVariableTable) attribute)
                            .getLocalVariableTable();
                    for (LocalVariable var : vars) {
                        int start = var.getStartPC();
                        int end = start + var.getLength();
                        goto_set.set(start);
                        goto_set.set(end);
                    }
                    break;
                }
            }
        }
        // Get target addresses from GOTO, JSR, TABLESWITCH, etc.
        for (; bytes.available() > 0;) {
            opcode = bytes.readUnsignedByte();
            //System.out.println(getOpcodeName(opcode));
            switch (opcode) {
                case Constants.TABLESWITCH:
                case Constants.LOOKUPSWITCH:
                    //bytes.readByte(); // Skip already read byte
                    int remainder = bytes.getIndex() % 4;
                    int no_pad_bytes = (remainder == 0) ? 0 : 4 - remainder;
                    int default_offset;
                    int offset;
                    for (int j = 0; j < no_pad_bytes; j++) {
                        bytes.readByte();
                    }
                    // Both cases have a field default_offset in common
                    default_offset = bytes.readInt();
                    if (opcode == Constants.TABLESWITCH) {
                        int low = bytes.readInt();
                        int high = bytes.readInt();
                        offset = bytes.getIndex() - 12 - no_pad_bytes - 1;
                        default_offset += offset;
                        goto_set.set(default_offset);
                        for (int j = 0; j < (high - low + 1); j++) {
                            index = offset + bytes.readInt();
                            goto_set.set(index);
                        }
                    } else { // LOOKUPSWITCH
                        int npairs = bytes.readInt();
                        offset = bytes.getIndex() - 8 - no_pad_bytes - 1;
                        default_offset += offset;
                        goto_set.set(default_offset);
                        for (int j = 0; j < npairs; j++) {
//                            int match = bytes.readInt();
                            bytes.readInt();
                            index = offset + bytes.readInt();
                            goto_set.set(index);
                        }
                    }
                    break;
                case Constants.GOTO:
                case Constants.IFEQ:
                case Constants.IFGE:
                case Constants.IFGT:
                case Constants.IFLE:
                case Constants.IFLT:
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
                case Constants.JSR:
                    //bytes.readByte(); // Skip already read byte
                    index = bytes.getIndex() + bytes.readShort() - 1;
                    goto_set.set(index);
                    break;
                case Constants.GOTO_W:
                case Constants.JSR_W:
                    //bytes.readByte(); // Skip already read byte
                    index = bytes.getIndex() + bytes.readInt() - 1;
                    goto_set.set(index);
                    break;
                default:
                    bytes.unreadByte();
                    codeToHTML(bytes, 0); // Ignore output
            }
        }
    }


    /**
     * Write a single method with the byte code associated with it.
     */
    private void writeMethod( Method method, int method_number ) throws IOException {
        // Get raw signature
        String signature = method.getSignature();
        // Get array of strings containing the argument types
        String[] args = Utility.methodSignatureArgumentTypes(signature, false);
        // Get return type string
        String type = Utility.methodSignatureReturnType(signature, false);
        // Get method name
        String name = method.getName();
        String html_name = Class2HTML.toHTML(name);
        // Get method's access flags
        String access = Utility.accessToString(method.getAccessFlags());
        access = Utility.replace(access, " ", "&nbsp;");
        // Get the method's attributes, the Code Attribute in particular
        Attribute[] attributes = method.getAttributes();
        file.print("<P><B><FONT COLOR=\"#FF0000\">" + access + "</FONT>&nbsp;" + "<A NAME=method"
                + method_number + ">" + Class2HTML.referenceType(type) + "</A>&nbsp<A HREF=\""
                + class_name + "_methods.html#method" + method_number + "\" TARGET=Methods>"
                + html_name + "</A>(");
        for (int i = 0; i < args.length; i++) {
            file.print(Class2HTML.referenceType(args[i]));
            if (i < args.length - 1) {
                file.print(",&nbsp;");
            }
        }
        file.println(")</B></P>");
        Code c = null;
        byte[] code = null;
        if (attributes.length > 0) {
            file.print("<H4>Attributes</H4><UL>\n");
            for (int i = 0; i < attributes.length; i++) {
                byte tag = attributes[i].getTag();
                if (tag != Constants.ATTR_UNKNOWN) {
                    file.print("<LI><A HREF=\"" + class_name + "_attributes.html#method"
                            + method_number + "@" + i + "\" TARGET=Attributes>"
                            + Constants.getAttributeName(tag) + "</A></LI>\n");
                } else {
                    file.print("<LI>" + attributes[i] + "</LI>");
                }
                if (tag == Constants.ATTR_CODE) {
                    c = (Code) attributes[i];
                    Attribute[] attributes2 = c.getAttributes();
                    code = c.getCode();
                    file.print("<UL>");
                    for (int j = 0; j < attributes2.length; j++) {
                        tag = attributes2[j].getTag();
                        file.print("<LI><A HREF=\"" + class_name + "_attributes.html#" + "method"
                                + method_number + "@" + i + "@" + j + "\" TARGET=Attributes>"
                                + Constants.getAttributeName(tag) + "</A></LI>\n");
                    }
                    file.print("</UL>");
                }
            }
            file.println("</UL>");
        }
        if (code != null) { // No code, an abstract method, e.g.
            //System.out.println(name + "\n" + Utility.codeToString(code, constant_pool, 0, -1));
            // Print the byte code
            ByteSequence stream = new ByteSequence(code);
            stream.mark(stream.available());
            findGotos(stream, c);
            stream.reset();
            file.println("<TABLE BORDER=0><TR><TH ALIGN=LEFT>Byte<BR>offset</TH>"
                    + "<TH ALIGN=LEFT>Instruction</TH><TH ALIGN=LEFT>Argument</TH>");
            for (; stream.available() > 0;) {
                int offset = stream.getIndex();
                String str = codeToHTML(stream, method_number);
                String anchor = "";
                /* Set an anchor mark if this line is targetted by a goto, jsr, etc.
                 * Defining an anchor for every line is very inefficient!
                 */
                if (goto_set.get(offset)) {
                    anchor = "<A NAME=code" + method_number + "@" + offset + "></A>";
                }
                String anchor2;
                if (stream.getIndex() == code.length) {
                    anchor2 = "<A NAME=code" + method_number + "@" + code.length + ">" + offset
                            + "</A>";
                } else {
                    anchor2 = "" + offset;
                }
                file
                        .println("<TR VALIGN=TOP><TD>" + anchor2 + "</TD><TD>" + anchor + str
                                + "</TR>");
            }
            // Mark last line, may be targetted from Attributes window
            file.println("<TR><TD> </A></TD></TR>");
            file.println("</TABLE>");
        }
    }
}
