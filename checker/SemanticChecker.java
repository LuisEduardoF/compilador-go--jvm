package checker;

import org.antlr.v4.runtime.Token;

import parser.GoParser;
import parser.GoParserBaseVisitor;
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
    	
    	this.root = AST.newSubtree(ast.NodeKind.PROGRAM_NODE,Type.NO_TYPE);
    	//System.out.println(ctx.functionDecl(0).block().statementList().statement(0).simpleStmt().shortVarDecl());
    	//visit(ctx.functionDecl(0).block().statementList().statement(0).simpleStmt().shortVarDecl());
    	visit(ctx.functionDecl(0));
    	return this.root;
    }

    
    
    //----------------------------------------------------------------VERIFICADORES DE TIPO----------------------------------------------------------------
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




	//----------------------------------------------------------------CRIAÇAO DE VARIAVEIS----------------------------------------------------------------
	@Override
	public AST visitVarDecl(GoParser.VarDeclContext ctx) {
		// Visita a declaração de tipo para definir a variável lastDeclType.
		

		int qtdVar = ctx.varSpec().size();
		
		for(int i = 0;i<qtdVar;i++){
			try{
				String tipo = ctx.varSpec(i).type_().typeName().IDENTIFIER().getSymbol().getText();
				
				int tam = ctx.varSpec(i).identifierList().IDENTIFIER().size();
				

				for(int j = 0;j < tam;j++){
					System.out.println(tipo);
					setLastDeclType(tipo);
					//visit(ctx.varSpec(0).type_().typeName());
					// Agora testa se a variável foi redeclarada.
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

		

		return null; // Java says must return something even when Void
	}
	@Override
	public AST visitShortVarDecl(GoParser.ShortVarDeclContext ctx){

		int tam = ctx.expressionList().expression().size();

		for(int i = 0;i < tam;i++){
			//setar o tipo
			visit(ctx.expressionList().expression(i).primaryExpr().operand().literal().basicLit());
			

			AST aux = newVar(ctx.identifierList().IDENTIFIER(i).getSymbol());
			System.out.println(aux);
			if(aux != null){
				this.root.addChild(aux);
			}
			else{
				System.out.println("ERRO DE ALOCACAO");
			}
			
		}
		
		return null;
	}
	
	//----------------------------------------------------------------CHECAGEM DE TIPO----------------------------------------------------------------
	
	
	private void typeError(int line, String operation, Type t1,Type t2){
		System.out.printf("SEMANTIC ERROS (%d): incopatible types %s and %s for operator %s",line,t1.toString(),t2.toString(),operation);
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
			System.out.printf("SEMANTIC ERROR (%d): conditional expression in '%s' is '%s' instead of '%s'.\n",
               line, s, t.toString(), Type.BOOL_TYPE.toString());
            passed = false;
		}

	}


	//----------------------------------------------------------------CHECAGEM DE TIPO----------------------------------------------------------------
	
	
	@Override
	public AST visitAssignment(GoParser.AssignmentContext ctx){
		
		System.out.println(ctx.assign_op().getStop().getText());
		
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
    

    /* 
    // Visita a regra var_decl: type_spec ID SEMI
    @Override
    public Void visitVar_decl(EZParser.Var_declContext ctx) {
    	// Visita a declaração de tipo para definir a variável lastDeclType.
    	visit(ctx.type_spec());
    	// Agora testa se a variável foi redeclarada.
    	newVar(ctx.ID().getSymbol());
    	return null; // Java says must return something even when Void
    }

    // Visita a regra assign_stmt: ID ASSIGN expr SEMI
	@Override
	public Void visitAssign_stmt(Assign_stmtContext ctx) {
		// Visita recursivamente a expressão da direita para procurar erros. 
		visit(ctx.expr());
		// Verifica se a variável a ser atribuída foi declarada.
		checkVar(ctx.ID().getSymbol());
		return null; // Java says must return something even when Void
	}

	// Visita a regra read_stmt: READ ID SEMI
	@Override
	public Void visitRead_stmt(Read_stmtContext ctx) {
		// Verifica se a variável que vai receber o valor lido foi declarada.
		checkVar(ctx.ID().getSymbol());
		return null; // Java says must return something even when Void
	}

	@Override
	// Visita a regra expr: STR_VAL
	// Valem os mesmos comentários do método visitBoolType.
	public Void visitExprStrVal(ExprStrValContext ctx) {
		// Adiciona a string na tabela de strings.
		st.add(ctx.STR_VAL().getText());
		return null; // Java says must return something even when Void
	}

	@Override
	// Visita a regra expr: ID
	// Valem os mesmos comentários do método visitBoolType.
	public Void visitExprId(ExprIdContext ctx) {
		// Verifica se a variável usada na expressão foi declarada.
		checkVar(ctx.ID().getSymbol());
		return null; // Java says must return something even when Void
	}
	*/
	
}
