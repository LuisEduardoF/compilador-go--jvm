# Compilador GO -> JVM

Esse é um projeto da dísciplina de Compiladores do curso de Ciência da Computação na Universidade Federal do Espírito Santo (UFES). Foi desenvolvido um compilador de Golang para JVM utilizando-se a linguagem Java e o ANTL4. Os alunos são: Enzo Cussuol, Luís Eduardo Câmara e Vitor Bonella.

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

Parar rodar todos os casos de testes, i.e, testar o Scanner, Parser, Checker e Gerador de Código para todos arquivos ".go" na pasta tests.

```sh
make runall
```

Caso queira testar um arquivo em específico, basta mudar a variável FILE no Makefile. Feito isso, basta rodar os comandos:

```sh
make
```

```sh
make run
```

```sh
make compile
```

Isso irá gerar um arquivo GoProgram.class, agora basta fazer:

```sh
java GoProgram
```

O resultado será a execução final do código de entrada .go inserido na variável FILE, lembrando que esse código foi compilado para byte-code da JVM e então executado.
