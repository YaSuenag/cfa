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

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.yasuenag.cfa.Option;


@SuppressWarnings("missing-explicit-ctor")
public class OptionTest extends DumperTestBase{

  @Test
  public void testTargetFilter(){
    var opt = new Option(new String[]{"-t", "Foo,Bar"});
    Assertions.assertEquals(Set.of("Foo", "Bar"), opt.getTargetSet().get());
    Assertions.assertFalse(opt.getClassFilterSet().isPresent());
    Assertions.assertFalse(opt.getMethodFilterSet().isPresent());
    Assertions.assertFalse(opt.isShort());
  }

  @Test
  public void testClassFilter(){
    var opt = new Option(new String[]{"-c", "Foo,Bar"});
    Assertions.assertEquals(Set.of("Foo", "Bar"), opt.getClassFilterSet().get());
    Assertions.assertFalse(opt.getTargetSet().isPresent());
    Assertions.assertFalse(opt.getMethodFilterSet().isPresent());
    Assertions.assertFalse(opt.isShort());
  }

  @Test
  public void testMethodFilter(){
    var opt = new Option(new String[]{"-m", "foo,bar"});
    Assertions.assertEquals(Set.of("foo", "bar"), opt.getMethodFilterSet().get());
    Assertions.assertFalse(opt.getTargetSet().isPresent());
    Assertions.assertFalse(opt.getClassFilterSet().isPresent());
    Assertions.assertFalse(opt.isShort());
  }

  @Test
  public void testIsShort(){
    var opt = new Option(new String[]{"-s"});
    Assertions.assertTrue(opt.isShort());
    Assertions.assertFalse(opt.getTargetSet().isPresent());
    Assertions.assertFalse(opt.getClassFilterSet().isPresent());
    Assertions.assertFalse(opt.getMethodFilterSet().isPresent());
  }

  @Test
  public void testTargetFiles(){
    var opt = new Option(new String[]{CLASSES_PATH.toString(), TEST_JAR_PATH.toString()});
    Assertions.assertEquals(Set.of(CLASSES_PATH, TEST_JAR_PATH), opt.getFileSet());
    Assertions.assertFalse(opt.getTargetSet().isPresent());
    Assertions.assertFalse(opt.getClassFilterSet().isPresent());
    Assertions.assertFalse(opt.getMethodFilterSet().isPresent());
    Assertions.assertFalse(opt.isShort());

    /* Test for invalid file */
    Assertions.assertThrows(IllegalArgumentException.class, () -> new Option(new String[]{"silver-bullet"}));
  }

  @Test
  public void testAllArguments(){
    var opt = new Option(new String[]{"-t", "Foo,Bar",
                                      "-c", "Baz,Qux",
                                      "-m", "foo,bar",
                                      "-s",
                                      CLASSES_PATH.toString(), TEST_JAR_PATH.toString()});
    Assertions.assertEquals(Set.of("Foo", "Bar"), opt.getTargetSet().get());
    Assertions.assertEquals(Set.of("Baz", "Qux"), opt.getClassFilterSet().get());
    Assertions.assertEquals(Set.of("foo", "bar"), opt.getMethodFilterSet().get());
    Assertions.assertTrue(opt.isShort());
    Assertions.assertEquals(Set.of(CLASSES_PATH, TEST_JAR_PATH), opt.getFileSet());
  }

}
