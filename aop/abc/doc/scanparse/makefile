all: 
# main 
	latex main < /dev/null
	bibtex main 
	latex main < /dev/null
	dvips -P cmz -t letter -o main.ps main.dvi
	ps2pdf main.ps
	latex2html -transparent -image_type gif main.tex

clean:
	rm *.aux *.bbl *.blg *.dvi *.log 
