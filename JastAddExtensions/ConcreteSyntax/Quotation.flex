<YYINITIAL> {
   "#Expr"                       { return sym(Terminals.EXPRQUOTE); }
   "#"                       { return sym(Terminals.ANTIQUOTE); }
}
