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


public class Main{

  public static void main(String[] args) throws Exception{
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

    DumperChooser chooser = new DumperChooser();
    option.getFileList()
          .stream()
          .map(chooser)
          .filter(d -> d !=  null)
          .forEach(d -> ((Dumper)d).dumpInfo(option));
  }

}

