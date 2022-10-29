package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.*;
import cn.edu.hitsz.compiler.utils.FilePathConfig;
import cn.edu.hitsz.compiler.utils.FileUtils;

import javax.swing.plaf.multi.MultiLabelUI;
import java.util.*;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {
    private ArrayList<Instruction> IR = new ArrayList<Instruction>();
    private ArrayList asm_list = new ArrayList<String>();               // 汇编指令
    private BMap<Reg, IRVariable> Reg_descripe = new BMap<>();          // 寄存器描述符
    private BMap<IRVariable, List<Addr>> Addr_descripe = new BMap<>();  // 地址描述符
    private ArrayList IRVar = new ArrayList<IRVariable>();              // 所有指令的变量，用于判断某一变量之后是否还会用到
    private int var_id = 0;                                             // 变量id，用于判断某一变量之后是否还会用到
    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        for(Instruction instruction : originInstructions){
            // 对每条指令进行预处理
            switch (instruction.getKind()) {
                case ADD -> {
                    if(instruction.getLHS().isImmediate()){         // 左操作数是立即数时调整
                        if (instruction.getRHS().isImmediate()){    // 左、右操作数都是立即数时改用mov指令
                            IR.add(Instruction.createMov(instruction.getResult(),IRImmediate.of(eval(instruction.getLHS())+eval(instruction.getRHS()))));
                        }else {
                            Instruction temp = Instruction.createAdd(instruction.getResult(), instruction.getRHS(), instruction.getLHS());
                            IR.add(temp);
                        }
                    }
                    else {
                        IR.add(instruction);
                    }
                }
                case SUB -> {
                    if(instruction.getLHS().isImmediate()){         // 左操作数是立即数时调整
                        if (instruction.getRHS().isImmediate()){    // 左、右操作数是立即数时改用mov指令
                            IR.add(Instruction.createMov(instruction.getResult(),IRImmediate.of(eval(instruction.getLHS())-eval(instruction.getRHS()))));
                        }else {
                            IRVariable a = IRVariable.temp();
                            Instruction temp1 = Instruction.createMov(a, instruction.getLHS());     // 增加mov指令
                            Instruction temp2 = Instruction.createSub(instruction.getResult(), a, instruction.getRHS());
                            IR.add(temp1);
                            IR.add(temp2);
                        }
                    }
                    else {
                        IR.add(instruction);
                    }
                }
                case MUL -> {
                    if(instruction.getLHS().isImmediate()){         // 左操作数是立即数时调整
                        if (instruction.getRHS().isImmediate()){    // 左、右操作数都是立即数时改用mov指令
                            IR.add(Instruction.createMov(instruction.getResult(),IRImmediate.of(eval(instruction.getLHS())*eval(instruction.getRHS()))));
                        }else {
                            IRVariable a = IRVariable.temp();
                            Instruction temp1 = Instruction.createMov(a,instruction.getLHS());      // 增加mov指令
                            Instruction temp2 = Instruction.createMul(instruction.getResult(),a,instruction.getRHS());
                            IR.add(temp1);
                            IR.add(temp2);
                        }
                    }else if(instruction.getRHS().isImmediate()){ // 右操作数是立即数时调整
                        IRVariable a = IRVariable.temp();
                        Instruction temp1 = Instruction.createMov(a,instruction.getRHS());          // 增加mov指令
                        Instruction temp2 = Instruction.createMul(instruction.getResult(),instruction.getLHS(),a);
                        IR.add(temp1);
                        IR.add(temp2);
                    }
                    else {
                        IR.add(instruction);
                    }
                }
                case RET -> {
                    IR.add(instruction);
                    break;
                }
                default -> {
                    IR.add(instruction);
                }
            }
        }
        System.out.println(IR);

        // 初始化寄存器描述符
        for (int i=0; i<7; i++){
            Reg_descripe.add(new Reg(i), IRVariable.named(""+(i*-1)));
        }

        // 获取的所有变量,添加地址描述符
        for(Instruction instruction : IR){
            // 添加结果变量
            if(!instruction.getKind().equals(InstructionKind.RET)){
                IRVar.add(instruction.getResult());
                Addr_descripe.add(instruction.getResult(),new ArrayList<Addr>());
            }
            // 添加操作数变量
            List<IRValue> ops = instruction.getALlOperands();   // 获取所有操作数
            for (IRValue var : ops){
                if(var.isIRVariable()){
                    IRVar.add(var);
                    Addr_descripe.add((IRVariable) var,new ArrayList<Addr>());
                }
            }

        }

        System.out.println(Reg_descripe);
        System.out.println(IRVar);

    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
        String asm_temp=null;
        asm_list.add(".text\n");
        // 对每条中间代码指令添加汇编代码
        for (Instruction instruction : IR){
            Reg res=null;                                            // 保存运算结果的寄存器
            if (!instruction.getKind().equals(InstructionKind.RET)){    // 除了RET指令，本实验其余指令均有运算结果
                res = getReg(instruction.getResult());
            }
            switch (instruction.getKind()) {
                case ADD -> {
                    // 获取左操作数寄存器
                    Reg reg_l = getReg((IRVariable)instruction.getLHS());

                    // 根据第二个操作数是立即数还是变量，判断使用addi还是add
                    if (instruction.getRHS().isImmediate()){        // addi
                        asm_temp = generate_asm("addi", res.toString(),reg_l.toString(),instruction.getRHS().toString());
                        var_id +=2;     // 已访问变量数加2
                    }else {                                         // add
                        Reg reg_r = getReg((IRVariable)instruction.getRHS());
                        asm_temp = generate_asm("add", res.toString(), reg_l.toString(), reg_r.toString());
                        var_id +=3;
                    }
                }
                case SUB,MUL -> {
                    Reg reg_l = getReg((IRVariable)instruction.getLHS());
                    Reg reg_r = getReg((IRVariable)instruction.getRHS());
                    String op = "sub";
                    if (instruction.getKind().equals(InstructionKind.MUL)){
                        op = "mul";
                    }
                    asm_temp = generate_asm(op, res.toString(), reg_l.toString(), reg_r.toString());
                    var_id +=3;
                }
                case MOV -> {
                    if (instruction.getFrom().isImmediate()){
                        asm_temp = generate_asm("li", res.toString(), instruction.getFrom().toString());
                        var_id +=1;
                    }
                    else {
                        Reg reg_l = getReg((IRVariable)instruction.getFrom());
                        asm_temp = generate_asm("mv", res.toString(), reg_l.toString());
                        var_id +=2;
                    }
                }
                case RET -> {
                    if (instruction.getReturnValue().isImmediate()){
                        asm_temp = "\tli a0, " + instruction.getReturnValue();
                    }
                    else {
                        asm_temp = "\tmv a0, " + getReg((IRVariable) instruction.getReturnValue());
                    }
                }
                default -> {            // MOV
                    IR.add(instruction);
                }
            }
            asm_list.add(asm_temp += "\t#" + instruction + "\n");
        }
        System.out.println(asm_list);
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        FileUtils.writeLines(path, asm_list.stream().toList());
    }
    // 获取立即数的值
    public Integer eval(IRValue value) {
        if (value instanceof IRImmediate immediate) {
            return immediate.getValue();
        } else {
            throw new RuntimeException("NOT IRImmediate type");
        }
    }

    // 为指定变量分配寄存器
    private Reg getReg(IRVariable var){
        // 变量已在寄存器中
        if(Reg_descripe.containsValue(var)){
            return Reg_descripe.getByValue(var);
        }
        // 寻找空闲寄存器
        for (int i=0; i<7; i++){
            IRVariable empty_reg_var = IRVariable.named(""+i*-1);
            if(Reg_descripe.containsValue(empty_reg_var)){          // 有空闲寄存器
                Reg reg = Reg_descripe.getByValue(empty_reg_var);   // 获取空闲寄存器
                Reg_descripe.replace(reg,var);                      // 更新寄存器描述符
                Addr_descripe.getByKey(var).add(reg);               // 更新地址描述符
                return reg;
            }
        }
        // 寻找一个之后不会被使用的变量，将其所占寄存器替换为当前变量
        for (Object temp_reg : Reg_descripe.getAllkey().toArray()){
            Reg reg = (Reg)temp_reg;
            IRVariable temp_var = Reg_descripe.getByKey(reg);
            if(finish(temp_var)){       // 变量后续不会被使用
                Reg_descripe.replace(reg,var);
                Addr_descripe.getByKey(temp_var).remove(reg);
                Addr_descripe.getByKey(var).add(reg);
                return reg;
            }
        }
        // 上述方法不管用，说明需要将某些变量暂存到内存中

        return null;


    }
    // 将各参数组合成汇编语句
    private String generate_asm(String... args){
        String res = "\t" + args[0];
        int n = args.length-1;
        for (int i=1; i<n; i++){
            res += " " + args[i] + ",";
        }
        res += " " + args[n];
        return res;
    }

    // 判断某一变量之后是否还会被使用
    private boolean finish(IRVariable var){
        System.out.println("temp_var:"+var);
        for (int i=var_id+1; i<IRVar.size(); i++){
            System.out.println("after_var:"+IRVar.get(i));
            if (var.equals(IRVar.get(i))){
                return false;       // 后续被使用
            }
        }
        return true;                // 后续不会被使用
    }
}

