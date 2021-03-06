package cdut.heruoxin.oscilloscope;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import org.eazegraph.lib.charts.ValueLineChart;


public class MainActivity extends ActionBarActivity {

    private OscilloscopeStack oscilloscopeStack;
    private AudioSensor audioSensor;
    private LightSensor lightSensor;
    private MagneticSensor magneticSensor;
    private TemperatureSensor temperatureSensor;
    private AccelerometerSensor accelerometerSensor;
    private Handler mHandler;

    private BaseSensor currentSensor;
    private BaseSensor[] sensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        BaseSensor.Callback mCallback = new BaseSensor.Callback() {
            @Override
            public void onValueChanged(final double decibel) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        oscilloscopeStack.addToStack((float) decibel);
                    }
                });
            }
        };

        ValueLineChart mCubicValueLineChart = (ValueLineChart) findViewById(R.id.cubiclinechart);

        oscilloscopeStack = new OscilloscopeStack(
                mCubicValueLineChart,
                getResources().getColor(R.color.accent),
                40);

        audioSensor = new AudioSensor(16, mCallback);
        lightSensor = new LightSensor(this, 40, mCallback);
        magneticSensor = new MagneticSensor(this, 40, mCallback);
        temperatureSensor = new TemperatureSensor(this, 40, mCallback);
        accelerometerSensor = new AccelerometerSensor(this, 40, mCallback);
        sensors = new BaseSensor[]
                {audioSensor, lightSensor, magneticSensor, temperatureSensor, accelerometerSensor};
        currentSensor = audioSensor;

        initSpinner();

    }

    private void initSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.sensors_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sensors_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                oscilloscopeStack.resetStack();
                if (!currentSensor.isPaused()) {
                    currentSensor.pause();
                    currentSensor = sensors[position];
                    currentSensor.resume();
                } else {
                    currentSensor = sensors[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                onAboutClick();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onAboutClick() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.action_about))
                .setMessage(getString(R.string.summary_about))
                .setPositiveButton(android.R.string.ok, null)
                .create().show();
    }

    public void mFabOnClick(final View view) {
        final boolean isPaused = currentSensor.isPaused();
        if (Build.VERSION.SDK_INT >= 12) {
            view.setRotation(0);
            view.animate().rotation(360).setDuration(600);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((ImageButton) view).setImageResource(
                        isPaused ?
                                R.drawable.ic_action_av_pause :
                                R.drawable.ic_action_av_play
                );
            }
        }, 400);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPaused) currentSensor.resume();
                else currentSensor.pause();
            }
        }, 200);
    }

}
