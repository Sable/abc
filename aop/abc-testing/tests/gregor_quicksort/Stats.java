//package quicksort.aspectj;

aspect Stats {

    private int partitions = 0;
    private int swaps      = 0;

    /**
     * Calls to quicksort. These are the only public entry to QuickSort.
     */
    pointcut sort():
        call(public void QuickSort.quicksort(Comparable[], int, int));

    /**
     * Just in case QuickSort.quicksort or one of its helpers call quicksort
     * recursively.
     */
    pointcut entry():
        sort() && !cflowbelow(sort());

    /**
     * Calls to partition.
     * This doesn't even really need the within, since it explicitly
     * acknowledges that partition is private.
     */
    pointcut partition(): //!!! was pcount
        call(private * QuickSort.partition(Comparable, Comparable[], int, int))
        && within(QuickSort);

    /**
     * Calls to swap.
     * This doesn't even really need the within, since it explicitly
     * acknowledges that partition is private.
     */
    pointcut swap(): //!!! was scount
        call(private * swap(Object [],int ,int ))
        && within(QuickSort);


    before(): partition() { partitions++; }
    before(): swap()      { swaps++;      }

    before() : entry() {
        partitions = 0;
          swaps = 0;
    }

    after() returning: entry() {
          System.out.println("partitions= " + partitions + "\n"+
                             "swaps     = " + swaps      + "\n");
    }
}
