@echo off
set CPATH=conf;lib\Xml2Txt.jar;lib\antlr-2.7.5.jar;lib\avalon-logkit-2.1.jar;lib\commons-cli-1.2.jar;lib\commons-collections-3.1.jar;lib\commons-lang-2.1.jar;lib\log4j-1.2.16.jar;lib\velocity-1.5.jar

java  -Xms512m -Xmx512m -cp %CPATH%  xml2txt.CUI --input-dir ..\testdata\xml --output-dir ..\txt --thread-count 2 --export-flat  --output-with-bom --template-file conf\xml2txt.vm 