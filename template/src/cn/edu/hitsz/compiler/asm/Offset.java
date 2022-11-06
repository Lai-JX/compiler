package cn.edu.hitsz.compiler.asm;

/**
 * 类Offset用于表示内存偏移量，从而记录数据在内存中的地址
 */

public class Offset extends Addr{
    public Offset(int addr_id) {
        super(addr_id);
    }
    @Override
    public String toString() {
        return addr_id+"(x0)";
    }
}
