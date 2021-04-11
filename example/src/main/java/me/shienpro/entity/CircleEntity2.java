package me.shienpro.entity;

import me.shienpro.annotation.Autowired;
import me.shienpro.annotation.Component;

@Component
public class CircleEntity2 {
    @Autowired
    private CircleEntity1 circleEntity1;

    @Override
    public String toString() {
        return "CircleEntity2{" +
                "circleEntity1=" + (circleEntity1 != null ? circleEntity1.hashCode() : "null") +
                '}';
    }
}
