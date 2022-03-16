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

# Diretório aonde está a classe com a função main.
MAIN_PATH=checker

# Diretório para os arquivos .class
BIN_PATH=bin

# Diretório para os casos de teste
DATA=$(ROOT)/tests
IN=$(DATA)
FILE=$(IN)/HelloWorld.go

all: antlr javac
	@echo "Done."

# Opção -no-listener foi usada para que o ANTLR não gere alguns arquivos
# desnecessários para o momento. Isto será explicado melhor nos próximos labs.
antlr: GoLexer.g4 GoParser.g4
	$(ANTLR4) -no-listener -visitor -o $(GEN_PATH) GoLexer.g4 GoParser.g4

# Compila todos os subdiretórios e joga todos os .class em BIN_PATH pra organizar.
javac:
	rm -r -f $(BIN_PATH)
	mkdir $(BIN_PATH)
	$(JAVAC) $(CLASS_PATH_OPTION) -d $(BIN_PATH) */*.java

run:
	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $(FILE)

runall:
	-for FILENAME in $(IN)/*.go; do \
	 	echo -e "\nRunning $${FILENAME}" && \
	 	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $${FILENAME}; \
	done;

compile:
	java -jar jasmin-2.4/jasmin.jar *.j

clean:
	@rm -rf $(GEN_PATH) $(BIN_PATH)
	@rm -rf *.j
