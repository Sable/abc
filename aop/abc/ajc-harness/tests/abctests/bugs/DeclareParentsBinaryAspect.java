public aspect DeclareParentsBinaryAspect {
    declare parents: DP2 extends DP1;
    declare parents: DP3 extends DP2;
    declare parents: DP4 implements DPI;
}
