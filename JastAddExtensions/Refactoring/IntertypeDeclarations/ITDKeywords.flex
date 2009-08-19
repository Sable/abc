// Intertype Declarations Keywords
<YYINITIAL> {
  "declare"                      { return sym(Terminals.DECLARE); }
  "precedence"                   { return sym(Terminals.PRECEDENCE); }
  "parents"                      { return sym(Terminals.PARENTS); }
  "aspect"                       { return sym(Terminals.ASPECT); }
}
