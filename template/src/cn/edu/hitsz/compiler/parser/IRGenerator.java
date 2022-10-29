package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {
    private Stack<IRValue> stack_addr = new Stack<>();     // 语义栈
//    private SymbolTable symbolTable;
    private ArrayList IR = new ArrayList<Instruction>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        String  cur_tokenKind_name = currentToken.getKind().getIdentifier(); // 获取当前词的类型名
        if (cur_tokenKind_name.equals("id")){
            stack_addr.push(IRVariable.named(currentToken.getText()));      // 将变量名入栈
        }
        else if(cur_tokenKind_name.equals("IntConst")) {
            stack_addr.push(IRImmediate.of(Integer.parseInt(currentToken.getText())));    // 将常量入栈
        }
        else{
            stack_addr.push(IRVariable.named(cur_tokenKind_name));
        }
        return;
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        Instruction temp;       // 存放指令
        switch (production.index()) {

            case 6 -> { // S -> id = E
                IRValue E = stack_addr.pop();
                stack_addr.pop();
                IRValue id = stack_addr.pop();
                temp = Instruction.createMov((IRVariable) id, E);
                IR.add(temp);

                stack_addr.push(IRVariable.named(""));
            }

            case 7 -> { // S -> return E
                IRValue E = stack_addr.pop();
                stack_addr.pop();
                temp = Instruction.createRet(E);
                IR.add(temp);

                stack_addr.push(IRVariable.named(""));
            }

            case 8 -> { // E -> E + A
                IRValue A = stack_addr.pop();
                stack_addr.pop();
                IRValue E = stack_addr.pop();
                IRVariable result = IRVariable.temp();
                temp = Instruction.createAdd(result, E, A);
                IR.add(temp);

                stack_addr.push(result);
            }

            case 9 -> { // E -> E - A
                IRValue A = stack_addr.pop();
                stack_addr.pop();
                IRValue E = stack_addr.pop();
                IRVariable result = IRVariable.temp();
                temp = Instruction.createSub(result, E, A);
                IR.add(temp);

                stack_addr.push(result);
            }

            case 11 -> { // A -> A * B
                IRValue B = stack_addr.pop();
                stack_addr.pop();
                IRValue A = stack_addr.pop();
                IRVariable result = IRVariable.temp();
                temp = Instruction.createMul(result, A, B);
                IR.add(temp);

                stack_addr.push(result);
            }
            case 13 -> { // B -> ( E )
                stack_addr.pop();
                IRValue E = stack_addr.pop();
                stack_addr.pop();
                stack_addr.push(E);
            }
            case 1,5,10,12,14,15 -> {   // 类似 A-> B 的形式, 栈保持不变
            }
            // ...
            default -> { //
                int len_pop = production.body().size();                         // 要弹出语义栈的元素个数
                stack_pop(len_pop);                                             // 弹出
                stack_addr.push(IRVariable.named(""));
            }
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {

    }

    @Override
    public void setSymbolTable(SymbolTable table) {

    }

    public List<Instruction> getIR() {
        // TODO
        return IR;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
    // 为状态栈和符号栈弹出n个元素
    private void stack_pop(int n) {
        for (int i=0;i<n;i++){
            stack_addr.pop();
        }
    }
}

