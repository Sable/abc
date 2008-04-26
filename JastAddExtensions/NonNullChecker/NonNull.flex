<YYINITIAL> {
  "@NonNull"                    { return sym(Terminals.NOTNULL); }
  "/*@NonNull*/"                    { return sym(Terminals.NOTNULL); }
  "@Nullable"                    { return sym(Terminals.NULLABLE); }
  "/*@Nullable*/"                    { return sym(Terminals.NULLABLE); }
  "@Raw"                        { return sym(Terminals.RAW); }
  "/*@Raw*/"                        { return sym(Terminals.RAW); }
  "@RawThis"                    { return sym(Terminals.RAWTHIS); }
  "/*@RawThis*/"                    { return sym(Terminals.RAWTHIS); }
}
