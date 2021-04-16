package com.tobias.schrittzaehlerfacharbeit;

/**
 * Das interface StepListener dient dazu, erkannte Schritte von der StepDetector Klasse
 * zurück zur Activity bzw. der Klasse, in der das Interface implementiert wird, zurück zu "senden".
 * Dazu muss die Klasse, in der das Interface implementiert ist, im StepDetector registriert werden.
 */
public interface StepListener {

    /**
     * Die Methode step soll erkannte Schritte übergeben.
     * @param accelerationData AccelerationData: Ein Datensatz des Beschleunigungssensors, welcher für einen Schritt steht.
     * @param stepType Enum StepType: Eine der drei Schritttypen aus dem Enum StepType.
     */
    void step(AccelerationData accelerationData, StepType stepType);

}
