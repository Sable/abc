import org.aspectj.testing.Tester;

abstract class AbstractCommand{}
class ZoomCommand extends AbstractCommand{}
class CopyCommand extends AbstractCommand{}

<L extends AbstractCommand> jpi void CommandContractsCheckingView(L acommand);
<L extends AbstractCommand> jpi void CommandContractsNotifyingView(L acommand) extends CommandContractsCheckingView(acommand);
<L extends Object> jpi void CommandContractsCheckingView(L acommand);
<L extends Object> jpi void CommandContractsNotifyingView(L acommand) extends CommandContractsCheckingView(acommand);

public class C{
	
	public static int counter=0;
	
	<R extends AbstractCommand> exhibits void CommandContractsNotifyingView(R acommand) : call(* *(..)) && argsinv(acommand);

	public static void foo(AbstractCommand a){}
	public static void bar(ZoomCommand a){}
	public static void zar(CopyCommand a){}
	
	public static void main(String[] args){
		foo(new ZoomCommand());
		bar(new ZoomCommand());
		zar(new CopyCommand());
		Tester.checkEqual(counter,6,"expected 6 matches but saw "+counter);		
	}
	
}

aspect A{
	
	<M extends Object> before CommandContractsCheckingView(M acommand){
		Tester.check(false,"should not execute");		
	}
	
	<M extends AbstractCommand> before CommandContractsCheckingView(M acommand){
		System.out.println("before "+acommand.getClass().getName());
		C.counter++;
	}
	
	<M extends AbstractCommand> after CommandContractsNotifyingView(M acommand){
		System.out.println("after "+acommand.getClass().getName());
		C.counter++;		
	}
}