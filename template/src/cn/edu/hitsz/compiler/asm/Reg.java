package cn.edu.hitsz.compiler.asm;

public class Reg extends Addr {
    public Reg(int addr_id) {
        super(addr_id);
    }
    @Override
    public String toString() {
        return "t"+addr_id;
    }
}
