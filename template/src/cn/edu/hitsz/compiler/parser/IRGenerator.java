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
    private Stack<IRValue> addrStack = new Stack<>();     // 语义栈
//    private SymbolTable symbolTable;
    private ArrayList IR = new ArrayList<Instruction>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        String  cur_tokenKind_name = currentToken.getKind().getIdentifier(); // 获取当前词的类型名
        if (cur_tokenKind_name.equals("id")){
            addrStack.push(IRVariable.named(currentToken.getText()));      // 将变量名入栈
        }
        else if(cur_tokenKind_name.equals("IntConst")) {
            addrStack.push(IRImmediate.of(Integer.parseInt(currentToken.getText())));    // 将常量入栈
        }
        else{
            addrStack.push(IRVariable.named(cur_tokenKind_name));
        }
        return;
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        Instruction temp;       // 存放指令
        switch (production.index()) {

            case 6 -> { // S -> id = E
                IRValue E = addrStack.pop();
                addrStack.pop();
                IRValue id = addrStack.pop();
                temp = Instruction.createMov((IRVariable) id, E);
                IR.add(temp);

                addrStack.push(IRVariable.named(""));
            }

            case 7 -> { // S -> return E
                IRValue E = addrStack.pop();
                addrStack.pop();
                temp = Instruction.createRet(E);
                IR.add(temp);

                addrStack.push(IRVariable.named(""));
            }

            case 8 -> { // E -> E + A
                IRValue A = addrStack.pop();
                addrStack.pop();
                IRValue E = addrStack.pop();
                IRVariable result = IRVariable.temp();
                temp = Instruction.createAdd(result, E, A);
                IR.add(temp);

                addrStack.push(result);
            }

            case 9 -> { // E -> E - A
                IRValue A = addrStack.pop();
                addrStack.pop();
                IRValue E = addrStack.pop();
                IRVariable result = IRVariable.temp();
                temp = Instruction.createSub(result, E, A);
                IR.add(temp);

                addrStack.push(result);
            }

            case 11 -> { // A -> A * B
                IRValue B = addrStack.pop();
                addrStack.pop();
                IRValue A = addrStack.pop();
                IRVariable result = IRVariable.temp();
                temp = Instruction.createMul(result, A, B);
                IR.add(temp);

                addrStack.push(result);
            }
            case 13 -> { // B -> ( E )
                addrStack.pop();
                IRValue E = addrStack.pop();
                addrStack.pop();
                addrStack.push(E);
            }
            case 1,5,10,12,14,15 -> {   // 类似 A-> B 的形式, 栈保持不变
            }
            // ...
            default -> { //
                int len_pop = production.body().size();                         // 要弹出语义栈的元素个数
                stack_pop(len_pop);                                             // 弹出
                addrStack.push(IRVariable.named(""));
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
            addrStack.pop();
        }
    }
}

