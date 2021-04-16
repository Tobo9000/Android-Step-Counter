package com.tobias.schrittzaehlerfacharbeit;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Die Klasse StepDetector dient der Ermittlung von Schritten
 * aus Datensätzen des Beschleunigungssensors. Über das StepListener-Interface
 * werden die erkannten Schritte "zurückgegeben".
 */
public class StepDetector {

    private static final int WALKINGTHRESHOLD = 17;
    private static final int JOGGINGTHRESHOLD = 24;
    private static final int RUNNINGTHRESHOLD = 30;

    private StepListener stepListener;

    private ArrayList<AccelerationData> newAccelerationDataList;
    private ArrayList<AccelerationData> calculatedList;

    /**
     * Konstruktor der Klasse.
     * Es werden zwei leere ArrayListen erstellt,
     * welche in den anderen Methoden benötigt werden.
     */
    public StepDetector(){
        newAccelerationDataList = new ArrayList<>();
        calculatedList = new ArrayList<>();
    }


    /**
     * Die Methode registerStepListener registriert das mitgegebene Interface
     * als Attribut in der Klasse. Über dieses werden später erkannte Schritte mitgeteilt.
     * @param pStepListener     Das Interface, welches erkannte Schritte mitgeteilt bekommt.
     */
    public void registerStepListener(StepListener pStepListener){
        stepListener = pStepListener;
    }

    /**
     * Die Methode addAccelerationData nimmt neue Messwerte des Beschleunigungssensors entgegen.
     * Wenn 25 Datensätze vorhanden sind, werden sie verarbeitet und es werden erneut 25 Datensätze gesammelt.
     * @param pNewAccelerationData
     */
    public void addAccelerationData(AccelerationData pNewAccelerationData){
        newAccelerationDataList.add(pNewAccelerationData);

        if(newAccelerationDataList.size() >= 25){
            handleAccelerationData();
        }
    }

    /**
     * Die Methode handleAccelerationData erkennt Schritte in Beschleunigungsdaten.
     * Dazu werden auch die vier Methoden calculateValueAndTime, findHighPoints, removeNearHighPoints, und examineStepTypeAndSendResponse
     * genutzt. Für jeden Datensatz wird außerdem die Vektorlänge (= Geschwindigkeit zu bestimmten Zeitpunkt) berechnet und
     * das time Attribut des Datensatzes wird von Nanosekunden seit Gerätestartzeit in Unixzeit (Millisekunden) geändert.
     * Nach Abarbeitung aller Daten werden die erkannten Schritte über das Interface ausgegeben und
     * die ArrayListen wieder geleert, damit sie erneut verwendet werden können.
     */
    private void handleAccelerationData(){

        for (int i = 0; i < newAccelerationDataList.size(); i++) {
            AccelerationData accelerationData = newAccelerationDataList.get(i);
            accelerationData = calculateValueAndTime(accelerationData);
            calculatedList.add(accelerationData);
        }

        ArrayList<AccelerationData> highPointList = findHighPoints();
        highPointList = removeNearHighPoints(highPointList);
        examineStepTypeAndSendResponse(highPointList);

        calculatedList.clear();
        newAccelerationDataList.clear();
    }

    /**
     * Die Methode calculateValueAndTime berechnet für pAccelerationData die Vektorlänge und den Unix-Timestamp.
     * Die entsprechenden Werte werden in pAccelerationData geändert. Anschließend wird pAccelerationData returnt.
     * @param pAccelerationData Objekt, von welchem die Vektorlänge und der Unix-Timestamp berechnet werden.
     * @return  AccelerationData: Das Objekt, mit geänderten Werten.
     */
    private AccelerationData calculateValueAndTime(AccelerationData pAccelerationData){

        float x = pAccelerationData.getX();
        float y = pAccelerationData.getY();
        float z = pAccelerationData.getZ();

        double vectorLength = Math.sqrt(x * x + y * y + z * z);
        pAccelerationData.setValue(vectorLength);

        long time = pAccelerationData.getTime();
        long timeOffsetToUnix = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        long unixTimestamp = (time / 1000000L) + timeOffsetToUnix;
        pAccelerationData.setTime(unixTimestamp);

        return pAccelerationData;
    }

    /**
     * Die Methode findHighPoints findet die Beschleunigungsdatensätze aus rawAccelerationData
     * heraus, deren Gesamtbeschleunigung höher als der Wert von WALKINGTHRESHOLD (17) ist. Diese werden
     * einer weiteren ArrayList hinzugefügt, welche returnt wird.
     * @return  ArrayList: Eine Liste, mit den höchsten Hochpunkten.
     */
    private ArrayList<AccelerationData> findHighPoints(){
        ArrayList<AccelerationData> highPointList = new ArrayList<>();
        ArrayList<AccelerationData> aboveWalkingThresholdList = new ArrayList<>();
        boolean wasAboveThreshold = true;
        for (int i = 0; i < calculatedList.size(); i++) {

            AccelerationData calculatedDataSet = calculatedList.get(i);
            if(calculatedDataSet.getValue() > WALKINGTHRESHOLD){
                aboveWalkingThresholdList.add(calculatedDataSet);
                wasAboveThreshold = true;
            } else {
                    // erst, wenn es einen Wert unter WALKINGTHRESHOLD gibt
                if(wasAboveThreshold && aboveWalkingThresholdList.size() > 0){
                    Collections.sort(aboveWalkingThresholdList, new AccelerationDataSorter());
                    highPointList.add(aboveWalkingThresholdList.get(aboveWalkingThresholdList.size() - 1));
                    aboveWalkingThresholdList.clear();
                }
                wasAboveThreshold = false;
            }
        }
        return highPointList;
    }


    /**
     * Die Methode removeNearHighPoints geht die ArrayList pAccelerationData mit den höchsten Hochpunkten
     * durch und überprüft, ob innerhalb von 400 Millisekunden ein weiterer "höchster Gipfel" vorhanden ist.
     * Falls dies der Fall ist, wird der kleinere von beiden aus der Liste entfernt.
     * @param pAccelerationDataList Die Liste mit den Hochpunkten als ArrayList
     * @return  Eine ArrayList, mit den entfernten Hochpunkten innerhalb von 400 Millisekunden
     */
    private ArrayList<AccelerationData> removeNearHighPoints(ArrayList<AccelerationData> pAccelerationDataList){
        ArrayList<Integer> wrongHighPointIndexes = new ArrayList<>();
        for (int i = 0; i < pAccelerationDataList.size() - 1; i++) {
            if((pAccelerationDataList.get(i + 1).getTime() - pAccelerationDataList.get(i).getTime()) < 400){
                if(pAccelerationDataList.get(i + 1).getValue() < pAccelerationDataList.get(i).getValue()){
                    wrongHighPointIndexes.add(i + 1);
                } else {
                    wrongHighPointIndexes.add(i);
                }
            }
        }
        for (int i = wrongHighPointIndexes.size() - 1; i >= 0; i--) {
            System.out.println(i);
            pAccelerationDataList.remove(i);
        }
        return pAccelerationDataList;
    }

    /**
     *
     * Die Methode examineStepTypeAndSendResponse überprüft die Gesamtbeschleunigung der höchsten Gipfel aus
     * pAccelerationData und sendet über das registrierte Interface stepListener alle erkannten Schritte.
     * Wenn die Gesamtbeschleunigung größer als RUNNINGPEAK ist, wird der Schritttyp RUNNING ausgegeben,
     * wenn die Gesamtbeschleunigung größer als JOGGINGPEAK ist, wird der Schritttyp JOGGING ausgegeben,
     * andernfalls der Schritttype WALKING.
     * @param pAccelerationDataList Eine ArrayList, mit den höchsten Hochpunkten
     */
    private void examineStepTypeAndSendResponse(ArrayList<AccelerationData> pAccelerationDataList){
        for (int i = 0; i < pAccelerationDataList.size(); i++) {
            AccelerationData highPoint = pAccelerationDataList.get(i);
            if(highPoint.getValue() > RUNNINGTHRESHOLD){
                stepListener.step(highPoint, StepType.RUNNING);
            } else if(highPoint.getValue() > JOGGINGTHRESHOLD){
                stepListener.step(highPoint, StepType.JOGGING);
            } else {
                stepListener.step(highPoint, StepType.WALKING);
            }
        }
    }

    /**
     * Die Klasse DataSorter ist eine Comparator-Klasse für Arraylisten mit AccelerationData.
     * Es wird nach der Größe des Gesamtbeschleunigungswertes aufwärts sortiert.
     */
    public class AccelerationDataSorter implements Comparator<AccelerationData> {
        /**
         * Die Methode compare vergleicht zwei Datensätze von AccelerationData anhand der
         * Gesamtbeschleunigung "value". Wenn die Beschleunigung des ersten Datensatzes größer ist,
         * wird 1 returnt. Wenn die Beschleunigung des zweiten Datensatzes größer ist, wird -1 returnt.
         * Ansonsten (wenn die Beschlunigung gleich ist) wird 0 returnt.
         * @param data1 AccelerationData: Das erste Vergleichsobjekt
         * @param data2 AccelerationData: Das zweite Vergleichsobjekt
         * @return int 0, 1 oder -1; je nachdem, welche Beschleunigung größer ist.
         */
        @Override
        public int compare(AccelerationData data1, AccelerationData data2) {
            int returnValue = 0;
            if(data1.getValue() < data2.getValue()){
                returnValue = -1;
            } else if(data1.getValue() > data2.getValue()){
                returnValue = 1;
            }
            return returnValue;
        }
    }

}


