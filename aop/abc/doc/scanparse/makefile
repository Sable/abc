all: 
# main 
	latex scanparse < /dev/null
	bibtex scanparse 
	latex scanparse < /dev/null
	dvips -P cmz -t letter -o scanparse.ps scanparse.dvi
	ps2pdf scanparse.ps
	latex2html -transparent -image_type gif scanparse.tex
	tar -zcvf scanparse.tar.gz scanparse.ps scanparse.pdf scanparse/

clean:
	rm *.aux *.bbl *.blg *.dvi *.log 
