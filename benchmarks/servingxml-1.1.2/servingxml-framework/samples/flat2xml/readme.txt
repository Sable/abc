Before running the hot-one2xml, you need to run build.

The batch files countries2xml.cmd, books2xml.cmd and messages2xml.cmd use 
convert flat files in the data directory to XML files in the output directory.
The batch files xml2countries.cmd and xml2books.cmd convert XML files in the
data directory to flat files in the output directory.

Run

  countries2xml
  
to convert the CSV file data/countries.csv to output/countries.xml. 

Run

  bad-countries2xml
  
to validate the CSV file data/bad-countries.csv row-by-row against the 
data/countries-record.xsd schema. 

Run

  books2xml
  
to convert the positional file data/books_pos.txt to output/books.xml. 

Run

  messages2xml
  
to convert the Java properties file data/messages.properties to output/messages.xml. 

Run

  xml2countries
  
to serialize the countries XML to the CSV file output/books.txt. 

Run

  xml2books
  
to serialize the books XML to the positional file output/books.txt. 

