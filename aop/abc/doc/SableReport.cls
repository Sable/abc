\NeedsTeXFormat{LaTeX2e}

\ProvidesClass{SableReport}[1998/02/11 v1.0]

\DeclareOption*{\PassOptionsToClass{\CurrentOption}{article}}
\ProcessOptions
\LoadClass[10pt]{article}
\RequirePackage{graphics}
\RequirePackage{sfheaders}

%\setlength{\parindent}{0in}
\setlength{\parskip}{1mm}

\setlength{\topmargin}{0in}
\setlength{\oddsidemargin}{0in}
\setlength{\evensidemargin}{0in}
\setlength{\textheight}{8.9in}
\setlength{\textwidth}{6.5in}
\setlength{\headheight}{0in}
\setlength{\headsep}{0in}
\setlength{\marginparwidth}{0.5in}

\newcommand{\ReportNumber}[1]{\renewcommand{\SR@documenttitle}{Sable Technical Report No. #1}}
\newcommand{\SR@documenttitle}{}

\include{TitlePage}
