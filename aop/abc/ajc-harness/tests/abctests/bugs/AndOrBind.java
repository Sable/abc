import org.aspectj.testing.Tester;

aspect Aspect { 
 
after(Ant a) returning:     
     call (void Command.step(..)) &&  
      (target(PickUp) || target(Drop))  &&  
      args(a) 
     	{ 
           Tester.event("<combined"); 
        } 
 
after(Ant a) returning:     
     call (void Command.step(..)) &&  
    target(PickUp)  && args(a) 
	{ 
           Tester.event("<pickup"); 
 	} 
 
after(Ant a) returning:     
     call (void Command.step(..)) &&  
     target(Drop)  && args(a) 
	 { 
            Tester.event("<drop"); 
 	 } 
 
} 
 
class Ant {} 
 
abstract class Command { 
    public abstract void step(Ant a); 
} 
 
class PickUp extends Command {  
    public void step(Ant a) {} 
} 
 
class Drop extends Command {  
    public void step(Ant a) {} 
} 
 
public class AndOrBind { 
 
    public static void main(String[] args) { 
	Command c1 = new PickUp(); 
	Command c2 = new Drop(); 
	Ant a = new Ant(); 
        c1.step(a); 
	Tester.expectEvent("<combined");
	Tester.expectEvent("<pickup");
	c2.step(a); 
	Tester.expectEvent("<combined");
	Tester.expectEvent("<drop");
	Tester.checkAllEvents();
    } 
}
