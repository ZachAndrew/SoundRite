package andrew.zach.soundrite;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import net.sf.javaml.distance.fastdtw.dtw.DTW;
import net.sf.javaml.distance.fastdtw.dtw.TimeWarpInfo;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeries;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeriesPoint;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

//*Addon #1 imports*

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //*Addon code*
    boolean isButtonClicked = false;
    boolean pauseButtonClicked = false;
    boolean recordButtonClicked = false;
    boolean isRecordFinished = false;

    Context context = this;
    MediaPlayer[] mp;
    private RelativeLayout graph;
    private LineChart chart;
    private TextView lilytalk;
    private TextView score;
    private TextView LevelText;
    private int level;
    private int TotalScore = 0;
    List<Double> recordedPart = new ArrayList<>();
    TimeSeries FFTsong = new TimeSeries(1);
    TimeSeries FFTrecord = new TimeSeries(1);


    List<Double> pitches = new ArrayList<>();
    List<Double> reocrdedPart = new ArrayList<>();

    Runnable runGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TextViews

        //final TextView feedbackbox = (TextView) findViewById(R.id.textView_feebackbox);

        //*Addon #2*
        final TextView lilytalk = (TextView) findViewById(R.id.lilytalk);
        final TextView score = (TextView) findViewById(R.id.score);
        final TextView LevelText = (TextView) findViewById(R.id.levelText);

        //Buttons
        final Button listenButton = (Button) findViewById(R.id.button_listen);//listen button
        final Button playbutton=(Button)findViewById(R.id.button_playback);//playback button
        //Switches
        final Switch listenSwitch = (Switch) findViewById(R.id.switch_listen);//listen switch


        //feedbackbox.setText("Feedback:\n    -#Note:\n    -#Pitch:\n    -#Score:");

      /*  ****EMAIL FUNCTION****(EXTRA)
      FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //*Addon #2*
        //Remembering the goal
        // $$$EXTRA$$$?  String opening1 = getResources().getString(R.string.lilyOpen);
        // $$$EXTRA$$$?  String opening2 = getResources().getString(R.string.lilyOpen2);
        // $$$EXTRA$$$? String opening = opening1 +"\n"+opening2;
        // $$$EXTRA$$$? LilySay(opening);

        //Preparing Song FFT-midi values
         String[] query = new String[50];
         query[0] = getResources().getString(R.string.songval1);
         query[1] = getResources().getString(R.string.songval2);
         query[2] = getResources().getString(R.string.songval3);

        //Adding song FFT-midi values into TimeSeries
        SongTimeSeriesEdit(query[level-1]);

        //Creating media player
        mp = new MediaPlayer[50];
        mp[0] = MediaPlayer.create(context, R.raw.sample);
        mp[1] = MediaPlayer.create(context, R.raw.sample2);
        mp[2] = MediaPlayer.create(context, R.raw.sample3);

        //Creating graph
        // $$$EXTRA$$$? graph = (RelativeLayout)findViewById(R.id.graphlayout);
        chart = new LineChart(this);

        //Get Screen Sizes
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int ht = displaymetrics.heightPixels;
        int wt = displaymetrics.widthPixels;

        //adding graph to view
// #3        graph.addView(chart, wt,240);

        //graph initialization
        InitiliazeGraph();

        //Graph Components
        GraphComponentInitiliazer();

        // $$$EXTRA$$$ actiontext.setText("playing "+pauseButtonClicked);
        //Play Song
        playbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseButtonClicked = !pauseButtonClicked;
                if (pauseButtonClicked) {
                    Toast.makeText(getApplicationContext(), "playClicked= "+pauseButtonClicked+" "+mp[level-1].isPlaying(), Toast.LENGTH_LONG).show();
                    try {
                        if (mp[level-1].isPlaying()) {
                            //  Log.d("***********mp"," "+mp[level-1].isPlaying());
                            mp[level-1].stop();
                            mp[level-1].release();
                            // $$$EXTRA$$$ actiontext.setText(R.string.empty);
                            //    Toast.makeText(getApplicationContext(), "empty= "+pauseButtonClicked, Toast.LENGTH_LONG).show();
                        }
                        mp[level-1].start();
                        // $$$EXTRA$$$ playbutton.setImageResource(R.drawable.pause);
                        // actiontext.setText("playing "+pauseButtonClicked);
                        //actiontext.setText(R.string.playing);
                        if(!mp[level-1].isPlaying()){
                            // $$$EXTRA$$$   actiontext.setText("...");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try{
                        mp[level-1].pause();
                        // $$$EXTRA$$$ playbutton.setImageResource(R.drawable.play);
                        Toast.makeText(getApplicationContext(), "set pauseButton"+pauseButtonClicked, Toast.LENGTH_LONG).show();
                        // actiontext.setText("Paused...");
                        // actiontext.setText("Paused "+pauseButtonClicked);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });


        //*Addon #2*
        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!listenSwitch.isChecked()) {
                    // listenSwitch.setBackgroundColor(getColor(R.color.colorGreen));
                    listenSwitch.setText("LIVE");
                    listenSwitch.setTextColor(getColor(R.color.colorRed));
                } else if (listenSwitch.isChecked()) {
                    listenSwitch.setText("OFF");
                    listenSwitch.setTextColor(getColor(R.color.colorBlack));
                    listenSwitch.setBackgroundColor(getColor(R.color.colorGrey));
                }
                listenSwitch.toggle(); //toggle switch when listenButton is pressed

                if (listenButton.getText().equals("LISTEN")) {
                    listenButton.setText("STOP");
                } else {
                    listenButton.setText("LISTEN");
                }
                //*Addon #1*
                recordButtonClicked = !recordButtonClicked;
                final Thread recordThread;
                if (recordButtonClicked) {
                    //actiontext.setText("recording"+recordButtonClicked);

                    try {
                        //recordbutton.setImageResource(R.drawable.stop);
                        //record button setImage...etc
                        final AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
                        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {
                            @Override
                            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                                final float pitchInHz = pitchDetectionResult.getPitch();
                                runOnUiThread((runGraph = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isRecordFinished) {
                                            // AddGraphData(pitchInHz);
                                            double tmp = pitchInHz;
                                            if (tmp != -1) {
                                                pitches.add(tmp);
                                            }
                                        }
                                    }
                                }));
                            }
                        }));
                        recordThread = new Thread(dispatcher, "Audio Dispatcher");
                        recordThread.start();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // listenButton.setImageRes...etc
                    recordButtonClicked = false;
                    // actiontext.setText(" .... recording etc.....");
                    Toast.makeText(MainActivity.this,"Recording",Toast.LENGTH_LONG).show();
                    isRecordFinished = true;
                    recordPart=pitches;
                   // System.out.println(pitches.isEmpty());
                    Log.i("Record","pitches"+pitches.isEmpty());
                    // recordPart
                }
            }

        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intent = new Intent(MainActivity.this, CurrentScore.class);
            startActivity(intent);

        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(MainActivity.this, ScoreHistory.class);
            startActivity(intent);


        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(MainActivity.this, AudioRecordings.class);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

        //*Addon #2*

        //Real Time Pitch Detection
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // Midi to Hertz Converter
    public double CalculateHertz(int midi) {
        double f = 440 * Math.pow(2, (((double) midi - 69)) / 12);
        return f;
    }

    // Hertz to Midi Converter
    public List<Double> CalculateMidiValues(List<Double> HertzValues) {
        List<Double> MidiValues = new ArrayList<Double>();
        for (int i = 0; i < HertzValues.size(); i++) {
            MidiValues.add((double) Math.round(69 + 12 * log2(HertzValues.get(i) / 440)));
        }
        return MidiValues;
    }

    //For Converter Operations to use log2 in android
    public double log2(double n) {
        return Math.log(n) / Math.log(2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecordFinished) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //This part for graph. I used android-ml-chart library
    public void InitiliazeGraph() {
        chart.setDescription(" ");
        chart.setNoDataText("...");
        chart.setHighlightPerTapEnabled(true);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.enableScroll();
        chart.setPinchZoom(true);
        chart.setBackgroundColor(Color.GRAY);
    }

    //This is also for graph
    public void GraphComponentInitiliazer() {
        LineData lineData = new LineData();
        lineData.setValueTextColor(Color.GRAY);
        chart.setData(lineData);

        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.GRAY);

        XAxis x = chart.getXAxis();
        x.setTextColor(ColorTemplate.getHoloBlue());
        x.setDrawGridLines(false);
        x.setAvoidFirstLastClipping(false);

        YAxis y = chart.getAxisLeft();
        y.setTextColor(ColorTemplate.getHoloBlue());
        y.setDrawGridLines(false);

        YAxis y2 = chart.getAxisRight();
        y2.setEnabled(false);
    }

    //Putting TimeSeries to Evaulate the score
    public void Calculate(TimeSeries ts1, TimeSeries ts2) {
        TimeWarpInfo warpInfo = DTW.getWarpInfoBetween(ts1, ts1);
        //LilySay(warpInfo.getDistance()+"");
//        if(warpInfo.getDistance() < 1500){
//            LilySay("Good Job! We earned 50 points!!");
//            AddScore(50);
//        } else {
//            LilySay("Lets Try Again, we earned 10 points for trying");
//            AddScore(10);
//        }
    }

    //Take the prepared song FFT-Midi values and convert this into TimeSerie
    public void SongTimeSeriesEdit(String query) {
        String[] values = query.split(",");
        List<Double> Dvals = new ArrayList<>();
        for (int j = 0; j < values.length; j++) {
            Dvals.add(Double.parseDouble(values[j]));
        }
        for (int i = 0; i < Dvals.size(); i++) {
            double[] tmp = {Dvals.get(i)};
            TimeSeriesPoint ts = new TimeSeriesPoint(tmp);
            try {
                FFTsong.addLast(i, ts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Take pitch values from Real Time Calculation and convert this into TimeSerie
    public void RecordEditor(List<Double> record) {
        int i;
        for (i = 0; i < record.size(); i++) {
            double[] tmp = {record.get(i)};
            TimeSeriesPoint ts = new TimeSeriesPoint(tmp);
            try {
                FFTrecord.addLast(i, ts);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("************************************RecordEditor i=" + i);
    }

    // String Parser  -----This is for using CSV Files in future
    public String[] StringParse(String query) {
        String[] values;
        values = query.split(",");
        return values;
    }

    //Method for CSV writing
    private void CSVWriter(String[] values) throws IOException {
        String csv = context.getFilesDir().getAbsolutePath() + ".csv";
        //String csv = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(csv));
        List<String[]> data = new ArrayList<String[]>();
        for (int j = 0; j < values.length; j++) {
            String[] tmp = {values[j]};
            data.add(tmp);
        }
        for (int i = 0; i < values.length; i++) {
            writer.writeNext(data.get(i));
        }
        writer.close();

    }

    //Method for CSVReading
    private void CSVReader() throws IOException {
        String path = context.getFilesDir().getAbsolutePath() + ".csv";
        CSVReader csvReader = new CSVReader(new FileReader(path));
        List<String[]> fromcsv = csvReader.readAll();
    }

    //Method for Changing Text
    public void LilySay(String speech) {
        lilytalk = (TextView) findViewById(R.id.lilyspeech);
        lilytalk.setText(speech);
    }

    //Method for adding Score
    public void AddScore(double value) {
        score = (TextView) findViewById(R.id.score);
        TotalScore += value;
        String point = TotalScore + "";
        score.setText(point);
    }

    //Method for Changing Level Text
    public void Level(int level) {
        LevelText = (TextView) findViewById(R.id.levelText);
        LevelText.setText(level + "");

    }

    //Method for adding data to graph
    private void AddGraphData(float data) {
        LineData lineData = chart.getData();
        if (lineData != null) {
            LineDataSet set = (LineDataSet) lineData.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                lineData.addDataSet(set);
            }
            lineData.addXValue("");

            lineData.addEntry(new Entry(data, set.getEntryCount()), 0);

            chart.notifyDataSetChanged();
            chart.setVisibleXRange(0, 20);
            chart.moveViewToX(lineData.getXValCount() - 21);
        }
    }

    //Method for creating data set
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "");
        set.setDrawCircles(false);
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        //set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        //set.setFillAlpha(65);
        return set;
    }
}
