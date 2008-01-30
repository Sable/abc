
package parser;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import model.Ant;
import command.*;

public class CommandParser {
    
      int lineCount = 0;
      Iterator tokens;
   
      public String nextToken() {
          if (tokens.hasNext())
              return ((String) tokens.next()).toLowerCase();
          else 
              return null;
      }
    
      private Iterator tokenise(String line) {
          char[] characters = line.toCharArray();
          List tokenList = new LinkedList();
          int i=0;
          while ( i < characters.length ) {
              while (i < characters.length && Character.isWhitespace(characters[i])) i++;
              if (i==characters.length)
                continue;
              int j = 1;
              while (i+j < characters.length && !Character.isWhitespace(characters[i+j])) j++;
              tokenList.add(new String(characters,i,j));
              i = i+j;
          }
          return tokenList.iterator();
      }
    
    
      public Command parse(String line) {
          lineCount++;
          tokens = tokenise(line);
          return parseCommand(); 
      }
    
      private Command parseCommand() {
          String next = nextToken();
          if (next.equals("sense")) {
              int sensedir = parseSenseDir();
              int st1 = parseState();
              int st2 = parseState();
              Condition cond = parseCondition();
              return new Sense(sensedir,st1,st2,cond);
          } else if (next.equals("mark")) {
              int i = parseMarker();
              int st = parseState();
              return new Mark(i,st);
          }if (next.equals("unmark")) {
              int i = parseMarker();
              int st = parseState();
              return new Unmark(i,st);
          } else if (next.equals("pickup")) {
              int st1 = parseState();
              int st2 = parseState();
              return new PickUp(st1,st2);
          } else if (next.equals("drop")) {
              int st = parseState();
              return new Drop(st);
          } else if (next.equals("turn")) {
              boolean left = parseLeftRight();
              int st = parseState();
              return new Turn(left,st);
          } else if (next.equals("move")) {
              int st1 = parseState();
              int st2 = parseState();
              return new Move(st1,st2);
          } else if (next.equals("flip")) {
              int p = parseInt();
              int st1 = parseState();
              int st2 = parseState();
              return new Flip(p,st1,st2);
          } else throw new RuntimeException("syntax error on line "+lineCount);
      }
    
      private int parseIntRange(int max) {
          String next = nextToken();
          int val = (new Integer(next)).intValue();
          if (val > max)
              throw new RuntimeException("number out of range on line "+lineCount);
          return val;
      }
    
      private int parseSenseDir() {
         String next = nextToken();
         if (next.equals( "here"))
              return Ant.SenseDir.HERE;
         else if (next.equals("ahead"))
              return Ant.SenseDir.AHEAD;
         else if (next.equals("leftahead"))
              return Ant.SenseDir.LEFTAHEAD;
         else if (next.equals("rightahead"))
              return Ant.SenseDir.RIGHTAHEAD;
         else throw new RuntimeException("sensedir out of range on line " +lineCount);
      }
    
      private int parseState() {
          return parseIntRange(9999);
      }
    
      private int parseMarker() {
          return parseIntRange(5);
      }
    
      private int parseInt() {
          String next = nextToken();
          return (new Integer(next)).intValue(); 
      }
    
      private boolean parseLeftRight() {
          String next = nextToken();
          return (next.equals("left"));
      }
    
      private Condition parseCondition() {
          String next = nextToken();
          if (next.equals("friend"))
              return new Friend();
          else if (next.equals("foe"))
              return new Foe();
          else if (next.equals("friendwithfood"))
              return new FriendWithFood();
          else if (next.equals("foewithfood"))
              return new FoeWithFood();
          else if (next.equals("food"))
              return new Food();
          else if (next.equals("rock"))
              return new Rock();
          else if (next.equals("marker")) {
              int i = parseMarker();
              return new Marker(i); }
          else if (next.equals("foemarker"))
              return new FoeMarker();
          else if (next.equals("home"))
              return new Home();
          else if (next.equals("foehome"))
              return new FoeHome();
          else throw new RuntimeException("syntax error on line "+lineCount);
      }
   
}
