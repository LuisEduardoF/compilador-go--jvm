# Modifique as variaveis conforme o seu setup.

JAVA=java
JAVAC=javac

ROOT=$(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))

# Certifique-se de que o antlr esteja instalado em /usr/local/lib

ANTLR_PATH=/usr/local/lib/antlr-4.9.2-complete.jar
CLASS_PATH_OPTION=-cp .:$(ANTLR_PATH)

# Comandos como descritos na página do ANTLR.
ANTLR4=$(JAVA) -jar $(ANTLR_PATH)
GRUN=$(JAVA) $(CLASS_PATH_OPTION) org.antlr.v4.gui.TestRig

# Diretório para aonde vão os arquivos gerados.
GEN_PATH=parser

# Diretório para os casos de teste
DATA=$(ROOT)/tests
IN=$(DATA)
FILE=$(IN)/HelloWorld.go

all: antlr javac
	@echo "Done."

# Opção -no-listener foi usada para que o ANTLR não gere alguns arquivos
# desnecessários para o momento. Isto será explicado melhor nos próximos labs.
antlr: GoLexer.g4 GoParser.g4
	$(ANTLR4) -no-listener -o $(GEN_PATH) GoLexer.g4 GoParser.g4

javac:
	$(JAVAC) $(CLASS_PATH_OPTION) $(GEN_PATH)/*.java

# 'Go' é o prefixo comum das duas gramáticas (GoLexer e GoParser).
# 'sourceFile' é a regra inicial de GoParser.
run:
	cd $(GEN_PATH) && $(GRUN) Go sourceFile $(FILE)

#Rodar com a opção -gui
run-debug:
	cd $(GEN_PATH) && $(GRUN) Go sourceFile $(FILE) -gui

runall:
	-for FILENAME in $(IN)/*.go; do \
	 	cd $(GEN_PATH) && \
	 	echo -e "\nRunning $${FILENAME}" && \
	 	$(GRUN) Go sourceFile $${FILENAME} && \
	 	cd .. ; \
	done;

clean:
	@rm -rf $(GEN_PATH)
