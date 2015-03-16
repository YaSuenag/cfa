/*
 * Bootstrap.java
 *
 * Copyright (C) 2015 Yasumasa Suenaga
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
 *
 */
package jp.dip.ysfactory.cfa;


import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;


/**
 * Bootstrap class for HSLoader.
 * This class loaded JAR in ./lib , and invoke HSLoader#main().
 * 
 * @author Yasumasa Suenaga
 */
public class Bootstrap {

  /**
   * ClassLoader for CFA.
   * WARNING: This classloader will load class from this classloader at first.
   */
  private static class CFAClassLoader extends URLClassLoader{

    public CFAClassLoader(URL[] urls, ClassLoader parent){
      super(urls, parent);

      String classpath = System.getProperty("java.class.path");
      String separator = System.getProperty("path.separator");

      Arrays.stream(classpath.split(separator))
            .map(CFAClassLoader::stringToURL)
            .forEach(this::addURL);
    }

    private static URL stringToURL(String path){

      try{
        return Paths.get(path).toUri().toURL();
      }
      catch(MalformedURLException e){
        throw new RuntimeException(e);
      }

    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException{

      try{
        return findClass(name);
      }
      catch(ClassNotFoundException e){
        return super.loadClass(name);
      }

    }

  }

  /**
   * Create ClassLoader for CFA.
   * This method loaded tools.jar JAR in JDK.
   * 
   * @return ClassLoader for CFA.
   */
  private static ClassLoader createAppClassLoader()
                          throws FileNotFoundException, MalformedURLException{
    ClassLoader currentClassLoader = Bootstrap.class.getClassLoader();
    File tools_jar = Paths.get(System.getProperty("java.home"),
                                             "..", "lib", "tools.jar").toFile();

    if(!tools_jar.exists()){
      throw new FileNotFoundException("tools.jar does not exist.");
    }

    URL[] urls = {tools_jar.toURI().toURL()};
    return new CFAClassLoader(urls, currentClassLoader);
  }

  public static void main(String[] args) throws FileNotFoundException,
                       MalformedURLException, ClassNotFoundException,
                          NoSuchMethodException, IllegalAccessException,
                            IllegalArgumentException, InvocationTargetException{
    System.out.println(System.getProperty("java.class.path"));
    ClassLoader appClassLoader = createAppClassLoader();
    Class<?> mainClass = appClassLoader.loadClass("jp.dip.ysfactory.cfa.Main");
    Method mainMethod = mainClass.getMethod("main", String[].class);
    mainMethod.invoke(null, (Object)args);
  }

}
