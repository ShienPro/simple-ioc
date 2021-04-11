package me.shienpro.entity;

import me.shienpro.annotation.Autowired;
import me.shienpro.annotation.Component;

@Component
public class CircleEntity1 {
    @Autowired
    private CircleEntity2 circleEntity2;

    @Override
    public String toString() {
        return "CircleEntity1{" +
                "circleEntity2=" + circleEntity2 +
                '}';
    }
}
