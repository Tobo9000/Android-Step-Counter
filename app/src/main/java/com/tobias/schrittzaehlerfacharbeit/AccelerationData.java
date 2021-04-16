package com.tobias.schrittzaehlerfacharbeit;

/**
 *
 */
public class AccelerationData {

    private double value; // Länge des Vektors (errechnet aus x, y und z)
    private float x;
    private float y;
    private float z;
    private long time;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
