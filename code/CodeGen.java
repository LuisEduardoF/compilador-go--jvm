package code;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
    private BufferedWriter writer;
    
    public CodeGen(VarTable vt,StrTable st, FuncTable ft, VarTable gt) throws IOException{
        this.vt = vt;
        this.st = st;
        this.ft = ft;
        this.gt = gt;
        this.writer = new BufferedWriter(new FileWriter("out.j"));
    }

    int lenFunctionInput = 0; //variavel que tem o tamanho da entrada

    public void writeJasmin(String str){
        try{
            this.writer.write(str);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void closeJasmin(){
        try{
            this.writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void montaCabecalho(){
        this.writeJasmin(".class public GoProgram\n.super java/lang/Object\n\n");
    }

    public void execute(AST tree){
        montaCabecalho();
        visit(tree);
    }

    public void emit(String s, int addr) {
        if (addr != -1){
            this.writeJasmin("\t" + s +" ");
            this.writeJasmin(addr + "\n");
        } else{
            this.writeJasmin("\t" + s + "\n");
        }
    }
    
    public void emit(String s, float addr){
        if (addr != -1){
            this.writeJasmin("\t" + s + " ");
            this.writeJasmin(String.format("%f\n",addr));
        } else{
            this.writeJasmin("\t" + s + "\n");
        }
    }

    @Override
    protected Integer visitAssign(AST node){
        AST r = node.getChild(1);
	    visit(r);
	    int addr = lenFunctionInput + node.getChild(0).intData; //Tamanho da entrada + Registrador da variavel(intData)
        
	    Type varType = vt.getType(node.getChild(0).intData); 
                         
	    if (varType == Type.FLOAT_TYPE) {
            emit("fstore", addr);
	    } else if(varType == Type.INT_TYPE){
	        emit("istore", addr);
	    }
	    return -1; // This is not an expression, hence no value to return.
    }

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
        emit("ldc", node.floatData);
        return -1;
    }

	@Override
    protected Integer visitRepeat(AST node){
        return -1;
    }

	@Override
    protected Integer visitStrVal(AST node){
        emit("ldc " + "\"" + node.stringData + "\"",-1);
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
        emit("getstatic java/lang/System/out Ljava/io/PrintStream;", -1);

        visit(node.getChild(1));

        if(node.getChild(1).getType() == Type.INT_TYPE)
            emit("invokevirtual java/io/PrintStream/println(I)V", -1);
        else if(node.getChild(1).getType() == Type.STRING_TYPE)
            emit("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V",-1);
        
        return -1;
    }

    @Override
    protected Integer visitFunc(AST node){
        this.lenFunctionInput = this.ft.getParamSize(node.intData);

        String nome = ft.getName(node.intData);
        String method = "\n.method public static " + nome + "([Ljava/lang/String;)V\n";
        
        this.writeJasmin(method);
        emit(".limit stack 20\n", -1);
        visit(node.getChild(0));

        this.writeJasmin("\n.end method\n");
        return -1;
    }

    @Override
    protected Integer visitAndNode(AST node){
        return -1;
    }

    @Override
    protected Integer visitOrNode(AST node){
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
