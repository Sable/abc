// 3.6 White Space
<YYINITIAL> {
  {LineTerminator}               { registerOffset(); }
  {WhiteSpace}                   { }
}

