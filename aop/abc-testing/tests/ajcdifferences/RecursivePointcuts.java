public aspect RecursivePointcuts {

    pointcut a() : b();
    pointcut b() : a();

}