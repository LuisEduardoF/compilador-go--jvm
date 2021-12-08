# Compilador GO -> JVM

## Instalação

```sh
git clone https://github.com/LuisEduardoF/compilador-go--jvm
```

## Utilização

### ANTL4 PATH
Para utilizar, primeiro deve ser certificado que seu o ANTLR4 esteja corretamente intalado na pasta "/usr/local/lib" como manda a instalação padrão do ANTLR4.

Caso seu ANTLR4 esteja instalado em outro diretorio, basta abrir o Makefile e modificar a variavel "ANTLR_PATH" para o diretorio do ANTLR4.

```sh
ANTLR_PATH=$SEU_DIRETORIO_DE_INSTALACAO_DO_ANTLR4/antlr-4.9.2-complete.jar
```
### Rodando os testes

Para executar o antlr e o javac apenas utilize:
```sh
make
```

Para executar um caso de teste mude a variavel FILE no arquivo com o nome do arquivo desejado e coloque o arquivo na pasta tests.
```sh
FILE=$(IN)/$NOME_DO_SEU_ARQUIVO

#Depois de alterado para usar o comando run
make run
```

Parar rodar todos os casos de testes, ie, testar o Scanner e Parser para todos arquivos ".go" na pasta tests.
```sh
make runall
```



