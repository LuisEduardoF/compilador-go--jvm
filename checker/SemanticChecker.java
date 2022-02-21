package checker;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import parser.GoParser;
import parser.GoParserBaseVisitor;
import parser.GoParser.ExpressionContext;
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
    private VarTable vt = new VarTable();   // Tabela de variáveis.
    
    Type lastDeclType;  // Variável "global" com o último tipo declarado.
    
    private boolean passed = true;

    AST root;

    // Testa se o dado token foi declarado antes.
    void checkVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = vt.lookupVar(text);
    	if (idx == -1) {
    		System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' was not declared.\n",
				line, text);
    		passed = false;
            return;
        }
    }
    
    // Cria uma nova variável a partir do dado token.
    AST newVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = vt.lookupVar(text);
        if (idx != -1) {
        	System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n",
                line, text, vt.getLine(idx));
        	passed = false;
            return null;
        }
        vt.addVar(text, line, lastDeclType);
        idx = vt.lookupVar(text);
        return new AST(ast.NodeKind.VAR_DECL_NODE, idx, lastDeclType);
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
    	System.out.print(vt);
    	System.out.print("\n\n");
    }
    
    void printAST() {
    	AST.printDot(root, vt);
    }
  	
    @Override
    public AST visitSourceFile(GoParser.SourceFileContext ctx){
    	
    	this.root = AST.newSubtree(ast.NodeKind.PROGRAM_NODE, Type.NO_TYPE);
    	
    	int tam = ctx.functionDecl().size();
    	for(int i = 0;i < tam;i++){
    		this.root.addChild(visit(ctx.functionDecl(i)));
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
		
		for(int i = 0;i<qtdVar;i++){
			if(this.testExpressionList(ctx, i) == true) {
				//caso var NomeVar Tipo = valor
				try{
					String tipo = ctx.varSpec(i).type_().typeName().IDENTIFIER().getSymbol().getText();
					
					int tam = ctx.varSpec(i).identifierList().IDENTIFIER().size();

					for(int j = 0;j < tam;j++){
						AST assing = AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE);
						setLastDeclType(tipo);
						

						assing.addChild(newVar(ctx.varSpec(i).identifierList().IDENTIFIER(j).getSymbol()));
						

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
						
						assing.addChild(newVar(ctx.varSpec(i).identifierList().IDENTIFIER(k).getSymbol()));

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
					int tam = ctx.varSpec(i).expressionList().expression().size();
					for(int k = 0;k < tam;k++){
						visit(ctx.varSpec(i).expressionList().expression(k).primaryExpr().operand().literal().basicLit());
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
		
		for(int i = 0;i < tam;i++){
			//setar o tipo
			visit(ctx.expressionList().expression(i));
			
			AST assing = AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE);
			//AST valor = retornaFilhoValor(ctx.expressionList().expression(i).getStop().getText());

			AST valor = makeTreeAssignment(ctx.expressionList().expression(i),father);

			assing.addChild(newVar(ctx.identifierList().IDENTIFIER(i).getSymbol()));

			

			assing.addChild(valor);
			
			
			father.addChild(assing);
		}
		
		return father;
	}	
	
	private void typeError(int line, String operation, Type t1,Type t2){
		System.out.printf("[typeError] SEMANTIC ERROR (%d): incopatible types %s and %s for operator %s\n",line,t1.toString(),t2.toString(),operation);
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

			if(sinal.equals("")) numFilhos2 = ctx.primaryExpr().operand().getChildCount();
			else numFilhos2 = ctx.unaryExpr().expression().primaryExpr().operand().getChildCount();

			
			if(numFilhos2 == 1) {
				// Expressao simples
				
				if(!this.testaConstante(ctx,sinal)) {
					
					// O operando eh uma variavel					
					Token token = ctx.getStop();
					this.checkVar(token);
										
					int idx = vt.lookupVar(token.getText());
										
					return new AST(ast.NodeKind.VAR_USE_NODE, idx, this.vt.getType(idx));
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
		AST assignTree = (AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE));
		Type primeiroTipo = null;
		for(int i = 0; i < 2; i++) {
			GoParser.ExpressionContext expressao = ctx.expressionList(i).expression(0);

			String vari = expressao.getStop().getText();

			if(this.vt.lookupVar(vari) == -1 && i == 0){
				String varName = "Teste";
				int line = 10;
				varNotDeclError(line,varName);
				return null;
			}

			AST ramo = this.makeTreeAssignment(expressao, assignTree);
			
			visit(expressao);
			
			//confere se a variavel ta recebendo o mesmo tipo
			if(i == 0) primeiroTipo = this.lastDeclType;

			if(primeiroTipo != ramo.type && i == 1){
				int line = expressao.getStop().getLine();
				typeError(line,ramo.kind.toString(),primeiroTipo,ramo.type);
				return null;
			}

			//System.out.println(this.lastDeclType);
			

			if(ramo != null) assignTree.addChild(ramo);
			else return null;
		}

		

		return fazPai(assignTree);
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
