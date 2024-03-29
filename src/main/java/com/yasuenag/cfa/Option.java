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


import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;


/**
 * Argument parser.
 */
public class Option{

  /**
   * Target class set.
   */
  private Set<String> targetSet;

  /**
   * Class filter set.
   */
  private Set<String> classFilterSet;

  /**
   * Method filter set.
   */
  private Set<String> methodFilterSet;

  /**
   * File set to analyze.
   */
  private Set<Path> fileSet;

  /**
   * Whether short output?
   */
  private boolean shortOutput;

  /**
   * Print usage.
   */
  public static void printOptions(){
    var prop = new Properties();
    try(var res = Option.class.getResourceAsStream("/cfa.properties")){
      prop.load(res);
    }
    catch(IOException e){
      throw new UncheckedIOException(e);
    }

    System.out.format("Class File Analyzer (CFA) %s\n", prop.getProperty("version"));
    System.out.format("Copyright (C) 2015, %s, Yasumasa Suenaga\n", prop.getProperty("buildYear"));
    System.out.println();
    System.out.println("""
    Usage:
      cfa [options] [file or directory...]

    Options:
      -h: This help.
      -t class1,class2,...: Target class.
                            CFA will pick up classes from file list.
      -c class1,class2,...: Class filter.
                            CFA will pick up classes which include them in ConstantPool.
      -m method1,method2,...: Method filter.
                              CFA will pick up classes which include them in ConstantPool.
      -s: Short output.
          If this option is added, CFA will output class name and file path only.
    """);
  }

  /**
   * Constructor of Option.
   *
   * @param args Commandline arguments.
   */
  @SuppressWarnings("fallthrough")
  public Option(String[] args) throws IllegalArgumentException{
    targetSet = null;
    classFilterSet = null;
    methodFilterSet = null;
    shortOutput = false;
    fileSet = new HashSet<>();

    Iterator<String> itr = Arrays.asList(args).iterator();
    while(itr.hasNext()){
      String str = itr.next();

      switch(str){

        case "-h":
          Option.printOptions();
          System.exit(1);

        case "-t":

          if(!itr.hasNext()){
            throw new IllegalArgumentException("Invalid target list.");
          }

          targetSet = new HashSet<>(Arrays.asList(itr.next().split(",")));
          break;

        case "-c":

          if(!itr.hasNext()){
            throw new IllegalArgumentException("Invalid class filter list.");
          }

          classFilterSet = new HashSet<>(Arrays.asList(itr.next().split(",")));
          break;

        case "-m":

          if(!itr.hasNext()){
            throw new IllegalArgumentException("Invalid method filter list.");
          }

          methodFilterSet = new HashSet<>(Arrays.asList(itr.next().split(",")));
          break;

        case "-s":
          shortOutput = true;
          break;

        default:
          Path path = Paths.get(str);
          File file = path.toFile();

          if(!file.exists()){
            throw new IllegalArgumentException(
                                       "Invalid file: " + path.toString());
          }

          fileSet.add(path);

      }

    }

  }

  public Optional<Set<String>> getTargetSet(){
    return Optional.ofNullable(targetSet);
  }

  public Optional<Set<String>> getClassFilterSet(){
    return Optional.ofNullable(classFilterSet);
  }

  public Optional<Set<String>> getMethodFilterSet(){
    return Optional.ofNullable(methodFilterSet);
  }

  public Set<Path> getFileSet(){
    return fileSet;
  }

  public boolean isShort(){
    return shortOutput;
  }

}

