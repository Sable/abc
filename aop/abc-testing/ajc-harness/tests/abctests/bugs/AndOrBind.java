import org.aspectj.testing.Tester;

aspect Aspect { 
 
after(MyAnt a) returning:     
     call (void Command.step(..)) &&  
      (target(PickUp) || target(Drop))  &&  
      args(a) 
     	{ 
           Tester.event("<combined"); 
        } 
 
after(MyAnt a) returning:     
     call (void Command.step(..)) &&  
    target(PickUp)  && args(a) 
	{ 
           Tester.event("<pickup"); 
 	} 
 
after(MyAnt a) returning:     
     call (void Command.step(..)) &&  
     target(Drop)  && args(a) 
	 { 
            Tester.event("<drop"); 
 	 } 
 
} 
 
class MyAnt {} 
 
abstract class Command { 
    public abstract void step(MyAnt a); 
} 
 
class PickUp extends Command {  
    public void step(MyAnt a) {} 
} 
 
class Drop extends Command {  
    public void step(MyAnt a) {} 
} 
 
public class AndOrBind { 
 
    public static void main(String[] args) { 
	Command c1 = new PickUp(); 
	Command c2 = new Drop(); 
	MyAnt a = new MyAnt(); 
        c1.step(a); 
	Tester.expectEvent("<combined");
	Tester.expectEvent("<pickup");
	c2.step(a); 
	Tester.expectEvent("<combined");
	Tester.expectEvent("<drop");
	Tester.checkAllEvents();
    } 
}
