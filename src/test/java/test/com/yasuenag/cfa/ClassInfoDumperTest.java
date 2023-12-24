/*
 * Copyright (C) 2023, Yasumasa Suenaga
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package test.com.yasuenag.cfa;

import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.yasuenag.cfa.ClassInfoDumper;


@SuppressWarnings("missing-explicit-ctor")
public class ClassInfoDumperTest extends DumperTestBase{

  @Test
  public void testClassNameFromPath() throws Exception{
    var expectedClass = "FieldHolder";
    var info = new ClassInfoDumper(CLASSES_PATH.resolve(expectedClass + ".class"));

    var nameField = ClassInfoDumper.class.getDeclaredField("className");
    nameField.setAccessible(true);
    String actualClassName = (String)nameField.get(info);

    Assertions.assertEquals(expectedClass, actualClassName);
  }

  @Test
  public void testClassNameFromStream() throws Exception{
    var expectedClass = "FieldHolder";
    var fileName = expectedClass + ".class";
    try(var in = Files.newInputStream(CLASSES_PATH.resolve(fileName))){
      var info = new ClassInfoDumper(in, fileName);

      var nameField = ClassInfoDumper.class.getDeclaredField("className");
      nameField.setAccessible(true);
      String actualClassName = (String)nameField.get(info);

      Assertions.assertEquals(expectedClass, actualClassName);
    }
  }

  @Test
  public void testFName() throws Exception{
    var className = "FieldHolder";
    var expectedFileName = "test.jar";
    try(var in = Files.newInputStream(CLASSES_PATH.resolve(className + ".class"))){
      var info = new ClassInfoDumper(in, expectedFileName);

      var fnameField = ClassInfoDumper.class.getDeclaredField("fname");
      fnameField.setAccessible(true);
      String actualFname = (String)fnameField.get(info);

      Assertions.assertEquals(expectedFileName, actualFname);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSuperClass() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("SubClass.class"));

    var superClassField = ClassInfoDumper.class.getDeclaredField("superClass");
    superClassField.setAccessible(true);
    Optional<String> actualSuperClass = (Optional<String>)superClassField.get(info);
    Assertions.assertEquals("InterfaceImplementer", actualSuperClass.get());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInterface() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("InterfaceImplementer.class"));

    var interfaceSetField = ClassInfoDumper.class.getDeclaredField("interfaceSet");
    interfaceSetField.setAccessible(true);
    Set<String> actualSet = (Set<String>)interfaceSetField.get(info);

    Set<String> expectedSet = Set.of("java.io.Closeable");
    Assertions.assertIterableEquals(expectedSet, actualSet);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFieldRef() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("FieldAccessor.class"));

    var fieldSetField = ClassInfoDumper.class.getDeclaredField("fieldSet");
    fieldSetField.setAccessible(true);
    Set<FieldRefEntry> actualSet = (Set<FieldRefEntry>)fieldSetField.get(info);
    Assertions.assertEquals(actualSet.size(), 1);

    FieldRefEntry fieldInfo = actualSet.iterator().next();
    Assertions.assertEquals("Ljava/lang/String;", fieldInfo.type().stringValue());
    Assertions.assertEquals("FieldHolder", fieldInfo.owner().name().stringValue());
    Assertions.assertEquals("testField", fieldInfo.name().stringValue());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMethodRef() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("MethodCaller.class"));

    var methodSetField = ClassInfoDumper.class.getDeclaredField("methodSet");
    methodSetField.setAccessible(true);
    Set<MemberRefEntry> actualSet = (Set<MemberRefEntry>)methodSetField.get(info);
    Assertions.assertEquals(actualSet.size(), 3);

    for(var methodInfo : actualSet){
      Assertions.assertTrue((methodInfo.owner().name().equalsString("java/lang/Object") && methodInfo.name().equalsString("<init>") && methodInfo.type().equalsString("()V")) ||
                            (methodInfo.owner().name().equalsString("MethodHolder") && methodInfo.name().equalsString("<init>") && methodInfo.type().equalsString("()V")) ||
                            (methodInfo.owner().name().equalsString("MethodHolder") && methodInfo.name().equalsString("testMethod") && methodInfo.type().equalsString("()I")),
                            "Unexpected method: " + methodInfo.toString());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInterfaceMethodRef() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("InterfaceMethodCaller.class"));

    var methodSetField = ClassInfoDumper.class.getDeclaredField("methodSet");
    methodSetField.setAccessible(true);
    Set<MemberRefEntry> actualSet = (Set<MemberRefEntry>)methodSetField.get(info);
    Assertions.assertEquals(actualSet.size(), 2);

    for(var methodInfo : actualSet){
      Assertions.assertTrue((methodInfo.owner().name().equalsString("java/lang/Object") && methodInfo.name().equalsString("<init>") && methodInfo.type().equalsString("()V")) ||
                            (methodInfo.owner().name().equalsString("java/io/Closeable") && methodInfo.name().equalsString("close") && methodInfo.type().equalsString("()V")),
                            "Unexpected method: " + methodInfo.toString());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testClassSet() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("SubClass.class"));

    var classSetField = ClassInfoDumper.class.getDeclaredField("classSet");
    classSetField.setAccessible(true);
    Set<String> actualSet = (Set<String>)classSetField.get(info);

    Set<String> expectedSet = Set.of("SubClass", "InterfaceImplementer", "java.io.IOException");
    Assertions.assertEquals(expectedSet, actualSet);
  }

}
