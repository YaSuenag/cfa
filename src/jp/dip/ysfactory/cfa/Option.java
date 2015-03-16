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


import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Argument parser.
 */
public class Option{

  /**
   * Target class list.
   */
  private List<String> targetList;

  /**
   * Class filter list.
   */
  private List<String> classFilterList;

  /**
   * Method filter list.
   */
  private List<String> methodFilterList;

  /**
   * File list to analyze.
   */
  private List<Path> fileList;

  /**
   * Whether short output?
   */
  private boolean shortOutput;

  /**
   * Print usage.
   */
  public static void printOptions(){
    System.out.println("Class File Analyzer (CFA)  0.1.1");
    System.out.println("Copyright (C) 2015 Yasumasa Suenaga");
    System.out.println();
    System.out.println("Usage:");
    System.out.println("  java -jar cfa.jar [options] file1 file2 ...");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  -h: This help.");
    System.out.println("  -t class1,class2,...: Target class.");
    System.out.println("                        CFA will pick up classes from file list.");
    System.out.println("  -c class1,class2,...: Class filter.");
    System.out.println("                        CFA will pick up classes which include them in ConstantPool.");
    System.out.println("  -m method1,method2,...: Method filter.");
    System.out.println("                          CFA will pick up classes which include them in ConstantPool.");
    System.out.println("  -s: Short output.");
    System.out.println("      If this option is added, CFA will output class name and file path only.");
  }

  /**
   * Constructor of Option.
   *
   * @param args Commandline arguments.
   */
  public Option(String[] args) throws IllegalArgumentException{
    targetList = null;
    classFilterList = null;
    methodFilterList = null;
    shortOutput = false;
    fileList = new ArrayList<>();

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

          targetList = Arrays.asList(itr.next().split(","));
          break;

        case "-c":

          if(!itr.hasNext()){
            throw new IllegalArgumentException("Invalid class filter list.");
          }

          classFilterList = Arrays.asList(itr.next().split(","));
          break;

        case "-m":

          if(!itr.hasNext()){
            throw new IllegalArgumentException("Invalid method filter list.");
          }

          methodFilterList = Arrays.asList(itr.next().split(","));
          break;

        case "-s":
          shortOutput = true;
          break;

        default:
          Path path = Paths.get(str);
          File file = path.toFile();

          if(!file.exists() || file.isDirectory()){
            throw new IllegalArgumentException(
                                       "Invalid file: " + path.toString());
          }

          fileList.add(path);

      }

    }

  }

  public List<String> getTargetList(){
    return targetList;
  }

  public List<String> getClassFilterList(){
    return classFilterList;
  }

  public List<String> getMethodFilterList(){
    return methodFilterList;
  }

  public List<Path> getFileList(){
    return fileList;
  }

  public boolean isShort(){
    return shortOutput;
  }

}

