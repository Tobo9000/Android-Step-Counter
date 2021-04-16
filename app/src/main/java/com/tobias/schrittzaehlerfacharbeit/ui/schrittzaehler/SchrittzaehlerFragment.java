package com.tobias.schrittzaehlerfacharbeit.ui.schrittzaehler;

import androidx.cardview.widget.CardView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tobias.schrittzaehlerfacharbeit.AccelerationData;
import com.tobias.schrittzaehlerfacharbeit.R;
import com.tobias.schrittzaehlerfacharbeit.StepDetector;
import com.tobias.schrittzaehlerfacharbeit.StepListener;
import com.tobias.schrittzaehlerfacharbeit.StepType;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Das SchrittzaehlerFragment, in dem der Schrittzähler mit der UI verknüpft wird
 */
public class SchrittzaehlerFragment extends Fragment implements SensorEventListener, StepListener {

    private static final String TAG = "PedometerFragment";

    private CardView cardViewToggleStepCounting;
    private TextView textView_amount_steps, textView_type_of_step,
                    textView_pedometer_is_running, textView_pedometer_toggle_text;

    // Ergebnisse - Textviews
    private TextView textview_results_total_steps, textview_results_walking_steps, textview_results_jogging_steps, textview_results_running_steps,
            textview_results_total_distance, textview_results_average_speed, textview_results_average_frequency, textview_results_burned_calories, textview_results_total_moving_time;

    // ViewModel - speichert alle relevanten Daten hier. --> Gehen nicht verloren wenn
    // das Fragment neu erstellt wird (Orientierung ändert sich; App im Hintergrund etc.)
    private SchrittzaehlerViewModel mViewModel;

    /**
     * Returnt eine neue Instanz des SchrittzaehlerFragment's.
     * @return neue Instanz
     */
    public static SchrittzaehlerFragment newInstance() {
        return new SchrittzaehlerFragment();
    }

    /**
     * Wird bei Erstellung der Ansicht / GUI aufgerufen.
     * Returnt die Ansicht
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View - neu generiertes GUI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schrittzaehler_fragment, container, false);

        cardViewToggleStepCounting = view.findViewById(R.id.btn_pedometer_toggle_tracking);
        cardViewToggleStepCounting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mViewModel.isCountingSteps()) stopCounting();
                else startCounting();
            }
        });
        textView_pedometer_toggle_text = view.findViewById(R.id.textview_pedometer_toggle_text);

        textView_amount_steps = view.findViewById(R.id.textview_amount_steps);
        textView_type_of_step = view.findViewById(R.id.textview_pedometer_type_of_step);
        textView_pedometer_is_running = view.findViewById(R.id.textview_pedometer_isRunning);

        textview_results_total_steps = view.findViewById(R.id.textview_results_total_steps);
        textview_results_walking_steps = view.findViewById(R.id.textview_results_walking_steps);
        textview_results_jogging_steps = view.findViewById(R.id.textview_results_jogging_steps);
        textview_results_running_steps = view.findViewById(R.id.textview_results_running_steps);
        textview_results_total_distance = view.findViewById(R.id.textview_results_total_distance);
        textview_results_average_speed = view.findViewById(R.id.textview_results_average_speed);
        textview_results_average_frequency = view.findViewById(R.id.textview_results_average_frequency);
        textview_results_burned_calories = view.findViewById(R.id.textview_results_burned_calories);
        textview_results_total_moving_time = view.findViewById(R.id.textview_results_total_moving_time);

        if(mViewModel.getSensorManager() == null) {
            mViewModel.setSensorManager((SensorManager) getActivity().getSystemService(SENSOR_SERVICE));
        }
        if(mViewModel.getAccelerationSensor() == null){
            if(mViewModel.getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mViewModel.setAccelerationSensor(mViewModel.getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            }
        }
        if(mViewModel.getStepDetector() == null){
            mViewModel.setStepDetector(new StepDetector());
        }
        mViewModel.getStepDetector().registerStepListener(this);

        if(mViewModel.getAccelerationDataArrayList() == null){
            mViewModel.setAccelerationDataArrayList(new ArrayList<AccelerationData>());
        }

        if(mViewModel.isCountingSteps()){
            textView_pedometer_toggle_text.setText(getResources().getText(R.string.disable_pedometer));
            textView_pedometer_is_running.setText(getResources().getText(R.string.pedometer_running));
            textView_pedometer_is_running.setTextColor(getResources().getColor(R.color.green));
            textView_amount_steps.setText(String.valueOf(mViewModel.getAmountOfSteps()));
            mViewModel.getSensorManager().registerListener(this, mViewModel.getAccelerationSensor(), SensorManager.SENSOR_DELAY_NORMAL);
        }
        return view;
    }

    /**
     * Wird bei Erstellung des Fragments aufgerufen. Initialisiert das ViewModel.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(SchrittzaehlerViewModel.class);
        super.onCreate(savedInstanceState);
    }

    /**
     * Wird aufgerufen, bevor das Fragment zerstört wird.
     */
    @Override
    public void onDetach() {
        mViewModel.getSensorManager().unregisterListener(this);
        super.onDetach();
    }

    /**
     * Wird aufgerufen, wenn sich Messwerte eines Sensors ändern.
     * Da nur der Beschleunigungssensor registriert wird, kommen Werte nur von diesem.
     * @param sensorEvent SensorEvent mit allen neuen Messwerten, Zeitstempel und Urpsrung
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        AccelerationData newAccelerationData = new AccelerationData();
        newAccelerationData.setX(sensorEvent.values[0]);
        newAccelerationData.setY(sensorEvent.values[1]);
        newAccelerationData.setZ(sensorEvent.values[2]);
        newAccelerationData.setTime(sensorEvent.timestamp);

        mViewModel.getAccelerationDataArrayList().add(newAccelerationData);
        mViewModel.getStepDetector().addAccelerationData(newAccelerationData);

        // Vorherige Version (jetzt in StepDetector gehandhabt):
        /*
        // bei 200 Millisekunden Delay ca. 5 Sekunden
        if(mViewModel.getAccelerationDataArrayList().size() >= 25){
            sendDataArray();
        }*/
    }

/*
    private void sendDataArray(){
        mViewModel.getStepDetector().handleData(mViewModel.getAccelerationDataArrayList());
        mViewModel.getAccelerationDataArrayList().clear();
    }*/

    /**
     * Wird aufgerufen, wenn sich die Genauigkeit des registrierten Sensors ändert.
     * @param sensor Sensor, von dem sich die Genauigkeit geändert hat.
     * @param i neue Genauigkeit
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Wird aufgerufen, wenn im StepDetector ein Schritt erkannt wurde. Speichert Schritt im ViewModel.
     * @param accelerationData AccelerationData: Ein Datensatz des Beschleunigungssensors, welcher für einen Schritt steht.
     * @param stepType Enum StepType: Eine der drei Schritttypen aus dem Enum StepType.
     */
    @Override
    public void step(AccelerationData accelerationData, StepType stepType) {
        // Step event coming back from StepDetector
        mViewModel.setAmountOfSteps(mViewModel.getAmountOfSteps() + 1);
        textView_amount_steps.setText(String.valueOf(mViewModel.getAmountOfSteps()));
        if(stepType == StepType.WALKING) {
            mViewModel.setWalkingSteps(mViewModel.getWalkingSteps() + 1);
            textView_type_of_step.setText(getResources().getText(R.string.walking));
        }
        else if(stepType == StepType.JOGGING) {
            mViewModel.setJoggingSteps(mViewModel.getJoggingSteps() + 1);
            textView_type_of_step.setText(getResources().getText(R.string.jogging));
        }
        else {
            mViewModel.setRunningSteps(mViewModel.getRunningSteps() + 1);
            textView_type_of_step.setText(getResources().getText(R.string.running));
        }
    }

    /**
     * Berechnet Ergebnisse der letzten Messung. Nur Schätzungen.
     * Werden in der GUI angezeigt.
     */
    private void calculateResults(){
        int totalSteps = mViewModel.getAmountOfSteps();
        textview_results_total_steps.setText(String.valueOf(totalSteps));

        int walkingSteps = mViewModel.getWalkingSteps();
        int joggingSteps = mViewModel.getJoggingSteps();
        int runningSteps = mViewModel.getRunningSteps();

        textview_results_walking_steps.setText(String.valueOf(walkingSteps));
        textview_results_jogging_steps.setText(String.valueOf(joggingSteps));
        textview_results_running_steps.setText(String.valueOf(runningSteps));

        float totalDistance = walkingSteps * 0.5f + joggingSteps * 1.0f + runningSteps * 1.5f;
        String distance = totalDistance + " m";
        textview_results_total_distance.setText(distance);

        float totalDuration = walkingSteps * 1.0f + joggingSteps * 0.75f + runningSteps * 0.5f;
        float hours = totalDuration / 3600;
        float minutes = (totalDuration % 3600) / 60;
        float seconds = totalDuration % 60;
        String duration = String.format(Locale.GERMANY,"%.0f", hours) + "h " +
                        String.format(Locale.GERMANY, "%.0f", minutes) + "min " +
                        String.format(Locale.GERMANY, "%.0f", seconds) + "s";
        textview_results_total_moving_time.setText(duration);

        // Average speed:
        String averageSpeed = String.format(Locale.GERMANY, "%.2f", totalDistance / totalDuration) + " m/s";
        textview_results_average_speed.setText(averageSpeed);

        // Average step frequency
        String averageStepFrequency = String.format(Locale.GERMANY, "%.0f", totalSteps / minutes) + " Schritte/min";
        textview_results_average_frequency.setText(averageStepFrequency);

        // Calories
        float totalCaloriesBurned = walkingSteps + 0.05f + joggingSteps * 0.1f + runningSteps * 0.2f;
        String totalCalories = String.format(Locale.GERMANY, "%.0f", totalCaloriesBurned) + " Kalorien";
        textview_results_burned_calories.setText(totalCalories);
    }

    /**
     * Setzt einige Daten zurück.
     */
    private void resetUI(){
        mViewModel.setAmountOfSteps(0);
        mViewModel.setWalkingSteps(0);
        mViewModel.setJoggingSteps(0);
        mViewModel.setRunningSteps(0);
        textView_amount_steps.setText(String.valueOf(mViewModel.getWalkingSteps()));
    }

    /**
     * Startet Schrittsensor. (Fragment wird im SensorManager registriert)
     */
    private void startCounting(){
        if(!mViewModel.isCountingSteps()){
            try {
                resetUI();
                mViewModel.getSensorManager().registerListener(this, mViewModel.getAccelerationSensor(), SensorManager.SENSOR_DELAY_NORMAL);
                mViewModel.setCountingSteps(true);
                textView_pedometer_toggle_text.setText(getResources().getText(R.string.disable_pedometer));
                textView_pedometer_is_running.setText(getResources().getText(R.string.pedometer_running));
                textView_pedometer_is_running.setTextColor(getResources().getColor(R.color.green));
            } catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * Stoppt Schrittsensor. (Fragment wird im SensorManager unregistriert)
     */
    private void stopCounting(){
        if(mViewModel.isCountingSteps()){
            try {
                // Letzten verbliebenen Daten werden ebenfalls verarbeitet
                //sendDataArray();

                mViewModel.getSensorManager().unregisterListener(this);
                mViewModel.setCountingSteps(false);
                calculateResults();
                textView_pedometer_toggle_text.setText(getResources().getText(R.string.acitvate_pedometer));
                textView_pedometer_is_running.setText(getResources().getText(R.string.pedometer_not_running));
                textView_pedometer_is_running.setTextColor(getResources().getColor(R.color.red));
            } catch (Exception e){
                Log.d(TAG, e.getMessage());
            }
        }
    }

}
