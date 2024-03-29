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
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * ClassInfoDumper for JAR.
 */
public class JarClassInfoDumper implements Dumper{

  /**
   * Archive file name.
   */
  private final String fname;

  /**
   * Constructor of JarClassInfoDumper.
   *
   * @param path Path to JAR.
   */
  public JarClassInfoDumper(Path path){
    fname = path.toString();
  }

  private void dumpFromStream(JarFile jar, JarEntry entry, Option option){
    try(InputStream in = jar.getInputStream(entry)){
      ClassInfoDumper dumper = new ClassInfoDumper(in, fname);
      dumper.dumpInfo(option);
    }
    catch(Exception ex){
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dumpInfo(Option option){
    try(JarFile jar = new JarFile(fname)){
      jar.stream()
         .filter(Predicate.not(JarEntry::isDirectory))
         .filter(e -> e.getName().endsWith(".class"))
         .forEach(e -> dumpFromStream(jar, e, option));
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }

}

