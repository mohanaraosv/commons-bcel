/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.commons.bcel6.classfile;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.bcel6.Constants;

// The new table is used when generic types are about...

//LocalVariableTable_attribute {
//       u2 attribute_name_index;
//       u4 attribute_length;
//       u2 local_variable_table_length;
//       {  u2 start_pc;
//          u2 length;
//          u2 name_index;
//          u2 descriptor_index;
//          u2 index;
//       } local_variable_table[local_variable_table_length];
//     }

//LocalVariableTypeTable_attribute {
//    u2 attribute_name_index;
//    u4 attribute_length;
//    u2 local_variable_type_table_length;
//    { 
//      u2 start_pc;
//      u2 length;
//      u2 name_index;
//      u2 signature_index;
//      u2 index;
//    } local_variable_type_table[local_variable_type_table_length];
//  }
// J5TODO: Needs some testing !

/**
 * @since 6.0
 */
public class LocalVariableTypeTable extends Attribute {

    private LocalVariable[] local_variable_type_table;        // variables

    public LocalVariableTypeTable(LocalVariableTypeTable c) {
        this(c.getNameIndex(), c.getLength(), c.getLocalVariableTypeTable(), c.getConstantPool());
    }

    public LocalVariableTypeTable(int name_index, int length, LocalVariable[] local_variable_table, ConstantPool constant_pool) {
        super(Constants.ATTR_LOCAL_VARIABLE_TYPE_TABLE, name_index, length, constant_pool);
        this.local_variable_type_table = local_variable_table;
    }

    LocalVariableTypeTable(int nameIdx, int len, DataInput input, ConstantPool cpool) throws IOException {
        this(nameIdx, len, (LocalVariable[]) null, cpool);

        int local_variable_type_table_length = input.readUnsignedShort();
        local_variable_type_table = new LocalVariable[local_variable_type_table_length];

        for (int i = 0; i < local_variable_type_table_length; i++) {
            local_variable_type_table[i] = new LocalVariable(input, cpool);
        }
    }

    @Override
    public void accept(Visitor v) {
        v.visitLocalVariableTypeTable(this);
    }

    @Override
    public final void dump(DataOutputStream file) throws IOException {
        super.dump(file);
        file.writeShort(local_variable_type_table.length);
        for (LocalVariable variable : local_variable_type_table) {
            variable.dump(file);
        }
    }

    public final LocalVariable[] getLocalVariableTypeTable() {
        return local_variable_type_table;
    }

    public final LocalVariable getLocalVariable(int index) {
        for (LocalVariable variable : local_variable_type_table) {
            if (variable.getIndex() == index) {
                return variable;
            }
        }

        return null;
    }

    public final void setLocalVariableTable(LocalVariable[] local_variable_table) {
        this.local_variable_type_table = local_variable_table;
    }

    /**
     * @return String representation.
     */
    @Override
    public final String toString() {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < local_variable_type_table.length; i++) {
            buf.append(local_variable_type_table[i].toStringShared(true));

            if (i < local_variable_type_table.length - 1) {
                buf.append('\n');
            }
        }

        return buf.toString();
    }

    /**
     * @return deep copy of this attribute
     */
    @Override
    public Attribute copy(ConstantPool constant_pool) {
        LocalVariableTypeTable c = (LocalVariableTypeTable) clone();

        c.local_variable_type_table = new LocalVariable[local_variable_type_table.length];
        for (int i = 0; i < local_variable_type_table.length; i++) {
            c.local_variable_type_table[i] = local_variable_type_table[i].copy();
        }

        c.setConstantPool(constant_pool);
        return c;
    }

    public final int getTableLength() {
        return local_variable_type_table == null ? 0 : local_variable_type_table.length;
    }
}
