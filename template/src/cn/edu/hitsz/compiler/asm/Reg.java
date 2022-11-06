package cn.edu.hitsz.compiler.asm;

/**
 * 类Reg用于表示寄存器，从而记录数据所在的寄存器
 */
public class Reg extends Addr {
    public Reg(int addr_id) {
        super(addr_id);
    }
    @Override
    public String toString() {
        return "t"+addr_id;
    }
}
