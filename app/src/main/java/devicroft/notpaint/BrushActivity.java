package devicroft.notpaint;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class BrushActivity extends AppCompatActivity {

    RadioButton selectButt;
    RadioButton selectRound;
    RadioButton selectSquare;

    TextView sizeLabel;
    SeekBar sizeSeek;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brush);

        selectButt = (RadioButton) findViewById(R.id.select_butt);
        selectRound = (RadioButton) findViewById(R.id.select_round);
        selectSquare = (RadioButton) findViewById(R.id.select_square);
        sizeLabel = (TextView) findViewById(R.id.brush_size_label);
        sizeSeek = (SeekBar) findViewById(R.id.brush_size_seek);


        Bundle bundle = getIntent().getExtras();
        String capName = bundle.getString(MainActivity.FETCH_BRUSH_CAP);
        int currentCapWidth = bundle.getInt(MainActivity.FETCH_BRUSH_WIDTH);
        int currrentColorInt = bundle.getInt(MainActivity.FETCH_COLOR_BRUSH, 0);

        sizeLabel.setText("Brush Size: " + currentCapWidth);
        sizeSeek.setProgress(currentCapWidth);

        // sizeLabel.setText(R.string.brush_size_label_default + capWidth);

        //update action bar color
        Log.d("Brush activity", "brush color" + Integer.toString(currrentColorInt));
        String colorHex = "#" + Integer.toHexString(currrentColorInt);
        if (colorHex.length() > 2 || colorHex != null) {
            colorHex.substring(2);
        } else {
            colorHex = "#111111";
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorHex)));

        sizeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //sizeLabel.setText(R.string.brush_size_label_default + i);
                sizeLabel.setText("Brush size: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //TODO clean this up you lazy man
        selectRound.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Log.d("Brush", "Cap: " + Paint.Cap.ROUND.name());

               selectButt.setChecked(false);
               selectSquare.setChecked(false);
               selectRound.setChecked(true);
           }
        });

        selectSquare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectButt.setChecked(false);
                selectRound.setChecked(false);
                selectSquare.setChecked(true);
            }
        });


        selectButt.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
              selectRound.setChecked(false);
              selectSquare.setChecked(false);
              selectButt.setChecked(true);
           }
        });
        if(capName.compareTo(Paint.Cap.BUTT.name())==0){
            selectButt.setChecked(true);
        }else if(capName.compareTo(Paint.Cap.SQUARE.name()) == 0){
            selectSquare.setChecked(true);
        }else if(capName.compareTo(Paint.Cap.ROUND.name())==0){
            selectRound.setChecked(true);
        }else{
            Log.w("Brush", "No radio was active ?");
            bundle.putString(MainActivity.FETCH_BRUSH_CAP, Paint.Cap.ROUND.name());
        }


                /*

        //set starting position of caps
                if(capName.equals(Paint.Cap.BUTT.name())){
                    selectButt.setChecked(true);
                    selectRound.setChecked(false);
                    selectSquare.setChecked(false);
                }else if(capName.equals(Paint.Cap.ROUND.name())){
                    selectButt.setChecked(false);
                    selectRound.setChecked(true);
                    selectSquare.setChecked(false);
                }else{
                    selectButt.setChecked(false);
                    selectRound.setChecked(false);
                    selectSquare.setChecked(true);
                }

        */

    }


    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.brush_picker_options, menu);
        Log.d("Brush", "Menu inflated");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_brush_confirm:

                Log.d("Brush", "SIZE: " + sizeSeek.getProgress());


                Bundle bundle = new Bundle();
                bundle.putInt(MainActivity.FETCH_BRUSH_WIDTH, sizeSeek.getProgress());

                //TODO clean this up
                if(selectRound.isChecked()){
                    bundle.putString(MainActivity.FETCH_BRUSH_CAP, Paint.Cap.ROUND.name());
                }else if(selectButt.isChecked()){
                    bundle.putString(MainActivity.FETCH_BRUSH_CAP, Paint.Cap.BUTT.name());
                }else if(selectSquare.isChecked()){
                    bundle.putString(MainActivity.FETCH_BRUSH_CAP, Paint.Cap.SQUARE.name());
                }else{
                    Log.w("Brush", "No radio was active ?");
                    bundle.putString(MainActivity.FETCH_BRUSH_CAP, Paint.Cap.ROUND.name());
                }


                Intent brushIntent = new Intent(BrushActivity.this, MainActivity.class);
                brushIntent.putExtras(bundle);

                setResult(BrushActivity.RESULT_OK, brushIntent);        //always remember to put set result after the intent...
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                break;
            default:
                //

        }
        return true;

    }

    @Override
    public void onBackPressed() {
        Log.d("Brush Picker", "Brush mod cancelled");
        Intent result = new Intent();
        this.setResult(RESULT_CANCELED, result);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}




