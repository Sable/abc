import org.aspectj.testing.Tester;

aspect CastAspect {
    before() : cast(*) {
        Tester.event("cast happened");
    }
}