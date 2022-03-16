package code;

import ast.AST;
import ast.ASTBaseVisitor;
import tables.FuncTable;
import tables.StrTable;
import tables.VarTable;
import typing.Type;

public class CodeGen extends ASTBaseVisitor<Integer>{

    private Instruction code[]; // Code memory
	private StrTable st;
	private VarTable vt;
    private VarTable gt;
    private FuncTable ft;

    public CodeGen(VarTable vt,StrTable st, FuncTable ft, VarTable gt){
        this.vt = vt;
        this.st = st;
        this.ft = ft;
        this.gt = gt;
    }

    int lenFunctionInput = 0; //variavel que tem o tamanho da entrada

    public void execute(AST tree){
        visit(tree);
    }

    public void emit(String s,int addr){
        if (addr != -1){
            System.out.print(s+" ");
            System.out.println(addr);
        }else{
            System.out.println(s);
        }
    }

    @Override
    protected Integer visitAssign(AST node){
        AST r = node.getChild(1);
	    visit(r);
	    int addr = lenFunctionInput + node.getChild(0).intData; //Tamanho da entrada + Registrador da variavel(intData)
        
        //System.out.println("Addr:" + addr); // Debug ADDR

	    Type varType = vt.getType(node.getChild(0).intData); 
                         
	    if (varType == Type.FLOAT_TYPE) {
            emit("fstore", addr);
	    } else if(varType == Type.INT_TYPE){ // All other types, include ints, bools and strs.
	        emit("istore", addr);
	    }
	    return -1; // This is not an expression, hence no value to return.
    }
    //iload 2
    //iload 3
    //sum
    //istore
	@Override
    protected Integer visitEq(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));
        
        return -1;
    }   

	@Override
    protected Integer visitBlock(AST node){
        //ERRADOOOOOO

        int qtdChild = node.getChildren().size();
        for(int i = 0;i< qtdChild;i++){
            visit(node.getChild(i));
        }

        return -1;
    }

	@Override
    protected Integer visitBoolVal(AST node){
        return -1;        
    }

	@Override
    protected Integer visitIf(AST node){
        return -1;        
    }

	@Override
    protected Integer visitIntVal(AST node){
        emit("ldc",node.intData);
        return -1;   
    }

	@Override
    protected Integer visitLt(AST node){
        return -1;
    }

    @Override
    protected Integer visitRt(AST node){
        return -1;
    }

    @Override
    protected Integer visitErt(AST node){
        return -1;
    }
    
    @Override
    protected Integer visitElt(AST node){
        return -1;
    }

	@Override
    protected Integer visitMinus(AST node){
        return -1;
    }

	@Override
    protected Integer visitOver(AST node){
        return -1;
    }

	@Override
    protected Integer visitPlus(AST node){
        return -1;
    }

	@Override
    protected Integer visitProgram(AST node){
        
        int qtdChild = node.getChildren().size();
        for(int i =0;i<qtdChild;i++){
            visit(node.getChild(i));
        }
        
        return -1;
    }

	@Override
    protected Integer visitRead(AST node){
        return -1;
    }

	@Override
    protected Integer visitRealVal(AST node){
        return -1;
    }

	@Override
    protected Integer visitRepeat(AST node){
        return -1;
    }

	@Override
    protected Integer visitStrVal(AST node){
        return -1;
    }

	@Override
    protected Integer visitTimes(AST node){
        return -1;
    }

	@Override
    protected Integer visitVarDecl(AST node){
        return -1;
    }

	@Override
    protected Integer visitVarList(AST node){
        return -1;
    }

	@Override
    protected Integer visitVarUse(AST node){
        return -1;
    }

	@Override
    protected Integer visitWrite(AST node){
        return -1;
    }

    @Override
    protected Integer visitFunc(AST node){
        this.lenFunctionInput = this.ft.getParamSize(node.intData);

        String nome = ft.getName(node.intData);
        String method = ".method public static "+nome+ "([Ljava/lang/String;)V";
        
        emit(method,-1);
        visit(node.getChild(0));
        return -1;
    }

    @Override
    protected Integer visitAndNode(AST node){
        return -1;
    }

    @Override
    protected Integer  visitOrNode(AST node){
        return -1;
    }

    @Override
    protected Integer visitNeqNode(AST node){
        return -1;
    }

    @Override
    protected Integer visitModNode(AST node){
        return -1;
    }

    @Override
    protected Integer visitArrayNode(AST node){
        return -1;
    }
}
