package cn.edu.hitsz.compiler.asm;

public class Offset extends Addr{
    public Offset(int addr_id) {
        super(addr_id);
    }
    @Override
    public String toString() {
        return addr_id+"(x0)";
    }
}
