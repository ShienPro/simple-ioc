package me.shienpro.entity;

import me.shienpro.annotation.Autowired;
import me.shienpro.annotation.Component;
import me.shienpro.annotation.Value;

@Component
public class ValueEntity {
    private int i1;
    @Value("2")
    private int i2;
    private int i3;
    private Integer i11;
    @Value("12")
    private Integer i12;
    private Integer i13;

    @Autowired
    public ValueEntity(@Value("1") int i1, @Value("11") Integer i11) {
        this.i1 = i1;
        this.i11 = i11;
    }

    public int getI1() {
        return i1;
    }

    public void setI1(int i1) {
        this.i1 = i1;
    }

    public int getI2() {
        return i2;
    }

    public void setI2(int i2) {
        this.i2 = i2;
    }

    public int getI3() {
        return i3;
    }

    @Value("3")
    public void setI3(int i3) {
        this.i3 = i3;
    }

    public Integer getI11() {
        return i11;
    }

    public void setI11(Integer i11) {
        this.i11 = i11;
    }

    public Integer getI12() {
        return i12;
    }

    public void setI12(Integer i12) {
        this.i12 = i12;
    }

    public Integer getI13() {
        return i13;
    }

    @Value("13")
    public void setI13(Integer i13) {
        this.i13 = i13;
    }

    @Override
    public String toString() {
        return "ValueEntity{" +
                "i1=" + i1 +
                ", i2=" + i2 +
                ", i3=" + i3 +
                ", i11=" + i11 +
                ", i12=" + i12 +
                ", i13=" + i13 +
                '}';
    }
}
