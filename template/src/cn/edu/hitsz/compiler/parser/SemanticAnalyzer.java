package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Term;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.symtab.SymbolTableEntry;

import java.util.ArrayList;
import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {

    // 新建一个数据结构，用于辅助实现语义分析
    // 属性type为变量类型，token为终结符
    class Type_Token{
        private SourceCodeType type;
        private Token token;

        public Type_Token(SourceCodeType type, Token token){
            this.type = type;
            this.token = token;
        }
        public Type_Token(Token token){
            this.token = token;
        }
        public Type_Token(){

        }

        public SourceCodeType getType() {
            return type;
        }

        public Token getToken() {
            return token;
        }
    }

    private Stack<Type_Token> stack_type = new Stack<>();     // 语义栈（栈中元素包含type和token）
    private SymbolTable symbolTable;



    @Override
    public void whenAccept(Status currentStatus) {

    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {

        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        if (production.index()==5){       // D -> int
            Type_Token temp = stack_type.pop();
            stack_type.push(temp);
        }
        else if(production.index()==4){   // S -> D id
            Type_Token id = stack_type.pop();
            Type_Token D = stack_type.pop();

            // 更新符号表
            SymbolTableEntry symbol = symbolTable.get(id.getToken().getText());
            if(D.getType() == SourceCodeType.Int){         // 由于只有int类型，只需判断一次
                symbol.setType(SourceCodeType.Int);
            }
            stack_type.push(new Type_Token());   //
        }
        else{
            int len_pop = production.body().size();                         // 要弹出语义栈的元素个数
            stack_pop(len_pop);                                             // 弹出
            stack_type.push(new Type_Token());
        }
        return;
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        Type_Token type_token=null;
        if(currentToken.getKindId().equals("int")){     // 由于只有int类型,只需要判断一次
            type_token = new Type_Token(SourceCodeType.Int, currentToken);
        }else{
            type_token = new Type_Token(currentToken);
        }

        stack_type.push(type_token);
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        this.symbolTable = table;
    }
    // 为状态栈和符号栈弹出n个元素
    private void stack_pop(int n) {
        for (int i=0;i<n;i++){
            stack_type.pop();
        }
    }
}

