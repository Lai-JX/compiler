package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.*;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

//TODO: 实验二: 实现 LR 语法分析驱动程序

/**
 * LR 语法分析驱动程序
 * <br>
 * 该程序接受词法单元串与 LR 分析表 (action 和 goto 表), 按表对词法单元流进行分析, 执行对应动作, 并在执行动作时通知各注册的观察者.
 * <br>
 * 你应当按照被挖空的方法的文档实现对应方法, 你可以随意为该类添加你需要的私有成员对象, 但不应该再为此类添加公有接口, 也不应该改动未被挖空的方法,
 * 除非你已经同助教充分沟通, 并能证明你的修改的合理性, 且令助教确定可能被改动的评测方法. 随意修改该类的其它部分有可能导致自动评测出错而被扣分.
 */
public class SyntaxAnalyzer {
    private final SymbolTable symbolTable;
    private final List<ActionObserver> observers = new ArrayList<>();
    private List<Token> tokens = new ArrayList<>();                 // 词法单元集合
    private LRTable lrTable;                 // 词法单元集合
    private Stack<Status> stack_state = new Stack<>();      // 状态栈
    private Stack<Term> stack_symbol = new Stack<>();     // 符号栈


    public SyntaxAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * 注册新的观察者
     *
     * @param observer 观察者
     */
    public void registerObserver(ActionObserver observer) {
        observers.add(observer);
        observer.setSymbolTable(symbolTable);
    }

    /**
     * 在执行 shift 动作时通知各个观察者
     *
     * @param currentStatus 当前状态
     * @param currentToken  当前词法单元
     */
    public void callWhenInShift(Status currentStatus, Token currentToken) {
        for (final var listener : observers) {
            listener.whenShift(currentStatus, currentToken);
        }
    }

    /**
     * 在执行 reduce 动作时通知各个观察者
     *
     * @param currentStatus 当前状态
     * @param production    待规约的产生式
     */
    public void callWhenInReduce(Status currentStatus, Production production) {
        for (final var listener : observers) {
            listener.whenReduce(currentStatus, production);
        }
    }

    /**
     * 在执行 accept 动作时通知各个观察者
     *
     * @param currentStatus 当前状态
     */
    public void callWhenInAccept(Status currentStatus) {
        for (final var listener : observers) {
            listener.whenAccept(currentStatus);
        }
    }

    public void loadTokens(Iterable<Token> tokens) {
        // TODO: 加载词法单元
        // 你可以自行选择要如何存储词法单元, 譬如使用迭代器, 或是栈, 或是干脆使用一个 list 全存起来
        // 需要注意的是, 在实现驱动程序的过程中, 你会需要面对只读取一个 token 而不能消耗它的情况,
        // 在自行设计的时候请加以考虑此种情况

        this.tokens = (ArrayList<Token>) tokens;
        return;
//        System.out.println(this.tokens.get(0));
//        throw new NotImplementedException();
    }

    public void loadLRTable(LRTable table) {
        // TODO: 加载 LR 分析表
        // 你可以自行选择要如何使用该表格:
        // 是直接对 LRTable 调用 getAction/getGoto, 抑或是直接将 initStatus 存起来使用
        this.lrTable = table;
        return;
//        throw new NotImplementedException();
    }

    public void run() {
        // TODO: 实现驱动程序
        // 你需要根据上面的输入来实现 LR 语法分析的驱动程序
        // 请分别在遇到 Shift, Reduce, Accept 的时候调用上面的 callWhenInShift, callWhenInReduce, callWhenInAccept
        // 否则用于为实验二打分的产生式输出可能不会正常工作
        boolean flag = true;
        int i = 0;
        int len_pop = 0;
        Action action;
        Token current_read = tokens.get(i);;

        // 初始化 将初始状态和$符号入栈；
        stack_state.push(lrTable.getInit());
        stack_symbol.push(new NonTerminal("$"));

        while (flag){
            // 根据当前状态与规约到非终结符获得应转移到的状态
            action = lrTable.getAction(stack_state.peek(),current_read);
            switch (action.getKind()) {
                case Shift -> {     // 移入
                    final var shiftTo = action.getStatus();
                    callWhenInShift(stack_state.peek(),current_read);       // 传入当前状态和词
                    stack_state.push(shiftTo);                              // 状态入栈
                    stack_symbol.push(current_read.getKind());              // 符号入栈
                    current_read = tokens.get(++i);                         // 读取符号串中的下一字符

                    // ...
                }

                case Reduce -> {    // 规约
                    final var production = action.getProduction();      // 获取产生式
//                    System.out.println(production.toString());
                    callWhenInReduce(stack_state.peek(),production);
                    len_pop = production.body().size();                         // 要弹出栈的符号和状态个数
                    stack_pop(len_pop);                                         // 弹出符号和状态

                    stack_symbol.push(production.head());                       // 产生式左部入栈
                    stack_state.push(lrTable.getGoto(stack_state.peek(),(NonTerminal) stack_symbol.peek())); // 根据goto表将状态入栈
//                    System.out.println(stack_state.peek().index());

                    // ...
                }

                case Accept -> {
                    callWhenInAccept(stack_state.peek());
                    flag = false;
                }

                case Error -> {
                    throw new RuntimeException("编译出错");
                }
            }

        }
        return;
//        throw new NotImplementedException();
    }

    // 为状态栈和符号栈弹出n个元素
    private void stack_pop(int n) {

        for (int i=0;i<n;i++){
            stack_state.pop();
            stack_symbol.pop();
        }
    }

}
