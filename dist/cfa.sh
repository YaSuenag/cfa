#!/bin/sh

# Copyright (C) 2017, Yasumasa Suenaga
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.

if [ -z "$JAVA_HOME" ]; then
  echo '$JAVA_HOME is required.'
  exit 1
fi

JAVA_VERSION=`$JAVA_HOME/bin/java -version 2>&1 | head -n 1 | sed -e 's/^.\+"\([0-9]\+\)\.\?.*$/\1/'`

BASEDIR=`dirname $0`
MAIN_CLASS=jp.dip.ysfactory.cfa.Main

if [ $JAVA_VERSION -eq 1 ]; then
  TOOLS_JAR=$JAVA_HOME/lib/tools.jar

  if [ ! -e $TOOLS_JAR ]; then
    echo "$TOOLS_JAR does not exist."
    exit 2
  fi

  $JAVA_HOME/bin/java -cp $BASEDIR/cfa.jar:$TOOLS_JAR $MAIN_CLASS $@
else
  $JAVA_HOME/bin/java -p $BASEDIR/cfa.jar \
                      --add-modules cfa \
                      --add-exports jdk.jdeps/com.sun.tools.classfile=cfa \
                      $MAIN_CLASS \
                      $@
fi
