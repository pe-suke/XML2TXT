set CPATH=conf;lib\Xml2Txt.jar;lib\antlr-2.7.5.jar;lib\avalon-logkit-2.1.jar;lib\commons-cli-1.2.jar;lib\commons-collections-3.1.jar;lib\commons-lang-2.1.jar;lib\log4j-1.2.16.jar;lib\velocity-1.5.jar

java  -Xms512m -Xmx512m -cp %CPATH%  xml2txt.CUI %1 %2 %3 %4 %5 %6 %7 %8 %9 %10