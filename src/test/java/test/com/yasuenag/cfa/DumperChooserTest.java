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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.yasuenag.cfa.ClassInfoDumper;
import com.yasuenag.cfa.DirectoryDumper;
import com.yasuenag.cfa.Dumper;
import com.yasuenag.cfa.DumperChooser;
import com.yasuenag.cfa.JarClassInfoDumper;


@SuppressWarnings("missing-explicit-ctor")
public class DumperChooserTest extends DumperTestBase{

  @Test
  public void testDirectory() throws Exception{
    Dumper actual = (new DumperChooser()).apply(CLASSES_PATH);
    Assertions.assertInstanceOf(DirectoryDumper.class, actual);
  }

  @Test
  public void testJar() throws Exception{
    Dumper actual = (new DumperChooser()).apply(TEST_JAR_PATH);
    Assertions.assertInstanceOf(JarClassInfoDumper.class, actual);
  }

  @Test
  public void testClassFile() throws Exception{
    Dumper actual = (new DumperChooser()).apply(CLASSES_PATH.resolve("FieldHolder.class"));
    Assertions.assertInstanceOf(ClassInfoDumper.class, actual);
  }

  @Test
  public void testInvalidFile() throws Exception{
    Dumper actual = (new DumperChooser()).apply(DUMMY_FILE_PATH);
    Assertions.assertNull(actual);
  }

}
