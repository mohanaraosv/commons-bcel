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
import org.apache.commons.bcel6.classfile.AnnotationEntry;
import org.apache.commons.bcel6.classfile.Annotations;
import org.apache.commons.bcel6.classfile.Attribute;
import org.apache.commons.bcel6.classfile.ConstantPool;
import org.apache.commons.bcel6.classfile.Field;
import org.apache.commons.bcel6.classfile.JavaClass;
import org.apache.commons.bcel6.classfile.Method;
import org.apache.commons.bcel6.classfile.RuntimeInvisibleAnnotations;
import org.apache.commons.bcel6.classfile.RuntimeVisibleAnnotations;
import org.apache.commons.bcel6.classfile.SourceFile;
import org.apache.commons.bcel6.util.BCELComparator;

/** 
 * Template class for building up a java class. May be initialized with an
 * existing java class (file).
 *
 * @see JavaClass
 * @version $Id$
 */
public class ClassGen extends AccessFlags implements Cloneable {

    /* Corresponds to the fields found in a JavaClass object.
     */
    private String class_name;
    private String super_class_name;
    private final String file_name;
    private int class_name_index = -1;
    private int superclass_name_index = -1;
    private int major = Constants.MAJOR_1_1;
    private int minor = Constants.MINOR_1_1;
    private ConstantPoolGen cp; // Template for building up constant pool
    // ArrayLists instead of arrays to gather fields, methods, etc.
    private final List<Field> field_vec = new ArrayList<>();
    private final List<Method> method_vec = new ArrayList<>();
    private final List<Attribute> attribute_vec = new ArrayList<>();
    private final List<String> interface_vec = new ArrayList<>();
    private final List<AnnotationEntryGen> annotation_vec = new ArrayList<>();

    private static BCELComparator _cmp = new BCELComparator() {

        @Override
        public boolean equals( Object o1, Object o2 ) {
            ClassGen THIS = (ClassGen) o1;
            ClassGen THAT = (ClassGen) o2;
            return THIS.getClassName().equals(THAT.getClassName());
        }


        @Override
        public int hashCode( Object o ) {
            ClassGen THIS = (ClassGen) o;
            return THIS.getClassName().hashCode();
        }
    };


    /** Convenience constructor to set up some important values initially.
     *
     * @param class_name fully qualified class name
     * @param super_class_name fully qualified superclass name
     * @param file_name source file name
     * @param access_flags access qualifiers
     * @param interfaces implemented interfaces
     * @param cp constant pool to use
     */
    public ClassGen(String class_name, String super_class_name, String file_name, int access_flags,
            String[] interfaces, ConstantPoolGen cp) {
        super(access_flags);
        this.class_name = class_name;
        this.super_class_name = super_class_name;
        this.file_name = file_name;
        this.cp = cp;
        // Put everything needed by default into the constant pool and the vectors
        if (file_name != null) {
            addAttribute(new SourceFile(cp.addUtf8("SourceFile"), 2, cp.addUtf8(file_name), cp
                    .getConstantPool()));
        }
        class_name_index = cp.addClass(class_name);
        superclass_name_index = cp.addClass(super_class_name);
        if (interfaces != null) {
            for (String interface1 : interfaces) {
                addInterface(interface1);
            }
        }
    }


    /** Convenience constructor to set up some important values initially.
     *
     * @param class_name fully qualified class name
     * @param super_class_name fully qualified superclass name
     * @param file_name source file name
     * @param access_flags access qualifiers
     * @param interfaces implemented interfaces
     */
    public ClassGen(String class_name, String super_class_name, String file_name, int access_flags,
            String[] interfaces) {
        this(class_name, super_class_name, file_name, access_flags, interfaces,
                new ConstantPoolGen());
    }


    /**
     * Initialize with existing class.
     * @param clazz JavaClass object (e.g. read from file)
     */
    public ClassGen(JavaClass clazz) {
        super(clazz.getAccessFlags());
        class_name_index = clazz.getClassNameIndex();
        superclass_name_index = clazz.getSuperclassNameIndex();
        class_name = clazz.getClassName();
        super_class_name = clazz.getSuperclassName();
        file_name = clazz.getSourceFileName();
        cp = new ConstantPoolGen(clazz.getConstantPool());
        major = clazz.getMajor();
        minor = clazz.getMinor();
        Attribute[] attributes = clazz.getAttributes();
        // J5TODO: Could make unpacking lazy, done on first reference
        AnnotationEntryGen[] annotations = unpackAnnotations(attributes);
        Method[] methods = clazz.getMethods();
        Field[] fields = clazz.getFields();
        String[] interfaces = clazz.getInterfaceNames();
        for (String interface1 : interfaces) {
            addInterface(interface1);
        }
        for (Attribute attribute : attributes) {
            if (!(attribute instanceof Annotations)) {
                addAttribute(attribute);
            }
        }
        for (AnnotationEntryGen annotation : annotations) {
            addAnnotationEntry(annotation);
        }
        for (Method method : methods) {
            addMethod(method);
        }
        for (Field field : fields) {
            addField(field);
        }
    }

    /**
     * Look for attributes representing annotations and unpack them.
     */
    private AnnotationEntryGen[] unpackAnnotations(Attribute[] attrs)
    {
        List<AnnotationEntryGen> annotationGenObjs = new ArrayList<>();
        for (Attribute attr : attrs) {
            if (attr instanceof RuntimeVisibleAnnotations)
            {
                RuntimeVisibleAnnotations rva = (RuntimeVisibleAnnotations) attr;
                AnnotationEntry[] annos = rva.getAnnotationEntries();
                for (AnnotationEntry a : annos) {
                    annotationGenObjs.add(new AnnotationEntryGen(a,
                            getConstantPool(), false));
                }
            }
            else
                if (attr instanceof RuntimeInvisibleAnnotations)
                {
                    RuntimeInvisibleAnnotations ria = (RuntimeInvisibleAnnotations) attr;
                    AnnotationEntry[] annos = ria.getAnnotationEntries();
                    for (AnnotationEntry a : annos) {
                        annotationGenObjs.add(new AnnotationEntryGen(a,
                                getConstantPool(), false));
                    }
                }
        }
        return annotationGenObjs.toArray(new AnnotationEntryGen[annotationGenObjs.size()]);
    }


    /**
     * @return the (finally) built up Java class object.
     */
    public JavaClass getJavaClass() {
        int[] interfaces = getInterfaces();
        Field[] fields = getFields();
        Method[] methods = getMethods();
        Attribute[] attributes = null;
        if (annotation_vec.isEmpty()) {
            attributes = getAttributes();
        } else {
            // TODO: Sometime later, trash any attributes called 'RuntimeVisibleAnnotations' or 'RuntimeInvisibleAnnotations'
            Attribute[] annAttributes  = AnnotationEntryGen.getAnnotationAttributes(cp, getAnnotationEntries());
            attributes = new Attribute[attribute_vec.size()+annAttributes.length];
            attribute_vec.toArray(attributes);
            System.arraycopy(annAttributes,0,attributes,attribute_vec.size(),annAttributes.length);       
        }
        // Must be last since the above calls may still add something to it
        ConstantPool _cp = this.cp.getFinalConstantPool();
        return new JavaClass(class_name_index, superclass_name_index, file_name, major, minor,
                super.getAccessFlags(), _cp, interfaces, fields, methods, attributes);
    }


    /**
     * Add an interface to this class, i.e., this class has to implement it.
     * @param name interface to implement (fully qualified class name)
     */
    public void addInterface( String name ) {
        interface_vec.add(name);
    }


    /**
     * Remove an interface from this class.
     * @param name interface to remove (fully qualified name)
     */
    public void removeInterface( String name ) {
        interface_vec.remove(name);
    }


    /**
     * @return major version number of class file
     */
    public int getMajor() {
        return major;
    }


    /** Set major version number of class file, default value is 45 (JDK 1.1)
     * @param major major version number
     */
    public void setMajor( int major ) { // TODO could be package-protected - only called by test code
        this.major = major;
    }


    /** Set minor version number of class file, default value is 3 (JDK 1.1)
     * @param minor minor version number
     */
    public void setMinor( int minor ) {  // TODO could be package-protected - only called by test code
        this.minor = minor;
    }

    /**
     * @return minor version number of class file
     */
    public int getMinor() {
        return minor;
    }


    /**
     * Add an attribute to this class.
     * @param a attribute to add
     */
    public void addAttribute( Attribute a ) {
        attribute_vec.add(a);
    }

    public void addAnnotationEntry(AnnotationEntryGen a) { 
        annotation_vec.add(a); 
    }


    /**
     * Add a method to this class.
     * @param m method to add
     */
    public void addMethod( Method m ) {
        method_vec.add(m);
    }


    /**
     * Convenience method.
     *
     * Add an empty constructor to this class that does nothing but calling super().
     * @param access_flags rights for constructor
     */
    public void addEmptyConstructor( int access_flags ) {
        InstructionList il = new InstructionList();
        il.append(InstructionConstants.THIS); // Push `this'
        il.append(new INVOKESPECIAL(cp.addMethodref(super_class_name, "<init>", "()V")));
        il.append(InstructionConstants.RETURN);
        MethodGen mg = new MethodGen(access_flags, Type.VOID, Type.NO_ARGS, null, "<init>",
                class_name, il, cp);
        mg.setMaxStack(1);
        addMethod(mg.getMethod());
    }


    /**
     * Add a field to this class.
     * @param f field to add
     */
    public void addField( Field f ) {
        field_vec.add(f);
    }


    public boolean containsField( Field f ) {
        return field_vec.contains(f);
    }


    /** @return field object with given name, or null
     */
    public Field containsField( String name ) {
        for (Field f : field_vec) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }


    /** @return method object with given name and signature, or null
     */
    public Method containsMethod( String name, String signature ) {
        for (Method m : method_vec) {
            if (m.getName().equals(name) && m.getSignature().equals(signature)) {
                return m;
            }
        }
        return null;
    }


    /**
     * Remove an attribute from this class.
     * @param a attribute to remove
     */
    public void removeAttribute( Attribute a ) {
        attribute_vec.remove(a);
    }


    /**
     * Remove a method from this class.
     * @param m method to remove
     */
    public void removeMethod( Method m ) {
        method_vec.remove(m);
    }


    /** Replace given method with new one. If the old one does not exist
     * add the new_ method to the class anyway.
     */
    public void replaceMethod( Method old, Method new_ ) {
        if (new_ == null) {
            throw new ClassGenException("Replacement method must not be null");
        }
        int i = method_vec.indexOf(old);
        if (i < 0) {
            method_vec.add(new_);
        } else {
            method_vec.set(i, new_);
        }
    }


    /** Replace given field with new one. If the old one does not exist
     * add the new_ field to the class anyway.
     */
    public void replaceField( Field old, Field new_ ) {
        if (new_ == null) {
            throw new ClassGenException("Replacement method must not be null");
        }
        int i = field_vec.indexOf(old);
        if (i < 0) {
            field_vec.add(new_);
        } else {
            field_vec.set(i, new_);
        }
    }


    /**
     * Remove a field to this class.
     * @param f field to remove
     */
    public void removeField( Field f ) {
        field_vec.remove(f);
    }


    public String getClassName() {
        return class_name;
    }


    public String getSuperclassName() {
        return super_class_name;
    }


    public String getFileName() {
        return file_name;
    }


    public void setClassName( String name ) {
        class_name = name.replace('/', '.');
        class_name_index = cp.addClass(name);
    }


    public void setSuperclassName( String name ) {
        super_class_name = name.replace('/', '.');
        superclass_name_index = cp.addClass(name);
    }


    public Method[] getMethods() {
        return method_vec.toArray(new Method[method_vec.size()]);
    }


    public void setMethods( Method[] methods ) {
        method_vec.clear();
        for (Method method : methods) {
            addMethod(method);
        }
    }


    public void setMethodAt( Method method, int pos ) {
        method_vec.set(pos, method);
    }


    public Method getMethodAt( int pos ) {
        return method_vec.get(pos);
    }


    public String[] getInterfaceNames() {
        int size = interface_vec.size();
        String[] interfaces = new String[size];
        interface_vec.toArray(interfaces);
        return interfaces;
    }


    public int[] getInterfaces() {
        int size = interface_vec.size();
        int[] interfaces = new int[size];
        for (int i = 0; i < size; i++) {
            interfaces[i] = cp.addClass(interface_vec.get(i));
        }
        return interfaces;
    }


    public Field[] getFields() {
        return field_vec.toArray(new Field[field_vec.size()]);
    }


    public Attribute[] getAttributes() {
        return attribute_vec.toArray(new Attribute[attribute_vec.size()]);
    }

    //  J5TODO: Should we make calling unpackAnnotations() lazy and put it in here?
    public AnnotationEntryGen[] getAnnotationEntries() {
        return annotation_vec.toArray(new AnnotationEntryGen[annotation_vec.size()]);
    }


    public ConstantPoolGen getConstantPool() {
        return cp;
    }


    public void setConstantPool( ConstantPoolGen constant_pool ) {
        cp = constant_pool;
    }


    public void setClassNameIndex( int class_name_index ) {
        this.class_name_index = class_name_index;
        class_name = cp.getConstantPool().getConstantString(class_name_index,
                Constants.CONSTANT_Class).replace('/', '.');
    }


    public void setSuperclassNameIndex( int superclass_name_index ) {
        this.superclass_name_index = superclass_name_index;
        super_class_name = cp.getConstantPool().getConstantString(superclass_name_index,
                Constants.CONSTANT_Class).replace('/', '.');
    }


    public int getSuperclassNameIndex() {
        return superclass_name_index;
    }


    public int getClassNameIndex() {
        return class_name_index;
    }

    private List<ClassObserver> observers;


    /** Add observer for this object.
     */
    public void addObserver( ClassObserver o ) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        observers.add(o);
    }


    /** Remove observer for this object.
     */
    public void removeObserver( ClassObserver o ) {
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
            for (ClassObserver observer : observers) {
                observer.notify(this);
            }
        }
    }


    @Override
    public ClassGen clone() {
        try {
            return (ClassGen) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Clone Not Supported"); // never happens
        }
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
     * By default two ClassGen objects are said to be equal when
     * their class names are equal.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        return _cmp.equals(this, obj);
    }


    /**
     * Return value as defined by given BCELComparator strategy.
     * By default return the hashcode of the class name.
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return _cmp.hashCode(this);
    }
}
