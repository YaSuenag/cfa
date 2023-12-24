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

import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import org.junit.jupiter.api.BeforeAll;


@SuppressWarnings("missing-explicit-ctor")
public class DumperTestBase{

  public static final Path GENCODE_PATH = Path.of("generated-cfa-test-files");

  public static final Path CLASSES_PATH = GENCODE_PATH.resolve("classes");

  public static final Path TEST_JAR_PATH = GENCODE_PATH.resolve("test.jar");

  public static final Path DUMMY_FILE_PATH = GENCODE_PATH.resolve("dummy.txt");

  private static class JavaSourceFromString extends SimpleJavaFileObject{
    private final String code;

    public JavaSourceFromString(String name, String code) {
        super(URI.create("string:///" + name + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
  }

/********** test classes **********/
  private static final String FIELD_HOLDER = """
    public class FieldHolder{
      public String testField;
    }
    """;

  private static final String METHOD_HOLDER = """
    public class MethodHolder{
      public int testMethod(){
        return 0;
      };
    }
    """;

  private static final String FIELD_ACCESSOR = """
    public class FieldAccessor{
      public void testAccess(){
        var inst = new FieldHolder();
        inst.testField = "this is write operation";
      };
    }
    """;

  private static final String METHOD_CALLER = """
    public class MethodCaller{
      public void testCall(){
        var inst = new MethodHolder();
        inst.testMethod();
      };
    }
    """;

  private static final String INTERFACE_CALLER = """
    public class InterfaceMethodCaller{
      public void testCall(java.io.Closeable clo) throws Exception{
        clo.close();
      };
    }
    """;

  private static final String INTERFACE_IMPLEMENTER = """
    public class InterfaceImplementer implements java.io.Closeable{
      @Override
      public void close() throws java.io.IOException{
      };
    }
    """;

  private static final String SUBCLASS = """
    public class SubClass extends InterfaceImplementer{
      public void closeWithoutThrowing(){
        try{
          close();
        }
        catch(java.io.IOException e){
          e.printStackTrace();
        }
      }
    }
    """;
/**********************************/

  @BeforeAll
  public static void init() throws Exception{
    try{
      var classDir = Files.createDirectories(CLASSES_PATH);

      var compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
      var srcs = List.of(new JavaSourceFromString("FieldHolder", FIELD_HOLDER),
                         new JavaSourceFromString("MethodHolder", METHOD_HOLDER),
                         new JavaSourceFromString("FieldAccessor", FIELD_ACCESSOR),
                         new JavaSourceFromString("MethodCaller", METHOD_CALLER),
                         new JavaSourceFromString("InterfaceMethodCaller", INTERFACE_CALLER),
                         new JavaSourceFromString("InterfaceImplementer", INTERFACE_IMPLEMENTER),
                         new JavaSourceFromString("SubClass", SUBCLASS));
      var task = compiler.getTask(null, null, null, List.of("-d", classDir.toString()), null, srcs);
      if(!task.call()){
        throw new RuntimeException("Compilation failed");
      }

      var jar = java.util.spi.ToolProvider.findFirst("jar").get();
      jar.run(System.out, System.err, "-cf", TEST_JAR_PATH.toString(), "-C", classDir.toString(), ".");

      Files.createFile(DUMMY_FILE_PATH);
    }
    catch(FileAlreadyExistsException e){
    }
  }

}
