import org.aspectj.testing.Tester;

abstract class AbstractCommand{}
class ZoomCommand extends AbstractCommand{}
class CopyCommand extends AbstractCommand{}

jpi void CommandContracts_CheckingView(AbstractCommand acommand);
jpi void CommandContracts_CheckingView(ZoomCommand acommand); 
jpi void CommandContracts_CheckingView(CopyCommand acommand);


public aspect a{
	
	public static int counter=0;
	
	exhibits void CommandContracts_CheckingView(AbstractCommand acommand) : call(* foo(..)) && argsinv(acommand);
	exhibits void CommandContracts_CheckingView(ZoomCommand acommand) : call(* bar(..)) && argsinv(acommand);
	exhibits void CommandContracts_CheckingView(CopyCommand acommand) : call(* zar(..)) && argsinv(acommand);
	
	public static void foo(AbstractCommand a){}
	public static void bar(ZoomCommand a){}
	public static void zar(CopyCommand a){}
	
	public static void main(String[] args){
		foo(new ZoomCommand());
		bar(new ZoomCommand());
		zar(new CopyCommand());
		Tester.checkEqual(counter,3,"expected 3 matches but saw "+counter);		
	}
	
	void around CommandContracts_CheckingView(AbstractCommand acommand){
		a.counter++;
		proceed(acommand);
	}
	void around CommandContracts_CheckingView(ZoomCommand acommand){
		a.counter++;
		proceed(acommand);
	}
	void around CommandContracts_CheckingView(CopyCommand acommand){
		a.counter++;
		proceed(acommand);
	}
	
}