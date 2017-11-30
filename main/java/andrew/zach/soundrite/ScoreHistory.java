package andrew.zach.soundrite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Zach on 06/11/2017.
 */

public class ScoreHistory extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorehistory);

    }
    @Override
    public void onBackPressed(){
        Intent intent=new Intent (ScoreHistory.this,MainActivity.class);
        startActivity(intent);
    }
}
