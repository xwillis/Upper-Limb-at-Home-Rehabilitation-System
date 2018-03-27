package com.txbdc.imuoutputjava;

/**
 * Created by dragonfiero on 3/23/18.
 */

public class Status {
    public boolean scanning = false;
    public boolean stimming = false;
    public boolean fireflyCharFound = false;
    public boolean hipCalibrate, ankleCalibrate, kneeCalibrate = false;
    public int hipCalibrateCounter, kneeCalibrateCounter, ankleCalibrateCounter = 0;
    public float averageHip, averageAnkle, averageKnee = 0;
}
