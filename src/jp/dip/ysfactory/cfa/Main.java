package jp.dip.ysfactory.cfa;

/*
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
 */

import java.io.File;
import java.nio.file.Paths;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.FileNotFoundException;
import java.lang.NoSuchMethodException;
import java.net.MalformedURLException;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;


public class Main{

  private static void setupClassLoader() throws FileNotFoundException,
                              NoSuchMethodException, MalformedURLException,
                              IllegalAccessException, InvocationTargetException{
    File tools_jar = Paths.get(System.getProperty("java.home"), "..",
                                                  "lib", "tools.jar").toFile();
    if(!tools_jar.exists()){
      throw new FileNotFoundException("tools.jar does not exist.");
    }

    Method addURLMethod = URLClassLoader.class.getDeclaredMethod(
                                                         "addURL", URL.class);
    addURLMethod.setAccessible(true);
    addURLMethod.invoke(Main.class.getClassLoader(), tools_jar.toURI().toURL());
  }

  public static void main(String[] args) throws Exception{
    setupClassLoader();

    Option option;
    try{
      option = new Option(args);
    }
    catch(IllegalArgumentException e){
      System.err.println(e.getMessage());
      Option.printOptions();
      System.exit(1);
      return;
    }

    option.getFileList()
          .stream()
          .map(p -> {
                      try{
                        return p.toString().endsWith(".jar")
                                                 ? new JarClassInfoDumper(p)
                                                 : new ClassInfoDumper(p);
                      }
                      catch(Exception e){
                        throw new RuntimeException(e);
                      }
                    })
          .forEach(d -> ((Dumper)d).dumpInfo(option));
  }

}

