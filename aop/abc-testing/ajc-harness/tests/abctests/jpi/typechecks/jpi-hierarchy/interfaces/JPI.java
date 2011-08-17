package interfaces;

import java.lang.*;

jpi void Root(int a);
jpi void child(int a, String b) extends Root(a);
jpi void child2(int a) extends Root(a);