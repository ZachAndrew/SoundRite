package maro.okoro.soundrite;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

//*Addon #1 imports (ABOVE)*
//*ANDROID STUDIO DEVELOPER SAMPLE CODE IMPLEMENTATION START*:

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //for audio playbackstatus
    boolean isPlaying=false;
    private String OUTPUT_FILE;
    private MediaRecorder recorder;
    private MediaPlayer mediaPlayer;
    private StorageReference mStorage;// reference for firebase storage
    private ProgressDialog mProgress;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Start:*Drawer extra*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //End: *Drawer extra*

        //Firebase Storage
        mStorage = FirebaseStorage.getInstance().getReference();
        mProgress = new ProgressDialog(this);

        //Buttons
        final Button listenButton = (Button) findViewById(R.id.button_Record);//listen button
        final Button playbutton=(Button)findViewById(R.id.button_playback);//playback button
        final Button stopRecordingButton= (Button)findViewById(R.id.button_stop_recording);//stop recording button
        final Button saveRecordingButton =  (Button)findViewById(R.id.button_save);//save recording
        //TextViewsa
        final TextView feedBackTextView=(TextView)findViewById(R.id.textView_feedback); //feedback box
        final TextView scoreBox= (TextView)findViewById(R.id.textView_score);//score box
        final TextView scoreValue= (TextView)findViewById(R.id.textView_scoreText);// level box
        final TextView extraInfoBox=(TextView)findViewById(R.id.textView_extraInfo);//extra info box
        //Switches
        final Switch recSwitch = (Switch) findViewById(R.id.switch_listen);//listen switch

        //Implement main functions
        // #test 1: implement a simple audio playback, store can play audio stored

      //  final MediaPlayer lovelyday= MediaPlayer.create(this, R.raw.lovelyday);

        /*#2 DISABLING PLAY MUSIC FUNCTION TEST start: *
        lovelyday.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                playbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(lovelyday.isPlaying()){
                            lovelyday.stop();
                            lovelyday.seekTo(0);

                        }else {
                            lovelyday.start();
                        }


                   /*     REMOVED temp
                    if (!lovelyday.isPlaying()) {
                            lovelyday.start();

                        } else if(lovelyday.isPlaying()){
                            lovelyday.stop();
                            lovelyday.seekTo(0);
                        }
                        isPlaying = !isPlaying;}*/


                /* ***Attempt 1 start:*** to play then stop audio onClick
                if(playbutton.getText().toString().equals("Playback")&&!lovelyday.isPlaying()) {
                    playbutton.setText("Stop");
                    lovelyday.start(); //play 'Lovely Day' by Bill Withers

                }
                else if(playbutton.getText().toString().equals("Stop")&&lovelyday.isPlaying()) {
                    playbutton.setText("Playback");
                   // if(lovelyday.isPlaying()) {
                        lovelyday.stop(); //stop 'Lovely Day' by Bill Withers
                        //lovelyday.release(); //release stored audio to be resetted again
                    //}

                }
            }   //Attempt 1 end
                });

            }
        });
        * #2 DISABLING PLAY MUSIC FUNCTION TEST end:*/

//* #1 Recording audio ATTEMPT 1: *
        //* Set up record button function using 'listenbutton' *

        File audioFile = this.getCacheDir();
        OUTPUT_FILE =audioFile.getPath()+"/"+"myAudioFile.3gp";

        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                try{ startRecording();  //start recoding function
                }catch (Exception e){
                    System.out.println("***ERROR OCCURRED TRYING TO RECORD");
                }
                //AESTHETICS
                recSwitch.setText("LIVE");
                recSwitch.setTextColor(getResources().getColor(R.color.colorRed));
                recSwitch.toggle();

                feedBackTextView.setTextColor(getResources().getColor(R.color.colorRed));
                feedBackTextView.setText("RECORDING...");

            }
        });
        //* #1 Recording audio attempt end:

        //* #2 Stop recording start:
        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{stopRecording();}catch (Exception e){
                    e.printStackTrace();
                    System.out.println("***ERROR WITH STOPPING THE RECORDING***");
                }
                //AESTHETICS
                recSwitch.setText("Off");
                recSwitch.setTextColor(getResources().getColor(R.color.colorBlack));
                recSwitch.toggle();

                feedBackTextView.setTextColor(getResources().getColor(R.color.colorBlack));
                feedBackTextView.setText("STOPPED RECORDING\n(holding recorded audio)");

                //#3
                Random rand=new Random(); //*For testing scoring function*
                Integer x=rand.nextInt(100);
                //String temp=x.toString();
                scoreValue.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                scoreValue.setText(" "+x+"%");
            }
        });
        //: #2 Stop recording end


        playbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                try {playRecording();} catch (Exception e){
                    System.out.println("***ERROR OCCURRED TRYING TO PLAYBACK");
                }
                feedBackTextView.setTextColor(getResources().getColor(R.color.colorGreen));
                feedBackTextView.setText("PLAYING BACK");
            }
        });

        saveRecordingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {saveRecording();} catch (Exception e){
                    System.out.println("***ERROR OCCURRED TRYING TO SAVE");
            }
        }
    });
    }
    // ***OUTSIDE ONCREATE***
    //Function for Recoding Voice start:
    private void startRecording() throws IllegalStateException, IOException {

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(OUTPUT_FILE);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("development", "prepare() failed");
        }
        recorder.start();



    }
    //:Function for Recording Void end

    //Function for PlayingBack audio recorded start:
    private void playRecording() throws Exception{

        ditchMediaPlayer();
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setDataSource(OUTPUT_FILE);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }
    //:Function for PlayingBack recorded audio end

    private void ditchMediaPlayer() throws Exception{

        if(mediaPlayer !=null)
        {
            try{
                mediaPlayer.release();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    private  void stopRecording() throws Exception{
        if(recorder!=null){
            recorder.stop();
            ditchMediaPlayer();

        }
    }

    private void saveRecording() throws Exception{
        uploadAudio();
    }

// UPLOAD AUDIO TO FIREBASE
    private void uploadAudio() {
        mProgress.setMessage("Uploading Audio....");
        mProgress.show();
        StorageReference filepath = mStorage.child("Audio").child("new_audio.3gp" + new Date().getTime()); //makes sure file is not overwritten
        Uri uri = Uri.fromFile(new File(OUTPUT_FILE));
        filepath.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
               mProgress.dismiss();

               Uri downloadUrl = taskSnapshot.getDownloadUrl();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgress.setMessage("Unsuccessful upload");
                        mProgress.show();
                    }
                });
    }


    // #3 UI implmentation for scores/level/extra info start:
    // :#3 UI implmentation for scores/level/extra info end

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


    }

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;


    //private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;


    // private PlayButton mPlayButton = null;
    private MediaPlayer mPlayer = null;


    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

}

    /*  #1 start: REMOVED TEMPORARILY FOR TESTING OTHER CODE
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;


    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;


    private PlayButton mPlayButton = null;
    private MediaPlayer mPlayer = null;


    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    //#1++ START
    //#1++ END
    ///*#1 START edit:
    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }
    //#1 END edit*/

    /* #2 start: continuation of removing temp code redenduncy*
    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Start:*Drawer extra*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //End: *Drawer extra*

        //*Buttons from XML page

        // mRecordButton = (RecordButton) findViewById(R.id.button_Record);
        // mPlayButton = (PlayButton) findViewById(R.id.button_playback);
        mRecordButton=new RecordButton(this);
        mPlayButton=new PlayButton(this);


        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);



/*
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.activity_main, null);

// fill in any details dynamically here
        //TextView textView = (TextView) v.findViewById(R.id.a_text_view);
        //textView.setText("your text");
        mRecordButton = new RecordButton(this);
        mPlayButton = new PlayButton(this);

// insert into main view
        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.transition_current_scene);
        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
       */

         /*#1 AddView to existing layout attempt
        LinearLayout ll = new LinearLayout(this);


        mRecordButton = new RecordButton(this);
       // RecordButton=(Button)mRecordButton; //trying to cast mRecordButton into type 'Button'
        ll.addView(mRecordButton);

        mPlayButton = new PlayButton(this);
       // PlayButton=(Button) mPlayButton; //trying to cast mPlayButton into type 'Button'
        ll.addView(mPlayButton);
        setContentView(ll);
        //#1 END*/

//#2 ATTEMPt TO SYNC RECORD BUTTON WITH MY LAYOUT

//#2 END



/*
    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

*/

//ANDROID STUDIO DEVELOPER SAMPLE CODE IMPLEMENTATION END*



/*Tom and Joe's Lily code  START:

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
/*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this); */

//*Addon #2*
//Remembering the goal
// $$$EXTRA$$$?  String opening1 = getResources().getString(R.string.lilyOpen);
// $$$EXTRA$$$?  String opening2 = getResources().getString(R.string.lilyOpen2);
// $$$EXTRA$$$? String opening = opening1 +"\n"+opening2;
// $$$EXTRA$$$? LilySay(opening);

//Preparing Song FFT-midi values
       /*  String[] query = new String[50];
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
                    //recordPart=pitches;
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
Tom & Joe's Lily code END*/
