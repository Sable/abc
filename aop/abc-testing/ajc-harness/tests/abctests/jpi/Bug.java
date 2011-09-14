import org.aspectj.testing.Tester;


jpi int JP(String a);
jpi int JP2(String a) extends JP(a);


public class Bug{

	exhibits int JP(String a): args(a);
    exhibits int JP2(String a): args(a);
    
    int bar(String x){
    	//Tester.checkEqual(8,x,"we expected 8 and saw "+x);    	
        return 1;
    }
    public static void main(String[] args){
        new Bug().bar("");
    }

}

aspect A{

    int around JP(String a){ 
    	return proceed(a);
    }
    int around JP2(String a){ 
    	return proceed(a);
    }
}