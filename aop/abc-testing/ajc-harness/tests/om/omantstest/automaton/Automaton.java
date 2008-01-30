/*
 * Created on 18-Sep-2004
 *
 */
package automaton;


import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import model.Ant;
import command.Command;
import parser.CommandParser;

public class Automaton {

    protected Command[] instructions = new Command[10000];
    protected int maxCommand;
    
    public void step(Ant a) {
        Command c = instructions[a.getState()];
        c.step(a);
    }
    
    public void print(java.io.PrintStream out) {
        for (int i = 0; i < instructions.length; i++)
            out.print(instructions[i].toString());
    }
   
    public Automaton(DataInput in) throws IOException {
        maxCommand = 0;
        String line = in.readLine();
        CommandParser cp = new CommandParser();
        while (line != null) {
            instructions[maxCommand] = cp.parse(line);
            maxCommand++;
            line = in.readLine();
        }
    }
    
    
  

}
