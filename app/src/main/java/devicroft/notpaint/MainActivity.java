package devicroft.notpaint;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Scroller;
import android.widget.TextView;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_COLOR_SELECT = 1000;
    public static final int REQUEST_BRUSH_SELECT = 1001;
    public static final int REQUEST_SETTINGS_SELECT = 1002;
    public static final int REQUEST_CANVAS_COLOR_SELECT = 1003;
    public static final int REQUEST_IMAGE_SELECT = 1004;//menu click
    public static final int REQUEST_STYLE_SELECT = 1005;
    public static final int REQUEST_IMAGE_FROM_FILE = 1006;//selecting from file
    public static final int REQUEST_IMAGE_CAMERA = 1007;    //selecting from cam
    public static final int REQUEST_IMAGE_INTERNET = 1008;//probably won't need
    public static final int REQUEST_CANVAS_ON_ROTATE = 1009;    //to pass canvas when rotating


    //constants to define data coming back through intents
    public static final String FETCH_BRUSH_WIDTH = "width";
    public static final String FETCH_BRUSH_CAP = "cap";
    public static final String FETCH_COLOR_BRUSH = "color";
    public static final String FETCH_IMAGE_CAMERA = "camera";
    public static final String FETCH_IMAGE_FILE = "file";
    public static final String FETCH_IMAGE_INTERNET = "internet";
    public static final String FETCH_CANVAS_BACKGROUND = "background";

    public static FingerPainterView fingerPainterView;



    private void createNewCanvas(){

        //save old fpv in case of mistake
        final FingerPainterView oldFingerPainterView = fingerPainterView;
        Snackbar snackbar = Snackbar
                .make(fingerPainterView , R.string.new_canvas_confirmation, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("Main", "Undone createNewCanvas");
                        setContentView(oldFingerPainterView);
                        fingerPainterView = oldFingerPainterView;
                    }
                });
        snackbar.show();
        //load uri into new fpv or create new
        fingerPainterView = new FingerPainterView(getBaseContext());
        if(photoPath != null){
            Uri u = Uri.parse(photoPath);
            fingerPainterView.load(u);
        }
        setContentView(fingerPainterView);
        updateColor(oldFingerPainterView.getColour());
    }



    private void fetchImageFromFile(){
        Intent fileIntent = new Intent();
        fileIntent.setType("image/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(fileIntent, getResources().getString(R.string.image_from_file_prompt)), REQUEST_IMAGE_FROM_FILE);

    }

    private void fetchImageFromInternet(Uri uri){

        //TODO save downloaded image to file and then load it, creating a new canvas
    }

    //to update the color of action bar on nbrush change
    private void updateColor(int color){

        String colorHex = "#" + Integer.toHexString(color).substring(2);      //https://stackoverflow.com/questions/6539879/how-to-convert-a-color-integer-to-a-hex-string-in-android

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorHex)));
        fingerPainterView.setColour(Color.parseColor(colorHex));
    }

    //cycle through paint.style.*
    private void updateStyle() {
        //TODO change icon according to style
        if(fingerPainterView.getStyle().name().compareTo(Paint.Style.FILL.name()) == 0){
            fingerPainterView.setStyle(Paint.Style.STROKE);
        }else if(fingerPainterView.getStyle().name().compareTo(Paint.Style.FILL_AND_STROKE.name()) == 0){
            fingerPainterView.setStyle(Paint.Style.FILL);
        }else{
            fingerPainterView.setStyle(Paint.Style.FILL_AND_STROKE);
        }

        Toast.makeText(fingerPainterView.getContext() , getResources().getText(R.string.style_toast) + " " + fingerPainterView.getStyle().name(), Toast.LENGTH_SHORT).show();
        /*
        Snackbar snackbar = Snackbar
                .make(fingerPainterView , R.string.style_toast + fingerPainterView.getStyle().name(), Snackbar.LENGTH_LONG);
        snackbar.show();
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu from fingerpaintopinos
        MenuInflater menuInflater  = getMenuInflater();
        menuInflater.inflate(R.menu.finger_paint_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //sort through all options item stuff
        switch(item.getItemId()){
            //change color ,opens color wheel activity
            case R.id.action_color:
                Log.d("Activity", "Main open COLOR activity");
                Log.d("Color", "Current colour: " + fingerPainterView.getColour());
                //send over current color to keep consistent
                Intent colorIntent = new Intent(this, ColorActivity.class);
                colorIntent.putExtra(FETCH_COLOR_BRUSH, fingerPainterView.getColour());
                startActivityForResult(colorIntent, REQUEST_COLOR_SELECT);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                break;
            //change brush activity
            case R.id.action_brush:
                Log.d("Activity", "Main open BRUSH activity");
                Log.d("Brush", "Current brush name: " + fingerPainterView.getBrushCap().name());

                //pass through current brush values
                Bundle bundle = new Bundle();
                bundle.putInt(FETCH_BRUSH_WIDTH, fingerPainterView.getBrushWidth());
                //Paint.Cap.valueOf("BUTT");
                bundle.putString(FETCH_BRUSH_CAP, fingerPainterView.getBrushCap().name());
                //for action bar change in activity
                bundle.putInt(FETCH_COLOR_BRUSH, fingerPainterView.getColour());
                Intent brushIntent = new Intent(this, BrushActivity.class);
                brushIntent.putExtras(bundle);

                startActivityForResult(brushIntent, REQUEST_BRUSH_SELECT);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                break;
            //new canvas activity with
            case R.id.action_clear:
                Log.d("Activity", "Main NEW CANVAS");

                //open for result to make a new canvas with background color
                AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.new_canvas_dialog_title)
                        .setMessage(R.string.new_canvas_dialog_message)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //clear canvas
                                Intent newCanvasIntent = new Intent(MainActivity.this, ColorActivity.class);

                                //tell activity we want to show the image fetch dialog
                                //newCanvasIntent.putExtra(FETCH_CANVAS_BACKGROUND, 1);

                                newCanvasIntent.putExtra(FETCH_COLOR_BRUSH, fingerPainterView.getColour());
                                startActivityForResult(newCanvasIntent, REQUEST_CANVAS_COLOR_SELECT);
                                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //dismiss
                                dialogInterface.cancel();
                            }
                        })

                        //TODO canvas colour
                        .setNeutralButton(R.string.new_canvas_dialog_image, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle(R.string.new_canvas_dialog_title)
                                        .setMessage(R.string.new_canvas_dialog_message)
                                        .setCancelable(true)
                                        .setPositiveButton(R.string.image_dialog_file, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int which) {
                                                fetchImageFromFile();
                                            }
                                        });
                                        /*
                                        .setNeutralButton(R.string.image_dialog_internet, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                //TODO async internet image fetch
                                                Intent internetIntent = new Intent(MainActivity.this, InternetImageFetch.class);
                                                internetIntent.putExtra(FETCH_COLOR_BRUSH, fingerPainterView.getColour());
                                                startActivityForResult(internetIntent, REQUEST_IMAGE_INTERNET);
                                                overridePendingTransition(0, 0);

                                            }
                                        })
                                        .setNegativeButton(R.string.image_dialog_camera, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            //not saving to FILE! whhyys

                                                //https://developer.android.com/training/camera/photobasics.html
                                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                // Ensure that there's a camera activity to handle the intent
                                                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                                    // Create the File where the photo should go
                                                    File photoFile = null;
                                                    try {
                                                        photoFile = createImageFile();
                                                    } catch (IOException ex) {
                                                        // Error occurred while creating the File
                                                        Log.d("Camera", "Generic camera error");
                                                    }
                                                    // Continue only if the File was successfully created
                                                    if (photoFile != null) {
                                                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                                                "devicroft.notpaint",
                                                                photoFile);
                                                        photoPath = photoURI.getPath();
                                                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA);
                                                    }
                                                }
                                            }
                                        });
                                            */
                                dialog.create().show();
                            }
                        })

                        .setIcon(android.R.drawable.stat_sys_warning);
                dialog.create().show();
                //to result
                break;
            /*
            case R.id.action_settings:
                Log.d("Activity", "settings");
                break;
            */
            //opens dialogs with apache, etc licenses
            case R.id.action_licenses:
                AlertDialog d = new AlertDialog.Builder(this)
                        .setTitle("Licenses")
                        .setMessage( Html.fromHtml(getString(R.string.licenses_list)))    //https://stackoverflow.com/questions/3235131/set-textview-text-from-html-formatted-string-resource-in-xml
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(R.drawable.list_b)
                        .show();
                TextView textView = (TextView) d.findViewById(android.R.id.message);
                textView.setScroller(new Scroller(this));
                textView.setVerticalScrollBarEnabled(true);
                textView.setAllCaps(false);
                textView.setMovementMethod(new ScrollingMovementMethod());


                break;

            //to add image
            case R.id.action_image:     //for adding image onto of canvas, not as bg. move and transformable when inserted is the goal
                AlertDialog.Builder imageDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.new_canvas_dialog_title)
                        .setMessage(R.string.image_dialog_message)
                        .setCancelable(true)
                        .setIcon(R.drawable.image_g)
                        .setPositiveButton(R.string.image_dialog_internet, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //TODO get valid URL
                                //TODO get bitmap from the valid url

                            }
                        })
                        .setNegativeButton(R.string.image_dialog_file, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Intent fileIntent = new Intent();
                                fileIntent.setType("image/*");
                                fileIntent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(fileIntent, getResources().getString(R.string.image_from_file_prompt)), REQUEST_IMAGE_FROM_FILE);

                            }
                        })
                        .setNeutralButton(R.string.image_dialog_camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //TODO take photo with camera and place it ontop of canvas, no as a bg
                                overridePendingTransition(android.R.anim.bounce_interpolator, 0);
                            }
                        })
                        .setIcon(android.R.drawable.stat_sys_warning);
                imageDialog.create().show();
                break;
            //change style
            case R.id.action_style:
                updateStyle();
                break;
            //case R.id.action_save:
           //     Bitmap bitmap = Bitmap.createBitmap(fingerPainterView.getWidth(), fingerPainterView.getHeight(), Bitmap.Config.ARGB_8888);

            default:

        }
        return super.onOptionsItemSelected(item);
    }

    String photoPath;

    private File createImageFile() throws IOException {
        //https://developer.android.com/training/camera/photobasics.html
        String timeStamp = new SimpleDateFormat("ddMMyyyy HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                dir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_COLOR_SELECT:
                if(resultCode == RESULT_OK){
                    //get selected color and set new brush color
                    int colorResultAsInteger = data.getIntExtra(FETCH_COLOR_BRUSH, 000000);
                    //TODO add transparency support
                    updateColor(colorResultAsInteger);
                }
                if (resultCode == RESULT_CANCELED) {
                    //abort
                }

                break;
            case REQUEST_BRUSH_SELECT:
                if(resultCode == RESULT_OK){


                    Bundle bundle = data.getExtras();
                    String cap = bundle.getString(FETCH_BRUSH_CAP);
                    int width = bundle.getInt(FETCH_BRUSH_WIDTH);

                    fingerPainterView.setBrushCap(Paint.Cap.valueOf(cap));
                    fingerPainterView.setBrushWidth(width);


                }
                if(resultCode == RESULT_CANCELED){
                    //abort
                }

                break;
            case REQUEST_SETTINGS_SELECT:

                break;
            case REQUEST_CANVAS_COLOR_SELECT:


                createNewCanvas();
                if(resultCode == RESULT_OK){
                    int color = data.getIntExtra(FETCH_COLOR_BRUSH, 000000);        //canvas comes up as null
                    FingerPainterView ofpv = fingerPainterView;
                    fingerPainterView = new FingerPainterView(getBaseContext());
                    fingerPainterView.setBgColor(color);
                    setContentView(fingerPainterView);
                    updateColor(ofpv.getColour());
                }
                //fingerPainterView.setBackgroundColor(data.getIntExtra("result", 16777215));     //defaults to white

                break;
            case REQUEST_IMAGE_SELECT:


                break;
            case REQUEST_STYLE_SELECT:
                updateStyle();
                break;

            case REQUEST_IMAGE_FROM_FILE:
                //http://codetheory.in/android-pick-select-image-from-gallery-with-intents/
                //https://stackoverflow.com/questions/19834842/android-gallery-on-kitkat-returns-different-uri-for-intent-action-get-content
                //https://stackoverflow.com/questions/20260416/retrieve-absolute-path-when-select-image-from-gallery-kitkat-android
            //TODO open folder and allow selection of image

                Uri uri = data.getData();
                FingerPainterView ofpv = fingerPainterView;
                fingerPainterView = new FingerPainterView(getBaseContext());
                fingerPainterView.load(uri);
                setContentView(fingerPainterView);
                updateColor(ofpv.getColour());


                    /*
                try {
                    FOR ADDING IT ONTO CANVAS< NOT AS A CANVAS
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Log.d("FILE", String.valueOf(bitmap));
                    fingerPainterView.getCanvas().drawBitmap(bitmap, new Rect(0,0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0,0,100,100), null);




                } catch (IOException e) {
                    e.printStackTrace();
                }
                    */
                break;
            case REQUEST_IMAGE_CAMERA:

                //TODO figure out how to get camera uri out of data here.
                if (photoPath != null) {
                    galleryAddPic();
                    createNewCanvas();
                    photoPath = null;
                }


                break;
            case REQUEST_IMAGE_INTERNET:

                if(resultCode == RESULT_OK){

                    new DownloadImage().execute(data.getStringExtra(FETCH_IMAGE_INTERNET));

                }
                break;
            default:

        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    // DownloadImage AsyncTask
    private class DownloadImage extends AsyncTask<String, Void, String> {

        ProgressDialog progress;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(MainActivity.this);
            progress.setTitle("Downloading image");
            progress.setMessage("Loading...");
            progress.setIndeterminate(false);
            progress.show();
        }

        @Override
        protected String doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }

            FileOutputStream out = null;

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (!storageDir.exists())
                storageDir.mkdirs();
            try{
                File image = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );

                photoPath = image.getAbsolutePath();
                out = new FileOutputStream(image.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }



            return imageURL;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            fingerPainterView.load(Uri.parse(s));
            galleryAddPic();
            createNewCanvas();
        }
    }




    /*
    ACTIVITY STATE HANDLERS
     */

    //handle device rotation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO create fingerPainterView to be serializable
        //outState.putSerializable(fingerPainterView.get);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Activity", "main created");

        fingerPainterView = new FingerPainterView(getBaseContext());
        setContentView(fingerPainterView);


        updateColor(fingerPainterView.getColour());

        /*
        if(getIntent().hasExtra("dialog")) {//dialog intent
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.new_canvas_dialog_title)
                    .setMessage(R.string.new_canvas_dialog_message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.image_dialog_file, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int which) {
                            fetchImageFromFile();
                        }
                    })
                    .setNeutralButton(R.string.image_dialog_internet, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //TODO async internet image fetch
                        }
                    })
                    .setNegativeButton(R.string.image_dialog_camera, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if(cameraIntent.resolveActivity(getPackageManager()) != null){
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException ex) {
                                    Log.d("Camera", "Generic error");
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(getBaseContext(),
                                            "devicroft.notpaint",
                                            photoFile);
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAMERA);

                                    //Intent capturedUriIntent = new Intent(MainActivity.this, MainActivity.class);

                                }

                            }

                        }
                    });
            ;


            dialog.create().show();


        }*/
    }

    @Override
    protected void onStart() {
        Log.d("Activity", "main Started");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d("Activity", "Main resumed");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("Activity", "main paused");
        //on dialog popups
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("NotPaint", "Main Stopped");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("NotPaint", "Main Destroyed");
        super.onDestroy();
        //back button on main canvas screen
    }
}
