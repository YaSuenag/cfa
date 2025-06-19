package com.yasuenag.cfa;

/*
 * Copyright (C) 2015, 2025, Yasumasa Suenaga
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import java.nio.file.Path;
import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.InterfaceMethodRefEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.MethodRefEntry;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Dump class information.
 */
public class ClassInfoDumper implements Dumper{

  /**
   * ClassModel of the class.
   */
  private ClassModel clazz;

  /**
   * File name which is included in this class.
   */
  private String fname;

  /**
   * Class name.
   */
  private String className;

  /**
   * Super class of this class.
   */
  private Optional<String> superClass;

  /**
   * Interface set of this class.
   */
  private Set<String> interfaceSet;

  /**
   * FieldRef set of this class.
   */
  private Set<FieldRefEntry> fieldSet;

  /**
   * MethodRef set of this class.
   */
  private Set<MemberRefEntry> methodSet;

  /**
   * Class collection of this class.
   */
  private Set<String> classSet;

  /**
   * Pattern for JNI class signature
   */
  private static final Pattern JNISIG_PATTERN = Pattern.compile("^L(.+);$");

  public static final Properties CLASS_VERSION_MAP;

  static{
    CLASS_VERSION_MAP = new Properties();
    try(var res = ClassInfoDumper.class.getResourceAsStream("/versions.properties")){
      CLASS_VERSION_MAP.load(res);
    }
    catch(IOException e){
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Constructor of ClassInfoDumper.
   *
   * @param path Path of class file.
   */
  public ClassInfoDumper(Path path) throws IOException{
    clazz = ClassFile.of().parse(path);
    fname = path.toString();
    initialize();
  }

  /**
   * Constructor of ClassInfoDumper.
   *
   * @param in InputStream of class.
   * @param fname File name or archive of class.
   */
  public ClassInfoDumper(InputStream in, String fname) throws IOException{
    clazz = ClassFile.of().parse(in.readAllBytes());
    this.fname = fname;
    initialize();
  }

  private String getClassNameInJava(ClassEntry c){
    return getClassNameInJava(c.asInternalName());
  }

  private String getClassNameInJava(String c){
    return c.replace('/', '.');
  }

  private Optional<String> getJavaClassFromJNISignature(String sig){
    var matcher = JNISIG_PATTERN.matcher(sig);
    return matcher.matches() ? Optional.of(getClassNameInJava(matcher.group(1)))
                             : Optional.empty();
  }

  /**
   * Initialize class information.
   */
  private void initialize(){
    className = getClassNameInJava(clazz.thisClass());
    superClass = clazz.superclass()
                      .map(this::getClassNameInJava);

    interfaceSet = clazz.interfaces()
                        .stream()
                        .map(this::getClassNameInJava)
                        .collect(Collectors.toSet());

    fieldSet = new HashSet<>();
    methodSet = new HashSet<>();
    clazz.constantPool()
         .iterator()
         .forEachRemaining(p -> {
            if(p instanceof FieldRefEntry f){
              fieldSet.add(f);
            }
            else if(p instanceof MethodRefEntry ||
                    p instanceof InterfaceMethodRefEntry){
              methodSet.add((MemberRefEntry)p);
            }
          });

    classSet = new HashSet<>();

    superClass.ifPresent(classSet::add);
    interfaceSet.forEach(classSet::add);
    fieldSet.forEach(f -> {
      getJavaClassFromJNISignature(f.type().stringValue()).ifPresent(classSet::add);
      classSet.add(getClassNameInJava(f.owner()));
    });
    methodSet.stream()
             .map(m -> getClassNameInJava(m.owner()))
             .forEach(classSet::add);
  }

  /**
   * Print class information as below:
   * <ul>
   *   <li>Class name</li>
   *   <li>File name</li>
   *   <li>Super class</li>
   *   <li>Interfaces</li>
   *   <li>Class version</li>
   * </ul>
   */
  public void printClassInfo(boolean isShort){
    System.out.println("Name: " + className);
    System.out.println("File: " + fname);

    if(isShort){
      return;
    }

    System.out.println("Super class: " + superClass.orElse("<None>"));

    System.out.println("Interfaces:");
    interfaceSet.forEach(e -> System.out.println("  " + e));

    String clsVerStr = (String)CLASS_VERSION_MAP.getOrDefault(Integer.toString(clazz.majorVersion()), "Unknown");
    if(clazz.minorVersion() != 0){
      clsVerStr += clazz.minorVersion() == 65535 ? " (Preview)" : " (Unknown minor version)";
    }
    System.out.println(String.format("Class version: %d.%d (Java release: %s)",
                                     clazz.majorVersion(), clazz.minorVersion(), clsVerStr));
  }

  /**
   * Print field information.
   */
  public void printFieldRefInfo(){
    System.out.println("Field References:");
    fieldSet.forEach(f -> System.out.printf("  %s %s.%s\n", f.type().stringValue(), getClassNameInJava(f.owner()), f.name().stringValue()));
  }

  /**
   * Print method information.
   */
  public void printMethodRefInfo(){
    System.out.println("Method References:");
    methodSet.forEach(m -> System.out.printf("  %s.%s%s\n", getClassNameInJava(m.owner()), m.name().stringValue(), m.type().stringValue()));
  }

  /**
   * Return whether this instance should be processed
   *
   * @param option instance of Option which contains filter conditions.
   * @return true if the class which is contained in this instance should be processed.
   */
  public boolean shouldProcess(Option option){
    return option.getTargetSet()
                 .map(s -> s.stream()
                            .anyMatch(className::contains))
                 .orElse(false) ||
           option.getClassFilterSet()
                 .map(s -> s.stream()
                            .anyMatch(t -> classSet.stream()
                                                   .anyMatch(c -> c.contains(t))))
                 .orElse(false) ||
           option.getMethodFilterSet()
                 .map(s -> s.stream()
                            .anyMatch(t -> methodSet.stream()
                                                    .map(m -> m.name().stringValue())
                                                    .anyMatch(m -> m.contains(t))))
                 .orElse(false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dumpInfo(Option option){
    if(!shouldProcess(option)){
      return;
    }

    printClassInfo(option.isShort());

    if(!option.isShort()){
      printFieldRefInfo();
      printMethodRefInfo();
    }

    System.out.println();
  }

}

