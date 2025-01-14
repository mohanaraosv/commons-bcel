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
import org.apache.commons.bcel6.classfile.AnnotationEntry;
import org.apache.commons.bcel6.classfile.Annotations;
import org.apache.commons.bcel6.classfile.Attribute;
import org.apache.commons.bcel6.classfile.Constant;
import org.apache.commons.bcel6.classfile.ConstantObject;
import org.apache.commons.bcel6.classfile.ConstantPool;
import org.apache.commons.bcel6.classfile.ConstantValue;
import org.apache.commons.bcel6.classfile.Field;
import org.apache.commons.bcel6.classfile.Utility;
import org.apache.commons.bcel6.util.BCELComparator;

/** 
 * Template class for building up a field.  The only extraordinary thing
 * one can do is to add a constant value attribute to a field (which must of
 * course be compatible with to the declared type).
 *
 * @version $Id$
 * @see Field
 */
public class FieldGen extends FieldGenOrMethodGen {

    private Object value = null;
    private static BCELComparator _cmp = new BCELComparator() {

        @Override
        public boolean equals( Object o1, Object o2 ) {
            FieldGen THIS = (FieldGen) o1;
            FieldGen THAT = (FieldGen) o2;
            return THIS.getName().equals(THAT.getName())
                    && THIS.getSignature().equals(THAT.getSignature());
        }


        @Override
        public int hashCode( Object o ) {
            FieldGen THIS = (FieldGen) o;
            return THIS.getSignature().hashCode() ^ THIS.getName().hashCode();
        }
    };


    /**
     * Declare a field. If it is static (isStatic() == true) and has a
     * basic type like int or String it may have an initial value
     * associated with it as defined by setInitValue().
     *
     * @param access_flags access qualifiers
     * @param type  field type
     * @param name field name
     * @param cp constant pool
     */
    public FieldGen(int access_flags, Type type, String name, ConstantPoolGen cp) {
        super(access_flags);
        setType(type);
        setName(name);
        setConstantPool(cp);
    }


    /**
     * Instantiate from existing field.
     *
     * @param field Field object
     * @param cp constant pool (must contain the same entries as the field's constant pool)
     */
    public FieldGen(Field field, ConstantPoolGen cp) {
        this(field.getAccessFlags(), Type.getType(field.getSignature()), field.getName(), cp);
        Attribute[] attrs = field.getAttributes();
        for (Attribute attr : attrs) {
            if (attr instanceof ConstantValue) {
                setValue(((ConstantValue) attr).getConstantValueIndex());
            } else if (attr instanceof Annotations) {
                Annotations runtimeAnnotations = (Annotations)attr;
                AnnotationEntry[] annotationEntries = runtimeAnnotations.getAnnotationEntries();
                for (AnnotationEntry element : annotationEntries) {
                    addAnnotationEntry(new AnnotationEntryGen(element,cp,false));
                }
            } else {
                addAttribute(attr);
            }
        }
    }


    private void setValue( int index ) {
        ConstantPool cp = super.getConstantPool().getConstantPool();
        Constant c = cp.getConstant(index);
        value = ((ConstantObject) c).getConstantValue(cp);
    }


    /**
     * Set (optional) initial value of field, otherwise it will be set to null/0/false
     * by the JVM automatically.
     */
    public void setInitValue( String str ) {
        checkType(  ObjectType.getInstance("java.lang.String"));
        if (str != null) {
            value = str;
        }
    }


    public void setInitValue( long l ) {
        checkType(Type.LONG);
        if (l != 0L) {
            value = Long.valueOf(l);
        }
    }


    public void setInitValue( int i ) {
        checkType(Type.INT);
        if (i != 0) {
            value = Integer.valueOf(i);
        }
    }


    public void setInitValue( short s ) {
        checkType(Type.SHORT);
        if (s != 0) {
            value = Integer.valueOf(s);
        }
    }


    public void setInitValue( char c ) {
        checkType(Type.CHAR);
        if (c != 0) {
            value = Integer.valueOf(c);
        }
    }


    public void setInitValue( byte b ) {
        checkType(Type.BYTE);
        if (b != 0) {
            value = Integer.valueOf(b);
        }
    }


    public void setInitValue( boolean b ) {
        checkType(Type.BOOLEAN);
        if (b) {
            value = Integer.valueOf(1);
        }
    }


    public void setInitValue( float f ) {
        checkType(Type.FLOAT);
        if (f != 0.0) {
            value = new Float(f);
        }
    }


    public void setInitValue( double d ) {
        checkType(Type.DOUBLE);
        if (d != 0.0) {
            value = new Double(d);
        }
    }


    /** Remove any initial value.
     */
    public void cancelInitValue() {
        value = null;
    }


    private void checkType( Type atype ) {
        final Type superType = super.getType();
        if (superType == null) {
            throw new ClassGenException("You haven't defined the type of the field yet");
        }
        if (!isFinal()) {
            throw new ClassGenException("Only final fields may have an initial value!");
        }
        if (!superType.equals(atype)) {
            throw new ClassGenException("Types are not compatible: " + superType + " vs. " + atype);
        }
    }


    /**
     * Get field object after having set up all necessary values.
     */
    public Field getField() {
        String signature = getSignature();
        int name_index = super.getConstantPool().addUtf8(super.getName());
        int signature_index = super.getConstantPool().addUtf8(signature);
        if (value != null) {
            checkType(super.getType());
            int index = addConstant();
            addAttribute(new ConstantValue(super.getConstantPool().addUtf8("ConstantValue"), 2, index, 
                    super.getConstantPool().getConstantPool())); // sic
        }
        addAnnotationsAsAttribute(super.getConstantPool());
        return new Field(super.getAccessFlags(), name_index, signature_index, getAttributes(), 
                super.getConstantPool().getConstantPool()); // sic
    }

    private void addAnnotationsAsAttribute(ConstantPoolGen cp) {
          Attribute[] attrs = AnnotationEntryGen.getAnnotationAttributes(cp, super.getAnnotationEntries());
        for (Attribute attr : attrs) {
            addAttribute(attr);
        }
      }


    private int addConstant() {
        switch (super.getType().getType()) { // sic
            case Constants.T_INT:
            case Constants.T_CHAR:
            case Constants.T_BYTE:
            case Constants.T_BOOLEAN:
            case Constants.T_SHORT:
                return super.getConstantPool().addInteger(((Integer) value).intValue());
            case Constants.T_FLOAT:
                return super.getConstantPool().addFloat(((Float) value).floatValue());
            case Constants.T_DOUBLE:
                return super.getConstantPool().addDouble(((Double) value).doubleValue());
            case Constants.T_LONG:
                return super.getConstantPool().addLong(((Long) value).longValue());
            case Constants.T_REFERENCE:
                return super.getConstantPool().addString((String) value);
            default:
                throw new RuntimeException("Oops: Unhandled : " + super.getType().getType()); // sic
        }
    }


    @Override
    public String getSignature() {
        return super.getType().getSignature();
    }

    private List<FieldObserver> observers;


    /** Add observer for this object.
     */
    public void addObserver( FieldObserver o ) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        observers.add(o);
    }


    /** Remove observer for this object.
     */
    public void removeObserver( FieldObserver o ) {
        if (observers != null) {
            observers.remove(o);
        }
    }


    /** Call notify() method on all observers. This method is not called
     * automatically whenever the state has changed, but has to be
     * called by the user after he has finished editing the object.
     */
    public void update() {
        if (observers != null) {
            for (FieldObserver observer : observers ) {
                observer.notify(this);
            }
        }
    }


    public String getInitValue() {
        if (value != null) {
            return value.toString();
        }
        return null;
    }


    /**
     * Return string representation close to declaration format,
     * `public static final short MAX = 100', e.g..
     *
     * @return String representation of field
     */
    @Override
    public final String toString() {
        String name;
        String signature;
        String access; // Short cuts to constant pool
        access = Utility.accessToString(super.getAccessFlags());
        access = access.equals("") ? "" : (access + " ");
        signature = super.getType().toString();
        name = getName();
        StringBuilder buf = new StringBuilder(32); // CHECKSTYLE IGNORE MagicNumber
        buf.append(access).append(signature).append(" ").append(name);
        String value = getInitValue();
        if (value != null) {
            buf.append(" = ").append(value);
        }
        return buf.toString();
    }


    /** @return deep copy of this field
     */
    public FieldGen copy( ConstantPoolGen cp ) {
        FieldGen fg = (FieldGen) clone();
        fg.setConstantPool(cp);
        return fg;
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
     * By default two FieldGen objects are said to be equal when
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
     * By default return the hashcode of the field's name XOR signature.
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return _cmp.hashCode(this);
    }
}
