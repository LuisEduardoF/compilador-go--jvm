package checker;

import org.antlr.v4.runtime.Token;

import parser.GoParser;
import parser.GoParserBaseVisitor;
import tables.StrTable;
import tables.VarTable;
import typing.Type;

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
public class SemanticChecker extends GoParserBaseVisitor<Void> {

	private StrTable st = new StrTable();   // Tabela de strings.
    private VarTable vt = new VarTable();   // Tabela de variáveis.
    
    Type lastDeclType;  // Variável "global" com o último tipo declarado.
    
    private boolean passed = true;

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
    void newVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = vt.lookupVar(text);
        if (idx != -1) {
        	System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n",
                line, text, vt.getLine(idx));
        	passed = false;
            return;
        }
        vt.addVar(text, line, lastDeclType);
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
	public Void visitNilType(GoParser.NilTypeContext ctx){
		this.lastDeclType = Type.NULL_TYPE;
    		return null; // Java says must return something even when Void	
	}
	@Override
	public Void visitIntType(GoParser.IntTypeContext ctx){
		this.lastDeclType = Type.INT_TYPE;
    		return null; // Java says must return something even when Void	
	}
	@Override
	public Void visitStringType(GoParser.StringTypeContext ctx){
		this.lastDeclType = Type.STRING_TYPE;
    		return null; // Java says must return something even when Void	
	}
	@Override
	public Void visitFloatType(GoParser.FloatTypeContext ctx){
		this.lastDeclType = Type.FLOAT_TYPE;
    		return null; // Java says must return something even when Void	
	}
	@Override
	public Void visitImaginaryType(GoParser.ImaginaryTypeContext ctx){
		this.lastDeclType = Type.IMAGINARY_TYPE;
    		return null; // Java says must return something even when Void	
	}
	@Override
	public Void visitRuneType(GoParser.RuneTypeContext ctx){
		this.lastDeclType = Type.RUNE_TYPE;
    		return null; // Java says must return something even when Void	
	}
	@Override
	public Void visitArrayType(GoParser.ArrayTypeContext ctx){
		this.lastDeclType = Type.ARRAY_TYPE;
    		return null; // Java says must return something even when Void
	}
	
	@Override
	public Void visitVarDecl(GoParser.VarDeclContext ctx) {
	// Visita a declaração de tipo para definir a variável lastDeclType.
	String tipo = ctx.varSpec(0).type_().typeName().IDENTIFIER().getSymbol().getText();
	System.out.println("Tipo: " + tipo);
	
	visit(ctx.varSpec(0).type_().typeName());
	// Agora testa se a variável foi redeclarada.
	newVar(ctx.varSpec(0).identifierList().IDENTIFIER(0).getSymbol());
	return null; // Java says must return something even when Void
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
