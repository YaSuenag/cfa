package jp.dip.ysfactory.cfa;

import java.nio.file.Path;
import java.io.InputStream;
import java.io.IOException;
import java.util.jar.JarFile;

import com.sun.tools.classfile.ConstantPoolException;


public class JarClassInfoDumper implements Dumper{

  private final String fname;

  public JarClassInfoDumper(Path path){
    fname = path.toString();
  }

  @Override
  public void dumpInfo(Option option){
    try(JarFile jar = new JarFile(fname)){
      jar.stream()
         .filter(e -> !e.isDirectory())
         .filter(e -> e.getName().endsWith(".class"))
         .forEach(e -> {
                         try(InputStream in = jar.getInputStream(e)){
                           ClassInfoDumper dumper = new ClassInfoDumper(
                                                                     in, fname);
                           dumper.dumpInfo(option);
                         }
                         catch(Exception ex){
                         }
                       });
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }

}

