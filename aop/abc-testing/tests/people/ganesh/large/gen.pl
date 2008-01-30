#! /usr/bin/perl -w

print 
"
public class Test {
 public static void main(String args[])
        {
        int [] a=new int[66000];
        while(true) {
        
";
for($i=0; $i<7000; $i++) {
  print "a[$i]=$i;\n";
}

print 
"
}
}
}
";
