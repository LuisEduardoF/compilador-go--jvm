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
		}else if(tipo.equals("complex64") || tipo.equals("complex128")){
			this.lastDeclType = Type.IMAGINARY_TYPE;
		}else if(tipo.equals("int32")){
			this.lastDeclType = Type.RUNE_TYPE;
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
	public AST visitImaginaryType(GoParser.ImaginaryTypeContext ctx){
		this.lastDeclType = Type.IMAGINARY_TYPE;
    	return null; // Java says must return something even when Void	
	}
	@Override
	public AST visitRuneType(GoParser.RuneTypeContext ctx){
		this.lastDeclType = Type.RUNE_TYPE;
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
			System.out.println("[testExpressionList] Nao encontrei expression list");
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
				try{
					String tipo = ctx.varSpec(i).type_().typeName().IDENTIFIER().getSymbol().getText();
					
					int tam = ctx.varSpec(i).identifierList().IDENTIFIER().size();

					for(int j = 0;j < tam;j++){
						AST assing = AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE);
						setLastDeclType(tipo);
						
						AST valor = retornaFilhoValor(ctx.varSpec(i).expressionList().expression(j).getStop().getText());
						
						assing.addChild(newVar(ctx.varSpec(i).identifierList().IDENTIFIER(j).getSymbol()));
						assing.addChild(valor);
						
						father.addChild(assing);
					}
				}catch(Exception e){
					int tam = ctx.varSpec(i).expressionList().expression().size();
					for(int k = 0;k < tam;k++){
						AST assing = AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE);
						visit(ctx.varSpec(i).expressionList().expression(k).primaryExpr().operand().literal().basicLit());
						
						AST valor = retornaFilhoValor(ctx.varSpec(i).expressionList().expression(k).getStop().getText());
						
						assing.addChild(newVar(ctx.varSpec(i).identifierList().IDENTIFIER(k).getSymbol()));
						assing.addChild(valor);
						
						father.addChild(assing);	
					}
				}

			}else {
				try{
					String tipo = ctx.varSpec(i).type_().typeName().IDENTIFIER().getSymbol().getText();
					
					int tam = ctx.varSpec(i).identifierList().IDENTIFIER().size();
					

					for(int j = 0;j < tam;j++){
						setLastDeclType(tipo);
						newVar(ctx.varSpec(i).identifierList().IDENTIFIER(j).getSymbol());
					}
				}
					
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
			visit(ctx.expressionList().expression(i).primaryExpr().operand().literal().basicLit());
			
			AST assing = AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE);
			AST valor = retornaFilhoValor(ctx.expressionList().expression(i).getStop().getText());

			assing.addChild(newVar(ctx.identifierList().IDENTIFIER(i).getSymbol()));
			assing.addChild(valor);
			
			
			father.addChild(assing);
		}
		
		return father;
	}	
	
	private void typeError(int line, String operation, Type t1,Type t2){
		System.out.printf("[typeError] SEMANTIC ERROS (%d): incopatible types %s and %s for operator %s",line,t1.toString(),t2.toString(),operation);
		passed = false;
	}


	private void checkAssingTypes(int line,Type left,Type right){
		if(left == Type.FLOAT_TYPE && !(right == Type.INT_TYPE || right == Type.FLOAT_TYPE || right == Type.RUNE_TYPE)){
			typeError(line,"=",left,right);
		}
		else if(right != left){
			typeError(line,"=",left,right);
		}
	}

	private void checkBoolExpr(int line, String s, Type t){
		if(t != Type.BOOL_TYPE){
			System.out.printf("[checkBoolExpr] SEMANTIC ERROR (%d): conditional expression in '%s' is '%s' instead of '%s'.\n",
               line, s, t.toString(), Type.BOOL_TYPE.toString());
            passed = false;
		}

	}
	
	private AST makeTreeAssignment(GoParser.ExpressionContext ctx, AST ramo) {
		int numFilhos = ctx.getChildCount();
		if(numFilhos == 3) {
			// Expressao composta
			String operacao = ctx.getChild(1).getText();
			AST valor = new AST(ast.NodeKind.STR_VAL_NODE, operacao, Type.STRING_TYPE);
			
			AST left = this.makeTreeAssignment(ctx.expression(0), valor);
			valor.addChild(left);
			
			AST right = this.makeTreeAssignment(ctx.expression(1), valor);
			valor.addChild(right);
			
			return valor;
		}else {
			// Expressao simples
			int numFilhos2 = ctx.primaryExpr().operand().getChildCount();
			if(numFilhos2 == 1) {
				String operando = ctx.getStop().getText();
				System.out.println(operando);
				
				return new AST(ast.NodeKind.STR_VAL_NODE, operando, Type.STRING_TYPE);
			}else {
				return this.makeTreeAssignment(ctx.primaryExpr().operand().expression(), ramo);
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
		String op = ctx.assign_op().getStop().getText();
		AST assignTree = (AST.newSubtree(ast.NodeKind.ASSIGN_NODE,Type.NO_TYPE));
		for(int i = 0; i < 2; i++) {
			GoParser.ExpressionContext expressao = ctx.expressionList(i).expression(0);
			assignTree.addChild(this.makeTreeAssignment(expressao, assignTree));
		}

		AST.printDot(assignTree, vt);
		return fazPai(assignTree);
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
			System.out.printf("[visitBlock] Caiu exception statementList [%s]\n",e.toString());
		}
		
		return blockTree;

	}
}
