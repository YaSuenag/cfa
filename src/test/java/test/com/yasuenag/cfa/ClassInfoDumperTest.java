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

import java.nio.file.Files;
import java.util.List;
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

  @Test
  public void testSuperClass() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("SubClass.class"));

    var superClassField = ClassInfoDumper.class.getDeclaredField("superClass");
    superClassField.setAccessible(true);
    String actualSuperClass = (String)superClassField.get(info);
    Assertions.assertEquals("InterfaceImplementer", actualSuperClass);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInterface() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("InterfaceImplementer.class"));

    var interfaceListField = ClassInfoDumper.class.getDeclaredField("interfaceList");
    interfaceListField.setAccessible(true);
    List<String> actualList = (List<String>)interfaceListField.get(info);

    List<String> expectedList = List.of("java.io.Closeable");
    Assertions.assertIterableEquals(expectedList, actualList);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFieldRef() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("FieldAccessor.class"));

    var fieldListField = ClassInfoDumper.class.getDeclaredField("fieldList");
    fieldListField.setAccessible(true);
    List<ClassInfoDumper.FieldInfo> actualList = (List<ClassInfoDumper.FieldInfo>)fieldListField.get(info);
    Assertions.assertEquals(actualList.size(), 1);

    ClassInfoDumper.FieldInfo fieldInfo = actualList.get(0);
    Assertions.assertEquals("Ljava.lang.String;", fieldInfo.getType());
    Assertions.assertEquals("FieldHolder", fieldInfo.getClassName());
    Assertions.assertEquals("testField", fieldInfo.getName());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMethodRef() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("MethodCaller.class"));

    var methodListField = ClassInfoDumper.class.getDeclaredField("methodList");
    methodListField.setAccessible(true);
    List<ClassInfoDumper.MethodInfo> actualList = (List<ClassInfoDumper.MethodInfo>)methodListField.get(info);
    Assertions.assertEquals(actualList.size(), 3);

    for(ClassInfoDumper.MethodInfo methodInfo : actualList){
      Assertions.assertTrue((methodInfo.getClassName().equals("java.lang.Object") && methodInfo.getName().equals("<init>") && methodInfo.getSignature().equals("()V")) ||
                            (methodInfo.getClassName().equals("MethodHolder") && methodInfo.getName().equals("<init>") && methodInfo.getSignature().equals("()V")) ||
                            (methodInfo.getClassName().equals("MethodHolder") && methodInfo.getName().equals("testMethod") && methodInfo.getSignature().equals("()I")),
                            "Unexpected method: " + methodInfo.toString());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInterfaceMethodRef() throws Exception{
    var info = new ClassInfoDumper(CLASSES_PATH.resolve("InterfaceMethodCaller.class"));

    var methodListField = ClassInfoDumper.class.getDeclaredField("methodList");
    methodListField.setAccessible(true);
    List<ClassInfoDumper.MethodInfo> actualList = (List<ClassInfoDumper.MethodInfo>)methodListField.get(info);
    Assertions.assertEquals(actualList.size(), 2);

    for(ClassInfoDumper.MethodInfo methodInfo : actualList){
      Assertions.assertTrue((methodInfo.getClassName().equals("java.lang.Object") && methodInfo.getName().equals("<init>") && methodInfo.getSignature().equals("()V")) ||
                            (methodInfo.getClassName().equals("java.io.Closeable") && methodInfo.getName().equals("close") && methodInfo.getSignature().equals("()V")),
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
