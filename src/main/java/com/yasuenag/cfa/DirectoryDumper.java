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

import java.util.Objects;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.stream.StreamSupport;
import java.io.IOException;
import java.io.UncheckedIOException;


/**
 * ClassInfoDumper for Directory.
 */
public class DirectoryDumper implements Dumper{

  /**
   * Target directory.
   */
  private final Path dir;

  /**
   * Constructor of DirectoryDumper.
   *
   * @param path Path to target directory.
   */
  public DirectoryDumper(Path path){
    dir = path;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dumpInfo(Option option){
    DumperChooser chooser = new DumperChooser();

    try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)){
      StreamSupport.stream(stream.spliterator(), false)
                   .map(chooser)
                   .filter(Objects::nonNull)
                   .forEach(d -> d.dumpInfo(option));
    }
    catch(IOException e){
      throw new UncheckedIOException(e);
    }

  }

}

