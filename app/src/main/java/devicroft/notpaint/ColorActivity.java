package devicroft.notpaint;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.flask.colorpicker.ColorPickerPreference;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;

public class ColorActivity extends AppCompatActivity {

    ColorPickerView colorPickerView;


    RelativeLayout layout;


    @Override
    protected void onStart() {
        super.onStart();
        //update actionbar colr
        if(getIntent().hasExtra(MainActivity.FETCH_COLOR_BRUSH)){
            Intent i = getIntent();
            int colorInt = i.getIntExtra(MainActivity.FETCH_COLOR_BRUSH, 0);

            Log.d("Color intent", Integer.toString(colorInt));
            String colorHex = "#" + Integer.toHexString(colorInt);

            if(colorHex.length() > 2 || colorHex  != null) {
                colorHex.substring(2);
            }else{
                colorHex = "#111111";
            }
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorHex)));


        }

        int i = getIntent().getIntExtra(MainActivity.FETCH_COLOR_BRUSH, 0);
        colorPickerView.setInitialColor(i, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);

        Log.d("Activity", "Color Created");


        //setup layouts
        layout = (RelativeLayout) findViewById(R.id.activity_color_layout);
        colorPickerView = (ColorPickerView) findViewById(R.id.color_picker_view);


        final OnColorSelectedListener onColorSelectedListener = new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int colorInteger) {
                Log.d("Color Picker", "Color" + colorInteger);

                //layout.setBackgroundColor(Color.parseColor(colorHex));
            }
        };


        colorPickerView.addOnColorSelectedListener(onColorSelectedListener);









    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.color_picker_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_color_confirm:
                Log.d("Color Picker", "Selected color" + colorPickerView.getSelectedColor());

                Intent result = new Intent();
                result.putExtra(MainActivity.FETCH_COLOR_BRUSH, colorPickerView.getSelectedColor());
                this.setResult(RESULT_OK, result);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            /*
            TODO: add in manual hex support
            case R.id.action_color_input_hex:

                break;
             */

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //formally say color select was cancelled
        Log.d("Color Picker", "Color selection cancelled");
        Intent result = new Intent();
        this.setResult(RESULT_CANCELED, result);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
