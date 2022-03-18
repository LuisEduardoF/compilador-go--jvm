package ast;

import static typing.Type.NO_TYPE;

import java.util.ArrayList;
import java.util.List;

import tables.FuncTable;
import tables.VarTable;
import typing.Type;

// Implementação dos nós da AST.
public class AST {

	// Todos os campos são finais para simplificar, assim não precisa de getter/setter.
	// Note que não há union em Java, então aquele truque de ler
	// e/ou escrever o campo com formatos diferentes não funciona aqui.
	// Os campos 'data' NÃO ficam sincronizados!
	public  final NodeKind kind;
	public  final int intData;
	public  final float floatData;
	public  final String stringData;
	public  boolean boolData;
	public 	final int escopo; //se for NodeKind VAR tem que ter escopo
	public   Type type;
	private final List<AST> children; // Privado para que a manipulação da lista seja controlável.

	// Construtor completo para poder tornar todos os campos finais.
	// Privado porque não queremos os dois campos 'data' preenchidos ao mesmo tempo.
	private AST(NodeKind kind, int intData, float floatData, String stringData, boolean boolData, int escopo, Type type) {
		this.kind = kind;
		this.intData = intData;
		this.floatData = floatData;
		this.stringData = stringData;
		this.boolData = boolData;
		this.escopo = escopo;
		this.type = type; 
		this.children = new ArrayList<AST>();
	}

	// Cria o nó com um dado inteiro.
	public AST(NodeKind kind, int intData, Type type) {
		this(kind, intData, 0.0f, "", false, 0, type);
	}

	// Cria o nó com um dado float.
	public AST(NodeKind kind, float floatData, Type type) {
		this(kind, 0, floatData, "", false,0, type);
	}
	
	// Cria o nó com um dado String.
	public AST(NodeKind kind, String stringData, Type type) {
		this(kind, 0, 0.0f, stringData.replaceAll("\"", ""), false,0,type);
	}
	
	// Cria o nó com um dado boolean.
	public AST(NodeKind kind, boolean booleanData, Type type) {
		this(kind, 0, 0.0f, "", booleanData,0,type);
	}
	
	// Cria o nó com um dado variavel.
	public AST(NodeKind kind, int intData, int escopo, Type type) {
		this(kind, intData, 0.0f, "",false, escopo, type);
	}

	// Cria o nó com um dado variavel.
	public AST(NodeKind kind, int intData, int escopo, float floatData,Type type) {
		this(kind, intData, floatData, "",false, escopo, type);
	}

	// Adiciona um novo filho ao nó.
	public void addChild(AST child) {
		// A lista cresce automaticamente, então nunca vai dar erro ao adicionar.
		this.children.add(child);
	}

	// Retorna o filho no índice passado.
	// Não há nenhuma verificação de erros!
	public AST getChild(int idx) {
		// Claro que um código em produção precisa testar o índice antes para
		// evitar uma exceção.
	    return this.children.get(idx);
	}

	// Cria um nó e pendura todos os filhos passados como argumento.
	public static AST newSubtree(NodeKind kind, Type type, AST... children) {
		AST node = new AST(kind, 0, type);
	    for (AST child: children) {
	    	node.addChild(child);
	    }
	    return node;
	}
	
	public List<AST> getChildren(){
		return this.children;
	}

	public Type getType(){
		return this.type;
	}

	// Variáveis internas usadas para geração da saída em DOT.
	// Estáticas porque só precisamos de uma instância.
	private static int nr;
	private static VarTable vt;
	private static FuncTable ft;

	// Imprime recursivamente a codificação em DOT da subárvore começando no nó atual.
	// Usa stderr como saída para facilitar o redirecionamento, mas isso é só um hack.
	private int printNodeDot() {
		int myNr = nr++;

	    System.err.printf("node%d[label=\"", myNr);
	    if (this.type != NO_TYPE) {
	    	System.err.printf("(%s) ", this.type.toString());
	    }
	    if (this.kind == NodeKind.VAR_DECL_NODE || this.kind == NodeKind.VAR_USE_NODE) {
	    	if(this.escopo == 0) {
	    		System.err.printf("%s@", vt.getName(this.intData));
	    	}
	    	else {
	    		System.err.printf("%s@", ft.getVarTable(this.escopo-1).getName(this.intData));
	    	}
	    	
	    }else if(this.kind == NodeKind.FUNC_NODE){
	    	System.err.printf("%s@", ft.getName(this.intData));
	    }else if(this.kind == NodeKind.ARRAY_NODE){
	    	if(this.escopo == 0) {
	    		System.err.printf("%s[%.0f]@", vt.getName(this.intData),this.floatData);
	    	}
	    	else {
	    		System.err.printf("%s[%.0f]@", ft.getVarTable(this.escopo-1).getName(this.intData),this.floatData);
	    	}
	    }else {
	    	System.err.printf("%s", this.kind.toString());
	    }
	    if (NodeKind.hasData(this.kind)) {
	        if (this.kind == NodeKind.REAL_VAL_NODE) {
	        	System.err.printf("%.2f", this.floatData);
	        } else if (this.kind == NodeKind.STR_VAL_NODE) {
	        	System.err.print(this.stringData);
	        } else if (this.kind == NodeKind.INT_VAL_NODE) {
	        	System.err.printf("%d", this.intData);
	        } else if (this.kind == NodeKind.BOOL_VAL_NODE){
	        	if(this.boolData) System.err.print("true");
	        	else System.err.print("false");
	        } else {
	        	System.err.print(this.intData);
	        }
	    }
	    System.err.printf("\"];\n");

	    for (int i = 0; i < this.children.size(); i++) {
	        int childNr = this.children.get(i).printNodeDot();
	        System.err.printf("node%d -> node%d;\n", myNr, childNr);
	    }
	    return myNr;
	}

	// Imprime a árvore toda em stderr.
	public static void printDot(AST tree, VarTable table, FuncTable func) {
	    nr = 0;
	    vt = table;
	    ft = func;
	    
	    int tam = ft.getTable().size();
	    for(int i = 0; i< tam; i++) {
	    	VarTable aux = ft.getVarTable(i);
	    	int tamAux = aux.getSize();
		    for(int j = 0; j < tamAux; j++) {
		    	vt.addEntry(aux.getEntry(j));
		    }  
	    }

	    System.err.printf("digraph {\ngraph [ordering=\"out\"];\n");
	    tree.printNodeDot();
	    
	    System.err.printf("}\n");
	}
}
