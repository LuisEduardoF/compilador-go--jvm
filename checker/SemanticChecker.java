package checker;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import parser.GoParser;
import parser.GoParserBaseVisitor;
import parser.GoParser.ExpressionContext;
import tables.FuncTable;
import tables.StrTable;
import tables.VarTable;
import typing.Type;
import ast.AST;

/*
 * Analisador semântico de EZLang implementado como um visitor
 * da ParseTree do ANTLR. A classe EZParserBaseVisitor é gerada
 * automaticamente e já possui métodos padrão aonde o comportamento
 * é só visitar todos os filhos. Por conta disto, basta sobreescrever
 * os métodos que a gente quer alterar.
 * 
 * Por enquanto só há uma verificação simples de declaração de
 * variáveis usando uma tabela de símbolos. Funcionalidades adicionais
 * como análise de tipos serão incluídas no próximo laboratório.
 * 
 * O tipo Void indicado na super classe define o valor de retorno dos
 * métodos do visitador. Depois vamos alterar isso para poder construir
 * a AST.
 * 
 * Lembre que em um 'visitor' você é responsável por definir o
 * caminhamento nos filhos de um nó da ParseTree através da chamada
 * recursiva da função 'visit'. Ao contrário do 'listener' que
 * caminha automaticamente em profundidade pela ParseTree, se
 * você não chamar 'visit' nos métodos de visitação, o caminhamento
 * para no nó que você estiver, deixando toda a subárvore do nó atual
 * sem visitar. Tome cuidado neste ponto pois isto é uma fonte
 * muito comum de erros. Veja o método visitAssign_stmt abaixo para
 * ter um exemplo.
 */

public class SemanticChecker extends GoParserBaseVisitor<AST> {

	private StrTable st = new StrTable();   // Tabela de strings.
	private VarTable global = new VarTable(); //Tabela de variaveis globais
	private FuncTable ft = new FuncTable(); // Tabela de funcoes
	
    private VarTable vt;   // Ponteiro para uma tabela de variaveis;
    
    Type lastDeclType;  // Variável "global" com o último tipo declarado.
    
    private boolean passed = true;

    AST root;

    // Testa se o dado token foi declarado antes.
    void checkVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = vt.lookupVar(text);
   		int idx2 = global.lookupVar(text);
    	if (idx == -1 && idx2 == -1) {
    		System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' was not declared.\n",
				line, text);
    		passed = false;
            return;
        }
    }

    int tamArray = 0;
    
    // Cria uma nova variável a partir do dado token.
    AST newVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = vt.lookupVar(text);
   		int idx2 = global.lookupVar(text);
   		
        if (idx != -1) {
        	System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n",
                line, text, vt.getLine(idx));
        	passed = false;
            return null;
        }
        
        if(idx2 != -1) {
        	System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n",
                line, text, global.getLine(idx2));
        	passed = false;
            return null;
        }
        vt.addVar(text, line, lastDeclType,tamArray);
        tamArray = 0;
        idx = vt.lookupVar(text);
        
        return new AST(ast.NodeKind.VAR_DECL_NODE, idx,vt.getEscopo(), lastDeclType);
    }
    
    //token recebido é function decl
    void newFunc(GoParser.FunctionDeclContext ctx) {
    	String text = ctx.getChild(1).getText();
    	int line = ctx.IDENTIFIER().getSymbol().getLine();
   		int idx = ft.lookupFunc(text);
        if (idx != -1) {
        	System.err.printf(
    			"SEMANTIC ERROR (%d): function '%s' already declared at line %d.\n",
                line, text, ft.getLine(idx));
        	passed = false;
           
        }
        // Cria a lista de param
        List<Type> param = new ArrayList<Type>();
        
        List<GoParser.ParameterDeclContext> parameterDeclContext = ctx.signature().parameters().parameterDecl(); 
        int tam = parameterDeclContext.size();
        
        for(int i = 0; i < tam; i++) {
        	String tipo = parameterDeclContext.get(i).type_().typeName().IDENTIFIER().getSymbol().getText();
        	setLastDeclType(tipo);
        	param.add(this.lastDeclType);
        }
        
        //cria list retorno
        
        List<Type> returns = new ArrayList<Type>();
        
        if(ctx.signature().getChildCount() == 1) {
        	 ft.addFunc(text, line, param, returns, new VarTable());
        	 return;
        }
       
        
        List<GoParser.ParameterDeclContext> parameterDeclContext2 = ctx.signature().result().parameters().parameterDecl(); 
        int tam2 = parameterDeclContext2.size();
        
        for(int i = 0; i < tam2; i++) {
        	String tipo = parameterDeclContext2.get(i).type_().typeName().IDENTIFIER().getSymbol().getText();
        	setLastDeclType(tipo);
        	returns.add(this.lastDeclType);
        }
        
        ft.addFunc(text, line, param, param, new VarTable());
       
    }
    
    // Retorna true se os testes passaram.
    boolean hasPassed() {
    	return passed;
    }
    
    // Exibe o conteúdo das tabelas em stdout.
    void printTables() {
        System.out.print("\n\n");
        System.out.print(st);
        System.out.print("\n\n");
    	System.out.print(global);
    	System.out.print("\n\n");
    	System.out.print(ft);
    	System.out.print("\n\n");
    }
    
    void printAST() {
    	AST.printDot(root, global, ft);
    }
  	
    @Override
    public AST visitSourceFile(GoParser.SourceFileContext ctx){
    	
    	this.root = AST.newSubtree(ast.NodeKind.PROGRAM_NODE, Type.NO_TYPE);
    	
    	//variaveis globais
    	try {
	    	int tam = ctx.declaration().size();
	    	vt = global;
	    	vt.setEscopo(0);
	    	for(int i = 0;i < tam;i++){
	    		//this.root.addChild();
	    		for(AST child: visit(ctx.declaration(i)).getChildren()) this.root.addChild(child);
	    		
	    	}
    	}
    	catch(Exception decl) {
    		// Se não tiver variavel global
    	}

    	int tam = ctx.functionDecl().size();
	    for(int i = 0;i < tam;i++) {
	    	AST func = new AST(ast.NodeKind.FUNC_NODE, i, Type.NO_TYPE);
	    	this.newFunc(ctx.functionDecl(i));
	    	vt = ft.getVarTable(i);
	    	vt.setEscopo(i+1);
	    	AST funcVisit = visit(ctx.functionDecl(i));
	    	func.addChild(funcVisit);
	    	this.root.addChild(func);
	    }

    	return this.root;
    }
    
    void setLastDeclType(String tipo){
    	if(tipo.equals("bool")){
			this.lastDeclType = Type.BOOL_TYPE;
		}else if(tipo.equals("int") || tipo.equals("int8") || tipo.equals("int16") || tipo.equals("int64")){
			this.lastDeclType = Type.INT_TYPE;
		}else if(tipo.equals("string")){
			this.lastDeclType = Type.STRING_TYPE;
		}else if(tipo.equals("float64") || tipo.equals("float32")){
			this.lastDeclType = Type.FLOAT_TYPE;
		}
    }

    @Override
	public AST visitNilType(GoParser.NilTypeContext ctx){
		this.lastDeclType = Type.NULL_TYPE;
    	return null; // Java says must return something even when Void	
	}
	@Override
	public AST visitIntType(GoParser.IntTypeContext ctx){
		this.lastDeclType = Type.INT_TYPE;
    	return null; // Java says must return something even when Void	
	}
	@Override
	public AST visitStringType(GoParser.StringTypeContext ctx){
		this.lastDeclType = Type.STRING_TYPE;

		this.st.add(ctx.string_().getStop().getText());

    	return null; // Java says must return something even when Void	
	}
	@Override
	public AST visitFloatType(GoParser.FloatTypeContext ctx){
		this.lastDeclType = Type.FLOAT_TYPE;
    	return null; // Java says must return something even when Void	
	}
	@Override
	public AST visitArrayType(GoParser.ArrayTypeContext ctx){
		this.lastDeclType = Type.ARRAY_TYPE;
    	return null; // Java says must return something even when Void
	}
	@Override
	public AST visitBoolType(GoParser.BoolTypeContext ctx){
		this.lastDeclType = Type.BOOL_TYPE;
    	return null; // Java says must return something even when Void
	}

	private boolean testExpressionList(GoParser.VarDeclContext ctx, int i) {
		try {
			visit(ctx.varSpec(i).expressionList());
			return true;
		}catch(Exception e) {
			// System.out.println("[testExpressionList] Nao encontrei expression list");
			return false;
		}
	}
	
	private AST retornaFilhoValor(String s){
		if(this.lastDeclType == Type.BOOL_TYPE){
			
			return new AST(ast.NodeKind.BOOL_VAL_NODE,Boolean.parseBoolean(s),Type.BOOL_TYPE);
			
		}else if(this.lastDeclType == Type.INT_TYPE){
			
			return new AST(ast.NodeKind.INT_VAL_NODE, Integer.parseInt(s), Type.INT_TYPE);
					
		}else if(this.lastDeclType == Type.STRING_TYPE){
			
			return new AST(ast.NodeKind.STR_VAL_NODE, s, Type.STRING_TYPE);
			
		}else if(this.lastDeclType == Type.FLOAT_TYPE){
			
			return new AST(ast.NodeKind.REAL_VAL_NODE, Float.parseFloat(s), Type.FLOAT_TYPE);
			
		}
		
		return null;
		
	}
	
	@Override
	public AST visitVarDecl(GoParser.VarDeclContext ctx) {
		// Visita a declaração de tipo para definir a variável lastDeclType.
		int qtdVar = ctx.varSpec().size();
		
		AST father = AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE);
		AST aux;
		
		for(int i = 0;i<qtdVar;i++){
			if(this.testExpressionList(ctx, i) == true) {
				//caso var NomeVar Tipo = valor
				try{
					String tipo = ctx.varSpec(i).type_().typeName().IDENTIFIER().getSymbol().getText();
					
					int tam = ctx.varSpec(i).identifierList().IDENTIFIER().size();

					for(int j = 0;j < tam;j++){
						AST assing = AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE);
						setLastDeclType(tipo);
						
						aux = newVar(ctx.varSpec(i).identifierList().IDENTIFIER(j).getSymbol());
						if(aux == null) return null;
						assing.addChild(aux);
						
						
						AST valor = makeTreeAssignment(ctx.varSpec(i).expressionList().expression(j),assing);
						assing.addChild(valor);

						//System.out.println(assing.getChild(0).type +" "+ assing.getChild(1).type);
						if(assing.getChild(0).type != assing.getChild(1).type && (assing.getChild(0).type != Type.FLOAT_TYPE && assing.getChild(1).type == Type.INT_TYPE)){
							int line = ctx.getStop().getLine(); //qual a linha?
							typeError(line,valor.kind.toString(),assing.getChild(0).type,assing.getChild(1).type);
							return null;
						}

						
						
						father.addChild(assing);
					}

				//caso var NomeVar = valor
				}catch(Exception e){

					int tam = ctx.varSpec(i).expressionList().expression().size();
					
					// setLastDeclType(tipo);
					for(int k = 0;k < tam;k++){
						AST assing = AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE);
						
						visit(ctx.varSpec(i).expressionList().expression(k).primaryExpr().operand().literal().basicLit());
						
						aux = newVar(ctx.varSpec(i).identifierList().IDENTIFIER(k).getSymbol());
						if(aux == null) return null;
						assing.addChild(aux);

						AST valor = makeTreeAssignment(ctx.varSpec(i).expressionList().expression(k),assing);
						assing.addChild(valor);

						//System.out.println(assing.getChild(0).type +" "+ assing.getChild(1).type);
						if(assing.getChild(0).type != assing.getChild(1).type && (assing.getChild(0).type != Type.FLOAT_TYPE && assing.getChild(1).type == Type.INT_TYPE)){
							int line = ctx.getStop().getLine(); //qual a linha?
							typeError(line,valor.kind.toString(),assing.getChild(0).type,assing.getChild(1).type);
							return null;
						}

						
						
						father.addChild(assing);	
					}
				}

			}else {
				//declaraçao sem valores
				try{
					
					String tipo = ctx.varSpec(i).type_().typeName().IDENTIFIER().getSymbol().getText();
					
					int tam = ctx.varSpec(i).identifierList().IDENTIFIER().size();
					

					for(int j = 0;j < tam;j++){
						setLastDeclType(tipo);
						
						newVar(ctx.varSpec(i).identifierList().IDENTIFIER(j).getSymbol());	
						
					}
				}
				
				//declaracao array
				catch(Exception e){
					int tam = ctx.varSpec(i).identifierList().IDENTIFIER().size();
					for(int k = 0;k < tam;k++){
						tamArray = Integer.parseInt(ctx.varSpec(i).type_().typeLit().arrayType().arrayLength().getStop().getText());
						setLastDeclType(ctx.varSpec(i).type_().typeLit().arrayType().elementType().getStop().getText());
						
						newVar(ctx.varSpec(i).identifierList().IDENTIFIER(k).getSymbol());
					}
				}
			} 
			
		}
		
		return father;
	}
	
	@Override
	public AST visitShortVarDecl(GoParser.ShortVarDeclContext ctx){
		int tam = ctx.expressionList().expression().size();
		AST father = AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE);
		AST aux;
		
		for(int i = 0;i < tam;i++){
			//setar o tipo
			visit(ctx.expressionList().expression(i));
			
			AST assing = AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE);
			//AST valor = retornaFilhoValor(ctx.expressionList().expression(i).getStop().getText());

			AST valor = makeTreeAssignment(ctx.expressionList().expression(i),father);
			
			aux = newVar(ctx.identifierList().IDENTIFIER(i).getSymbol());
			if(aux == null) return null;
			assing.addChild(aux);
			assing.addChild(valor);
			
			
			father.addChild(assing);
		}
		
		return father;
	}	
	
	private void typeError(int line, String operation, Type t1,Type t2){
		System.out.printf("[typeError] SEMANTIC ERROR (%d): incopatible types %s and %s for operator %s\n",line,t1.toString(),t2.toString(),operation);
		passed = false;
	}

	private void assignNumError(int line) {
		System.out.printf("[assignNumError] SEMANTIC ERROR (%d): different number of operators in assignment\n", line);
		passed = false;
	}
	
	private void varNotDeclError(int line, String varName){
		System.out.printf("[varNotDeclError] SEMANTIC ERROR (%d): variable %s not declared\n",line,varName);
		passed = false;
	}
	private boolean testaConstante(GoParser.ExpressionContext ctx,String s) {
		GoParser.LiteralContext literal;
		if(s.equals("")) literal = ctx.primaryExpr().operand().literal();
		else literal = ctx.unaryExpr().expression().primaryExpr().operand().literal();
		if(literal != null) return true;
		else return false;
	}
	private ast.NodeKind defineTipoOperacao(String operacao) {
		if(operacao.equals("+")) {
			return ast.NodeKind.PLUS_NODE;
		}else if(operacao.equals("-")) {
			return ast.NodeKind.MINUS_NODE;
		}else if(operacao.equals("*")) {
			return ast.NodeKind.TIMES_NODE;
		}else if(operacao.equals("/")) {
			return ast.NodeKind.OVER_NODE;
		}else if(operacao.equals("==")){
			return ast.NodeKind.EQ_NODE;
		}else if(operacao.equals("<")){
			return ast.NodeKind.LT_NODE;
		}else if(operacao.equals(">")){
			return ast.NodeKind.RT_NODE;
		}else if(operacao.equals(">=")){
			return ast.NodeKind.ERT_NODE;
		}else if(operacao.equals("<=")){
			return ast.NodeKind.ELT_NODE;
		}else if(operacao.equals("&&")){
			return ast.NodeKind.AND_NODE;
		}else if(operacao.equals("||")){
			return ast.NodeKind.OR_NODE;
		}else if(operacao.equals("!=")){
			return ast.NodeKind.NEQ_NODE;
		}
		
		return null;
	}
	private String verificaUnaryExpression(GoParser.ExpressionContext ctx){
		try{
			String sinal = ctx.unaryExpr().getChild(0).getText();
			return sinal;
		}
		catch(Exception e){
			return "";
		}
	}
	private void funcNotDeclError(int line, String funcName){
		System.out.printf("[funcNotDeclError] SEMANTIC ERROR (%d): func %s not declared\n",line,funcName);
		passed = false;
	}
	private void funcNumArgsError(int line, String funcName) {
		System.out.printf("[funcNumArgsError] SEMANTIC ERROR (%d): func %s arguments mismatch\n",line,funcName);
		passed = false;
	}
	private void funcTypeArgsError(int line, String funcName,Type one, Type two) {
		System.out.printf("[funcTypeArgsError] SEMANTIC ERROR (%d): func %s arguments mismatch type %s and %s\n",line,funcName,one.toString(),two.toString());
		passed = false;
	}
	
	@Override
	public AST visitPrimaryExpr(GoParser.PrimaryExprContext ctx) {
		
		try {
			// Se tiver argumento
			AST teste = makeTreeFuncAssignment(ctx);
			return fazPai(teste);
		}catch(Exception e) {
			// Se não
			return visitChildren(ctx);
		}
		
	}
	
	private AST makeTreeFuncAssignment(GoParser.PrimaryExprContext ctx){
		// Criar a AST
				
		String func_name = ctx.primaryExpr().getStop().getText();
		int childs = ctx.arguments().getChildCount();

		int idx = ft.lookupFunc(func_name);
		if(idx == -1) {
			this.funcNotDeclError(ctx.primaryExpr().getStop().getLine(), func_name);
			return null;
		}
		AST func_decl = new AST(ast.NodeKind.FUNC_NODE, idx, Type.NO_TYPE);
		
		
		
		// Pegar os argumentos da função
		
		Integer numOfExpression = 0;
		GoParser.ExpressionListContext args_ctx = null;
		if(childs != 2){
			args_ctx = ctx.arguments().expressionList();
			numOfExpression = args_ctx.expression().size();
		}
		
		// AST(ast.NodeKind.VAR_USE_NODE, func_name, Type.INT_TYPE);
		List<Type> argumentos = new ArrayList<Type>();
		for (int i = 0; i < numOfExpression; i++){
			AST argument = makeTreeAssignment(args_ctx.expression(i), func_decl);
			
			argumentos.add(argument.getType());
			
			if(argument != null) func_decl.addChild(argument);
			else return null;
		}
		
		List<Type> entradas = ft.getTypes(idx);
		
		//tratamento de argumentos
		if(entradas.size() != argumentos.size()) {
			this.funcNumArgsError(ctx.primaryExpr().getStop().getLine(), func_name);
			return null;
		}
		
		for(int i = 0 ; i < entradas.size();i++) {
			if(entradas.get(i) != argumentos.get(i)) {
				this.funcTypeArgsError(ctx.primaryExpr().getStop().getLine(), func_name, entradas.get(i), argumentos.get(i));
				return null;
			}
		}
		
		//tratamento de retorno
		
		return func_decl;
	}
	

	private void outOfBoudariesArrayError(int line, String varName,int pos,int max) {
		System.out.printf("[outOfBoudariesArrayError] SEMANTIC ERROR (%d): position %d is out of boudaries of array %s with length %d\n",line,pos,varName,max);
		passed = false;
	}

	private AST makeTreeArrayAssignment(GoParser.PrimaryExprContext ctx) {
			
			String arr_name = ctx.primaryExpr().getStop().getText();
			
			int idx = vt.lookupVar(arr_name);
			int idx2 = global.lookupVar(arr_name);
			if(idx == -1 && idx2 == -2) {
				this.varNotDeclError(ctx.primaryExpr().getStop().getLine(), arr_name);
				return null;
			}
			VarTable aux;
			if(idx == -1) {
				aux = global;
				idx = idx2;

			}else {
				aux = vt;
			}
			
			int pos = Integer.parseInt(ctx.index().expression().getStop().getText());
			
			if(pos > aux.getTamArray(idx) || pos < 0){
				int line = ctx.getStop().getLine();
				outOfBoudariesArrayError(line,arr_name,pos,aux.getTamArray(idx));
				return null;
			}
			
			AST arr_use = new AST(ast.NodeKind.ARRAY_NODE, idx, aux.getEscopo(), pos, aux.getType(idx));

			return arr_use;
	}
	
	private AST makeTreeAssignment(GoParser.ExpressionContext ctx, AST ramo) {
		int numFilhos = ctx.getChildCount();
		if(numFilhos == 3) {
			// Expressao composta
			String operacao = ctx.getChild(1).getText();

			visit(ctx.expression(0));

			Type tipoDadosOperacao = this.lastDeclType;
			
			AST valor = AST.newSubtree(this.defineTipoOperacao(operacao), tipoDadosOperacao);
			
			AST left = this.makeTreeAssignment(ctx.expression(0), valor);

			AST right = this.makeTreeAssignment(ctx.expression(1), valor);

			

			if((left.type == Type.FLOAT_TYPE || right.type == Type.FLOAT_TYPE) && (right.type == Type.INT_TYPE || left.type == Type.INT_TYPE)){
				valor.type = Type.FLOAT_TYPE;

				if(left.kind != ast.NodeKind.VAR_USE_NODE && left.kind != ast.NodeKind.VAR_DECL_NODE)
					left.type = Type.FLOAT_TYPE;
				if(right.kind != ast.NodeKind.VAR_USE_NODE && right.kind != ast.NodeKind.VAR_DECL_NODE)
					right.type = Type.FLOAT_TYPE;

			}
			if(left.type == Type.FLOAT_TYPE && right.type == Type.FLOAT_TYPE) {
				valor.type = Type.FLOAT_TYPE;
			}
			
			if(left.type != right.type) {
				int line = ctx.getStop().getLine(); //qual a linha?
				typeError(line,operacao,left.type,right.type);
				return null;
			}

			valor.addChild(left);
			valor.addChild(right);

			return valor;
		}else {
			String sinal = verificaUnaryExpression(ctx);
			
			int numFilhos2;

			if(sinal.equals("")){
				// Se sinal for "", ou é um número sem sinal ou é uma função
				try{
					// Se rodar é um número sem sinal ( SAFE )
					numFilhos2 = ctx.primaryExpr().operand().getChildCount();
				}
				catch(Exception e){
					// Se não rodar é uma função ( F )
					// Pegar o nome da função
					try {
						// Funcao
						GoParser.ArgumentsContext argumentsContext = ctx.primaryExpr().arguments();
						
						return makeTreeFuncAssignment(ctx.primaryExpr());
					}catch(Exception e2) {
						// Array
						
						GoParser.IndexContext indexContext = ctx.primaryExpr().index();
						
						return makeTreeArrayAssignment(ctx.primaryExpr());
					}
				}
			}
			else{
				numFilhos2 = ctx.unaryExpr().expression().primaryExpr().operand().getChildCount();
			}

			
			if(numFilhos2 == 1) {
				// Expressao simples
				
				if(!this.testaConstante(ctx,sinal)) {
					
					// O operando eh uma variavel					
					Token token = ctx.getStop();
					this.checkVar(token);
					
					
					int idx = vt.lookupVar(token.getText());
					int escopo = 0;
					Type tipo = null;
					
					if(idx != -1) {
						escopo = vt.getEscopo();
						tipo = this.vt.getType(idx);
					}
					
					
					if(idx == -1) {
						idx = global.lookupVar(token.getText());
						escopo = 0;
						tipo = this.global.getType(idx);
					}
						
					return new AST(ast.NodeKind.VAR_USE_NODE, idx, escopo, tipo);
				}else {
					// O operando eh uma constante
					
					String constante; //constante é um tipo
					//nao possui sinal + ou -
					if(sinal.equals("")){
						visit(ctx.primaryExpr());
						constante = ctx.getStop().getText();
					}
					//possui sinal + ou -
					else{
						visit(ctx.unaryExpr().expression());
						constante = sinal + ctx.unaryExpr().expression().getStop().getText();
					}
					
					if(constante.contains(".")){
						this.lastDeclType = Type.FLOAT_TYPE;
					}
					return this.retornaFilhoValor(constante);
				}	
			}else {
				if(sinal.equals("")) return this.makeTreeAssignment(ctx.primaryExpr().operand().expression(), ramo);
				else return this.makeTreeAssignment(ctx.unaryExpr().expression(), ramo);
			}
		}
	}
	
	public AST fazPai(AST senpai) {
		AST compai = (AST.newSubtree(ast.NodeKind.PROGRAM_NODE,Type.NO_TYPE));
		compai.addChild(senpai);
		return compai;
	}
	
	@Override
	public AST visitAssignment(GoParser.AssignmentContext ctx){
		
		Type primeiroTipo = null;
		
		//é funcao ou o cara babou
		boolean funcao = true;
		if(ctx.expressionList(0).expression().size() != ctx.expressionList(1).expression().size()) {
			//if(ctx.expressionList(1).expression(0).primaryExpr().getC)
			funcao = false;
			if(ctx.expressionList(1).expression(0).primaryExpr().getChildCount() != 2){
				assignNumError(ctx.getStop().getLine());
				return null;
			}
			
		}
		
		int cont = 0;
		int i = 0;
		int j = 0;
		AST paiAssignTree = (AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE));
		AST assignTree = (AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE));
		boolean ok = true;
		while( cont < 2*ctx.expressionList(0).expression().size()) {
			if(i == 2 && funcao == false) break;
			
			GoParser.ExpressionContext expressao = ctx.expressionList(i).expression(j);

			String vari = expressao.getStop().getText();
			
			if(vari.equals("]")){
				vari = expressao.primaryExpr().primaryExpr().getStop().getText();
			}

			int idx = this.vt.lookupVar(vari);
			int idx2 = this.global.lookupVar(vari);
			
			if(idx == -1 && i == 0 &&  idx == -1){
				String varName = vari;
				int line = 10;
				varNotDeclError(line,varName);
				return null;
			}
			
			VarTable aux;
			if(idx == -1) {
				aux = global;
				idx = idx2;

			}else {
				aux = vt;
			}


			//confere se a variavel ta recebendo o mesmo tipo
			if(i == 0) {
				primeiroTipo = aux.getType(idx);
			}


			AST ramo = this.makeTreeAssignment(expressao, assignTree);
			
			//AST.printDot(ramo,global,ft);
			//visit(expressao);
			//System.out.println(primeiroTipo);


			if(primeiroTipo != ramo.type && i == 1 && ramo.type != Type.NO_TYPE){
				int line = expressao.getStop().getLine();
				typeError(line,ramo.kind.toString(),primeiroTipo,ramo.type);
				return null;
			}
			
			//System.out.println(this.lastDeclType);
			
			if(ramo != null && funcao == true) {
				assignTree.addChild(ramo);
			}
			else if(ramo != null && funcao == false) {
				paiAssignTree.addChild(ramo);
			}
			else return null;
			// (0, 0) (a) | (0, 1) (b) | (1, 1)  (1, 1) | (0,2) = (1,2) | (0,j) = (1,j)
			if(funcao) {
				if(cont % 2 == 0) {
					i = 1;
				}else{
					i = 0;
					j++;
					paiAssignTree.addChild(assignTree);
					assignTree = (AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE));
				}
				cont++;
			}else {
				
				if(j < ctx.expressionList(0).expression().size() - 1 && ok) {
					j++;
				}else {
					ok = false;
					j = 0;
					i++;
				}
				
				cont++;
				//(0, 0) a (0,1) b (1,0)
			}
			
		}
		
		
		if(funcao == false) return fazPai(paiAssignTree);
		if(funcao == true) return paiAssignTree;
		return null;
	}
	
	@Override
	public AST visitIfStmt(GoParser.IfStmtContext ctx){

		AST ifTree = AST.newSubtree(ast.NodeKind.IF_NODE,Type.NO_TYPE);

		ifTree.addChild(visit(ctx.block(0)));

		//tenta se tiver else
		try{
			ifTree.addChild(visit(ctx.block(1)));
		}
		catch(Exception e){
			//e.printStackTrace();
		}
		

		ifTree.addChild(makeTreeAssignment(ctx.expression(),ifTree));

		try{
			AST marretagem = visit(ctx.ifStmt());
			AST marretagem2 = marretagem.getChild(0);
			ifTree.addChild(marretagem2);
		}
		catch(Exception e){
			//e.printStackTrace();
		}		

		//AST.printDot(ifTree,vt);	

		return fazPai(ifTree);

	}
	
	@Override
	public AST visitForStmt(GoParser.ForStmtContext ctx) {
		AST forTree = AST.newSubtree(ast.NodeKind.REPEAT_NODE, Type.NO_TYPE);
		
		forTree.addChild(visit(ctx.block()));
		forTree.addChild(makeTreeAssignment(ctx.expression(), forTree));
		
		return fazPai(forTree);
	}

	@Override
	public AST visitBlock(GoParser.BlockContext ctx){
		AST blockTree = (AST.newSubtree(ast.NodeKind.BLOCK_NODE,Type.NO_TYPE));
		try {		
	    	int tam = ctx.statementList().statement().size();
	    	
	    	for(int i = 0;i < tam;i++){
	    		AST teste = visit(ctx.statementList().statement(i));	

	    		for(AST child: teste.getChildren()) blockTree.addChild(child);
	    	}
		}	catch(Exception e) {
			// System.out.printf("[visitBlock] Caiu exception statementList [%s]\n",e.toString());
		}
		
		return blockTree;

	}
}
