package com.yasuenag.cfa;

/*
 * Copyright (C) 2015, 2023, Yasumasa Suenaga
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
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.InterfaceMethodRefEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.MethodRefEntry;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
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

  private static final Map<Integer, String> CLASS_VERSION_MAP = Map.ofEntries(
                                                 Map.entry(46, "Java 1.2"),
                                                 Map.entry(47, "Java 1.3"),
                                                 Map.entry(48, "Java 1.4"),
                                                 Map.entry(49, "Java 1.5"),
                                                 Map.entry(50, "Java 6"),
                                                 Map.entry(51, "Java 7"),
                                                 Map.entry(52, "Java 8"),
                                                 Map.entry(53, "Java 9"),
                                                 Map.entry(54, "Java 10"),
                                                 Map.entry(55, "Java 11"),
                                                 Map.entry(56, "Java 12"),
                                                 Map.entry(57, "Java 13"),
                                                 Map.entry(58, "Java 14"),
                                                 Map.entry(59, "Java 15"),
                                                 Map.entry(60, "Java 16"),
                                                 Map.entry(61, "Java 17"),
                                                 Map.entry(62, "Java 18"),
                                                 Map.entry(63, "Java 19"),
                                                 Map.entry(64, "Java 20"),
                                                 Map.entry(65, "Java 21"),
                                                 Map.entry(66, "Java 22")
                                            );

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

    String clsVerStr = CLASS_VERSION_MAP.getOrDefault(clazz.majorVersion(), "Unknown");
    if(clazz.minorVersion() != 0){
      clsVerStr += clazz.minorVersion() == 65535 ? " (Preview)" : " (Unknown minor version)";
    }
    System.out.println(String.format("Class version: %d.%d (%s)",
                                     clazz.majorVersion(), clazz.minorVersion(), clsVerStr));
  }

  /**
   * Print field information.
   */
  public void printFieldRefInfo(){
    System.out.println("Field References:");
    fieldSet.forEach(f -> System.out.println(STR."  \{f.type().stringValue()} \{getClassNameInJava(f.owner())}.\{f.name().stringValue()}"));
  }

  /**
   * Print method information.
   */
  public void printMethodRefInfo(){
    System.out.println("Method References:");
    methodSet.forEach(m -> System.out.println(STR."  \{getClassNameInJava(m.owner())}.\{m.name().stringValue()}\{m.type().stringValue()}"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dumpInfo(Option option){
    boolean shouldShow = option.getTargetSet()
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

    if(!shouldShow){
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

