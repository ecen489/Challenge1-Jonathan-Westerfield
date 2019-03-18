package com.camerapt2.jonathanwesterfield.camerapt2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.Image;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.text.NumberFormat;
import java.text.ParsePosition;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQ_CODE_TAKE_PICTURE = 1;
    ImageView imgView;
    ImageButton captureBtn;
    EditText lookupTxt;
    Button lookupBtn;
    DBAPI db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeInterfaces();

        requestCameraPermission(findViewById(android.R.id.content));
    }

    public void initializeInterfaces()
    {
        this.imgView = (ImageView) findViewById(R.id.imageView);
        this.captureBtn = (ImageButton) findViewById(R.id.captureBtn);
        this.lookupTxt = (EditText) findViewById(R.id.idSearchText);
        this.lookupBtn = (Button) findViewById(R.id.lookupBtn);
        this.db = new DBAPI(getApplicationContext());
    }

    /**
     * Request permission to use the camera if it already isn't written in the manifest file
     * @param view
     */
    private void requestCameraPermission(View view)
    {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
            showCameraPermissionsAlert(view);
        else
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    /**
     * Lookup button click listener. Get the entered ID and select record from the database.
     * If the record can't be found or doesn't exist, display an error message
     * @param view
     */
    public void onLookupClk(View view)
    {
        String idChoice = this.lookupTxt.getText().toString();

        // check if what was input is actually a number
        if (isNumeric(idChoice))
        {
            try
            {
                String encodedImg = db.getPic(idChoice);
                if(encodedImg.equalsIgnoreCase("MASSIVE FUCKING SQL ERROR"))
                    showPicNotFoundAlert(view);
                else
                {
                    Bitmap bmp = db.decodeFromBase64(encodedImg);
                    this.imgView.setImageBitmap(bmp);
                }
            }
            catch (SQLiteException e )
            {
                System.out.println("COULDN'T READ SELECT FROM DB");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        else
        {
            showInvalidIDAlert(view);
        }
    }

    public boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

    public void onCaptureBtnClk(View view)
    {
        openCamera();
    }

    /**
     * Open up the camera app to take a picture since I don't know the correct
     * REQ_CODE to take a picture. 90210 from the slides is bullshit and doesn't work.
     * It makes the app crash every time so REQ_CODE is set to be 1 since I couldn't find
     * any documentation that actually lists what the code numbers mean
     */
    public void openCamera()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQ_CODE_TAKE_PICTURE);
        }

    }

    /**
     * Get the picture that was taken with the defualt camera app.
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(resultCode == RESULT_OK)
        {
            try
            {
                Bitmap bmp = (Bitmap) intent.getExtras().get("data");
                this.imgView.setImageBitmap(bmp);
                // convert to base64 values and insert into DB
                db.insertImg(db.encodeToBase64(bmp));
            }
            catch(SQLiteException e)
            {
                System.out.println("COULD NOT INSERT TO DATABASE");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Sometimes we have to ask the user for permission to use the camera
     * Shows OK/Cancel confirmation dialog about camera permission.
     * @param view
     */
    public void showCameraPermissionsAlert(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("We need help").
                setMessage("We need permission to use the camera")
                .setPositiveButton("Yeet", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Activity parent = getParent();
                        parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                REQUEST_CAMERA_PERMISSION);
                    }
                })
                .setNegativeButton("Hell Naw",
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Activity activity = getParent();
                                if (activity != null)
                                    activity.finish();
                            }
                        });


        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Shows the user entered ID is not numeric or has something else wrong with it
     * @param view
     */
    public void showInvalidIDAlert(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stop It. Get Some Help.").
                setMessage("Please enter a numeric ID")
                .setNeutralButton("Yeet", null);


        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Alert to show that the record wasn't found in the database
     * @param view
     */
    public void showPicNotFoundAlert(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("It literally doesn't exist").
                setMessage("Couldn't find a picture with that ID in our database.")
                .setNeutralButton("Yeet", null);


        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
