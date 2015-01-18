package jp.dip.ysfactory.cfa;

import java.nio.file.Paths;
import java.util.Arrays;


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

