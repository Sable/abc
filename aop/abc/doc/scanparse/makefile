all: 
# main 
	latex main < /dev/null
	bibtex main 
	latex main < /dev/null
	dvips -P cmz -t letter -o main.ps main.dvi
	ps2pdf main.ps

clean:
	rm *.aux *.bbl *.blg *.dvi *.log 
