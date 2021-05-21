package com.yasuenag.cfa;

/*
 * Copyright (C) 2015, 2021, Yasumasa Suenaga
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

import java.util.function.Function;
import java.nio.file.Path;
import java.io.IOException;
import java.io.UncheckedIOException;
import com.sun.tools.classfile.ConstantPoolException;


public class DumperChooser implements Function<Path, Dumper>{

  @Override
  public Dumper apply(Path path){
    try{
      if(path.toFile().isDirectory()){
        return new DirectoryDumper(path);
      }
      else if(path.toString().endsWith(".jar")){
        return new JarClassInfoDumper(path);
      }
      else if(path.toString().endsWith(".class")){
        return new ClassInfoDumper(path);
      }
      else{
        return null;
      }
    }
    catch(IOException e){
      throw new UncheckedIOException(e);
    }
    catch(ConstantPoolException e){
      throw new RuntimeException(e);
    }
  }

}

