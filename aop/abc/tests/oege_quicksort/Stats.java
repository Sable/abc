privileged aspect Stats {
    private int partitions=0;
    private int swaps=0;

    pointcut pcount():
		call(private static QuickSort.Middle QuickSort.partition(Comparable,Comparable[],int,int)) && 
		withincode(public static void quicksort(Comparable[],int,int));

    before(): pcount() {
		partitions++;
    }
    
    pointcut scount():
    	call(private static void swap(Object [],int ,int )) 
    	&& cflow(call(public static void QuickSort.quicksort(Comparable[],int,int)));
    	
    before(): scount() {
    	swaps++;
    }
    
    pointcut init():
		call(public static void QuickSort.quicksort(Comparable[],int,int))
		&& !(cflowbelow(call(public static void QuickSort.quicksort(Comparable[],int,int))));

    before() : init() {
    	partitions = 0;
    	swaps = 0;
    }
    
    after() : init() {
    	System.out.println("partitions="+partitions+"\n"+
    	                   "swaps="+swaps+"\n");
    }
    
}
