package me.shienpro.entity;

import me.shienpro.annotation.Autowired;
import me.shienpro.annotation.Component;

@Component
public class RefEntity {
    private CircleEntity1 circleEntity1;
    private CircleEntity2 circleEntity2;
    @Autowired
    private ValueEntity valueEntity;

    @Autowired
    private InterfaceEntity1 interfaceEntity1;

    @Autowired("i21")
    private InterfaceEntity2 interfaceEntity21;
    private InterfaceEntity2 interfaceEntity22;

    @Autowired
    public RefEntity(CircleEntity1 circleEntity1, @Autowired("i22") InterfaceEntity2 interfaceEntity22) {
        this.circleEntity1 = circleEntity1;
        this.interfaceEntity22 = interfaceEntity22;
    }

    public CircleEntity1 getCircleEntity1() {
        return circleEntity1;
    }

    public void setCircleEntity1(CircleEntity1 circleEntity1) {
        this.circleEntity1 = circleEntity1;
    }

    public CircleEntity2 getCircleEntity2() {
        return circleEntity2;
    }

    @Autowired
    public void setCircleEntity2(CircleEntity2 circleEntity2) {
        this.circleEntity2 = circleEntity2;
    }

    public ValueEntity getValueEntity() {
        return valueEntity;
    }

    public void setValueEntity(ValueEntity valueEntity) {
        this.valueEntity = valueEntity;
    }

    public InterfaceEntity1 getInterfaceEntity1() {
        return interfaceEntity1;
    }

    public void setInterfaceEntity1(InterfaceEntity1 interfaceEntity1) {
        this.interfaceEntity1 = interfaceEntity1;
    }

    public InterfaceEntity2 getInterfaceEntity21() {
        return interfaceEntity21;
    }

    public void setInterfaceEntity21(InterfaceEntity2 interfaceEntity21) {
        this.interfaceEntity21 = interfaceEntity21;
    }

    public InterfaceEntity2 getInterfaceEntity22() {
        return interfaceEntity22;
    }

    public void setInterfaceEntity22(InterfaceEntity2 interfaceEntity22) {
        this.interfaceEntity22 = interfaceEntity22;
    }

    @Override
    public String toString() {
        return "RefEntity{" +
                "circleEntity1=" + circleEntity1 +
                ", circleEntity2=" + circleEntity2 +
                ", valueEntity=" + valueEntity +
                ", interfaceEntity1=" + interfaceEntity1 +
                ", interfaceEntity21=" + interfaceEntity21 +
                ", interfaceEntity22=" + interfaceEntity22 +
                '}';
    }
}
