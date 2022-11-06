package cn.edu.hitsz.compiler.asm;

import java.util.Objects;
/**
 * 此类为抽象类
 * 类Reg（寄存器（t0-t6）编号）和类Offset（内存偏移量) 需继承此类
 */
public abstract class Addr {
    protected final int addr_id;

    protected Addr(int addr_id) {
        this.addr_id = addr_id;
    }

    @Override
    public abstract String toString();

    public int getAddr_id() {
        return addr_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Addr addr = (Addr) o;
        return addr_id == addr.addr_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(addr_id);
    }
}
