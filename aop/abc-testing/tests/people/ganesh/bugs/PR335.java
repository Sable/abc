public class PR335 {
}


aspect Bug1 {
    pointcut collisionDamage2(int so) : cflow(args(so));
    before(Object so) : collisionDamage2(so) { }
}
