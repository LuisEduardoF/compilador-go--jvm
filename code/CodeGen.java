package code;

import ast.AST;
import ast.ASTBaseVisitor;
import ast.NodeKind;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
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
    private BufferedWriter funcwriter;
    
    public CodeGen(VarTable vt,StrTable st, FuncTable ft, VarTable gt) throws IOException{
        this.vt = vt;
        this.st = st;
        this.ft = ft;
        this.gt = gt;
        this.writer = new BufferedWriter(new FileWriter("out.j"));
        // this.funcwriter = new BufferedWriter(new FileWriter("functions.j"));
    }
    //IDEIA -> write Jasmin imprime dependendo de uma flag main = 0;
    int lenFunctionInput = 0; //variavel que tem o tamanho da entrada
    NodeKind lastNodeTypeVisited;

    // public void writeFunction(String str){
    //     try{
    //         this.funcwriter.write(str);
    //     }catch(Exception e){
    //         e.printStackTrace();
    //     }
    // }

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

    //public void montaCabecalhoFunction(){
        //this.writeFunction(".class public Functions\n.super java/lang/Object\n\n");
    //}

    @Override
    public void execute(AST tree){
        montaCabecalho();
        // montaCabecalhoFunction();
        visit(tree);
    }

    public void emit(String s, int addr) {
        if (addr != -1){
            this.writeJasmin("\t" + s + " ");
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
	    } else if(varType == Type.INT_TYPE || varType == Type.BOOL_TYPE){
	        emit("istore", addr);
	    }else if(node.type == Type.STRING_TYPE){
	        emit("astore", addr);
	    } 

	    return -1;
    }

    String lastStringGenerate;
    public void BotaIF(Type t, int bgn){

        if(this.flagCompostLogical == 0) this.lastStringGenerate = "L" + UUID.randomUUID().toString().substring(0, 5);
       
        String ifType;
        if(ifModo == 0){
            ifType = checkIfType(t);
            emit(ifType + "saiif" + bgn, -1); //errado
        }else{
            ifType = checkIfType(t);
            emit(ifType+ this.lastStringGenerate,-1);
        }  

    }

	@Override
    protected Integer visitEq(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));
        
        if(this.negOR == 0) this.lastNodeTypeVisited = node.kind;
        else this.lastNodeTypeVisited = NodeKind.NEQ_NODE;

        BotaIF(node.getChild(1).getType(), lastbgn);

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
        if(node.boolData){
            emit("iconst_1",-1);
        }else{
            emit("iconst_0", -1);
        }
        
        return -1;     
    }

    private String checkIfType(Type tipo){
        
        if(tipo == Type.INT_TYPE){
            switch(this.lastNodeTypeVisited){
                case NEQ_NODE:
                    return "if_icmpeq ";
                case EQ_NODE:
                    return "if_icmpne ";
                case LT_NODE:
                    return "if_icmpge ";
                case RT_NODE:
                    return "if_icmple ";
                case ERT_NODE:
                    return "if_icmplt ";
                case ELT_NODE:
                    return "if_icmpgt ";
                default:
                    return ""; // Não é operação
            }
        }else{
            if(this.lastNodeTypeVisited == NodeKind.ELT_NODE || this.lastNodeTypeVisited == NodeKind.LT_NODE) emit("fcmpg", -1);
            else emit("fcmpl",-1);
            
            switch(this.lastNodeTypeVisited){
                case EQ_NODE:
                    return "ifne "; 
                case NEQ_NODE:
                    return "ifeq "; 
                case RT_NODE:
                    return "ifle ";
                case LT_NODE:
                    return "ifge ";
                case ELT_NODE:
                    return "ifgt ";
                case ERT_NODE:
                    return "iflt ";
                default:
                    return ""; // Não é operação
            }
        }
    }

    int saiflag = 0; //pode estourar o numero
    int ifModo = 0; // 0 = IF Sem else , 1 = IF Else , 2 = if else if
    int lastbgn;

	@Override
    protected Integer visitIf(AST node){ //IF encadeado não funciona!!!
        int bgn = this.saiflag;
        this.lastbgn = bgn;

        
        
        if(node.getChildren().size() == 2){ //caso base if
            this.ifModo = 0;
            visit(node.getChild(1)); //condição            
            if(node.getChild(1).kind == NodeKind.OR_NODE){
                this.writeJasmin(this.salvaUltimo+":\n"); // Se for OR
            }
            visit(node.getChild(0)); //block

        }else{
            
            Boolean elseTest = node.getChild(1).kind == NodeKind.BLOCK_NODE;
            if(elseTest){ //caso seja um else
                this.ifModo = 1; 
                visit(node.getChild(2)); //condição
                
                if(node.getChild(2).kind == NodeKind.OR_NODE){
                    this.writeJasmin(this.salvaUltimo+":\n"); // Se for OR
                }

                visit(node.getChild(0)); //block
                emit("goto " + "saiif" + bgn, -1);
            }
            else{ // ELSE IF
                this.ifModo = 1;
                visit(node.getChild(1)); //condição
                if(node.getChild(1).kind == NodeKind.OR_NODE){
                    this.writeJasmin(this.salvaUltimo+":\n"); // Se for OR
                }

                visit(node.getChild(0)); //block
                emit("goto "+ "saiif" + bgn, -1);
            }
            this.writeJasmin(this.lastStringGenerate+":\n"); // SE for AND
            if(elseTest) visit(node.getChild(1));
            else visit(node.getChild(2));
        }
        if(bgn == this.saiflag){
            this.writeJasmin("saiif" + bgn + ":\n");
            this.saiflag++;
        }

        return -1;        
    }

	@Override
    protected Integer visitIntVal(AST node){
        
        if(node.type == Type.FLOAT_TYPE){
            emit("ldc " + node.intData + ".f", -1);
            return -1;
        }
        
        emit("ldc",node.intData);
        return -1;   
    }

	@Override
    protected Integer visitLt(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));

        if(this.negOR == 0){
            this.lastNodeTypeVisited = node.kind;
            
        }else this.lastNodeTypeVisited = NodeKind.ERT_NODE;
        BotaIF(node.getChild(1).getType(), lastbgn);
        return -1;
    }

    @Override
    protected Integer visitRt(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));

        if(this.negOR == 0) this.lastNodeTypeVisited = node.kind;
        else this.lastNodeTypeVisited = NodeKind.ELT_NODE;

        BotaIF(node.getChild(1).getType(), lastbgn);
        
        return -1;
    }

    @Override
    protected Integer visitErt(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));

        if(this.negOR == 0) this.lastNodeTypeVisited = node.kind;
        else this.lastNodeTypeVisited = NodeKind.LT_NODE;
        BotaIF(node.getChild(1).getType(), lastbgn);
        return -1;
    }
    
    @Override
    protected Integer visitElt(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));

        if(this.negOR == 0) this.lastNodeTypeVisited = node.kind;
        else this.lastNodeTypeVisited = NodeKind.RT_NODE;
        BotaIF(node.getChild(1).getType(), lastbgn);
        return -1;
    }

	@Override
    protected Integer visitMinus(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));

        if(node.getType() == Type.INT_TYPE){
            emit("isub", -1);
        }else if(node.getType() == Type.FLOAT_TYPE){
            emit("fsub", -1);
        }

        return -1;
    }

	@Override
    protected Integer visitOver(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));

        if(node.getType() == Type.INT_TYPE){
            emit("idiv", -1);
        }else if(node.getType() == Type.FLOAT_TYPE){
            emit("fdiv", -1);
        }

        return -1;
    }

	@Override
    protected Integer visitPlus(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));

        if(node.getType() == Type.INT_TYPE){
            emit("iadd", -1);
        }else if(node.getType() == Type.FLOAT_TYPE){
            emit("fadd", -1);
        }

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
        
        emit("new java/util/Scanner", -1);
        emit("dup", -1);
        emit("getstatic java/lang/System/in Ljava/io/InputStream;",-1);
        emit("invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V",-1);

        if(node.getChild(1).getType() == Type.INT_TYPE){
            emit("invokevirtual java/util/Scanner/nextInt()I",-1);
            emit("istore",node.getChild(1).intData+lenFunctionInput);
        } else if(node.getChild(1).getType() == Type.FLOAT_TYPE){
            emit("invokevirtual java/util/Scanner/nextFloat()F",-1);
            emit("fstore",node.getChild(1).intData+lenFunctionInput);
        }else if(node.getChild(1).getType() == Type.BOOL_TYPE){
            emit("invokevirtual java/util/Scanner/nextBoolean()Z",-1);
            emit("istore",node.getChild(1).intData+lenFunctionInput);
        }else if(node.getChild(1).getType() == Type.STRING_TYPE){
            emit("invokevirtual java/util/Scanner/nextLine()Ljava/lang/String;",-1);
            emit("astore",node.getChild(1).intData+lenFunctionInput);
        }

        return -1;
    }

	@Override
    protected Integer visitRealVal(AST node){
        emit("ldc " + node.floatData + "f", -1);
        return -1;
    }

    
    int qtdWhile = 0;
	@Override
    protected Integer visitRepeat(AST node){
        this.writeJasmin("label" + qtdWhile + ":\n");
        visit(node.getChild(1));
        String ifType = checkIfType(node.getChild(1).getChild(1).getType());
        emit(ifType+ "saiWhile" + qtdWhile, -1); 
        visit(node.getChild(0));
        emit("goto label" + qtdWhile,-1);
        this.writeJasmin("saiWhile"+qtdWhile+":\n");
        qtdWhile++;
        return -1;
    }

	@Override
    protected Integer visitStrVal(AST node){
        emit("ldc " + "\"" + node.stringData + "\"",-1);
        return -1;
    }

	@Override
    protected Integer visitTimes(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));

        if(node.getType() == Type.INT_TYPE){
            emit("imul", -1);
        }else if(node.getType() == Type.FLOAT_TYPE){
            emit("fmul", -1);
        }

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
        int addr = node.intData+lenFunctionInput;
        if (node.type == Type.FLOAT_TYPE) {
            emit("fload", addr);
	    } else if(node.type == Type.INT_TYPE){
	        emit("iload", addr);
	    } else if(node.type == Type.STRING_TYPE){
	        emit("aload", addr);
	    } else if(node.type == Type.BOOL_TYPE){
	        emit("iload", addr);
	    }
        return -1;
    }

	@Override
    protected Integer visitWrite(AST node){
        emit("getstatic java/lang/System/out Ljava/io/PrintStream;", -1);

        visit(node.getChild(1));
        
        if(node.getChild(1).getType() == Type.INT_TYPE){
            emit("invokevirtual java/io/PrintStream/println(I)V", -1);
        } else if(node.getChild(1).getType() == Type.FLOAT_TYPE){
            emit("invokevirtual java/io/PrintStream/println(F)V", -1);
        }else if(node.getChild(1).getType() == Type.BOOL_TYPE){
            emit("invokevirtual java/io/PrintStream/println(Z)V", -1);
        }else if(node.getChild(1).getType() == Type.STRING_TYPE){
            emit("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V",-1);
        }
        return -1;
    }
    
    public String defineTipoJasmin(Type tipo) {
        if(tipo == Type.INT_TYPE) return "I";
        else if(tipo == Type.FLOAT_TYPE) return "F";
        else if(tipo == Type.BOOL_TYPE) return "Z";
        else if(tipo == Type.STRING_TYPE) return "Ljava/lang/String;";
        else return "V";
    }

    int inFunc = 0;
    @Override
    protected Integer visitFunc(AST node){
        
        if(this.inFunc == 0){ // Declarando Função
            inFunc = 1;
            this.lenFunctionInput = this.ft.getParamSize(node.intData);

            String nome = ft.getName(node.intData);
            String method;
            if(nome.equals("main")){
                method = "\n.method public static " + nome + "([Ljava/lang/String;)V\n"; //trocar
            } else{
                String parametros = "(";
                List<Type> param = ft.getTypes(node.intData);
                for(int i = 0; i < ft.getParamSize(node.intData); i++){
                   parametros += defineTipoJasmin(param.get(i));
                }
                parametros += ")";
                method = "\n.method public static " + nome + parametros + "\n"; //trocar
            }
            
            this.writeJasmin(method);
            emit(".limit stack 20", -1);
            emit(".limit locals 20\n", -1);
            visit(node.getChild(0));
            emit("return",-1);
            this.writeJasmin("\n.end method\n");
            inFunc = 0;
        }
        else{ // Chamando Função
            for(int i = 0; i < this.ft.getParamSize(node.intData); i++){
                visit(node.getChild(i));
            }
            String nome = "GoProgram/" + ft.getName(node.intData) + "(I)V"; // trocar
            emit("invokestatic " + nome ,-1);

        }
        
        return -1;
    }

    int flagCompostLogical = 0;
    @Override
    protected Integer visitAndNode(AST node){
        visit(node.getChild(0));
        this.flagCompostLogical = 1;

        visit(node.getChild(1));
        this.flagCompostLogical = 0;
        
        return -1;
    }

    int negOR = 0;
    String salvaUltimo;
    @Override
    protected Integer visitOrNode(AST node){
        // emit("or_label:" + , -1);

        this.negOR = 1;
        visit(node.getChild(0));
        this.negOR = 0;
        this.flagCompostLogical = 1;
        this.salvaUltimo = this.lastStringGenerate;
        this.lastStringGenerate = "L" + UUID.randomUUID().toString().substring(0, 5);
        visit(node.getChild(1));
        this.flagCompostLogical = 0;
        
        return -1;

    }

    @Override
    protected Integer visitNeqNode(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));
        
        if(this.negOR == 0) this.lastNodeTypeVisited = node.kind;
        else this.lastNodeTypeVisited = NodeKind.EQ_NODE;
        BotaIF(node.getChild(1).getType(), lastbgn);
        return -1;
    }

    @Override
    protected Integer visitModNode(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));

        if(node.getType() == Type.INT_TYPE){
            emit("irem", -1);
        }else if(node.getType() == Type.FLOAT_TYPE){
            emit("frem", -1);
        }

        return -1;
    }

    @Override
    protected Integer visitArrayNode(AST node){
        

        return -1;
    }
}
