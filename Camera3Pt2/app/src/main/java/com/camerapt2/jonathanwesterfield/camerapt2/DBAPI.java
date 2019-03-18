package com.camerapt2.jonathanwesterfield.camerapt2;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class DBAPI
{
    private SQLiteDatabase db;
    private Context context;
    private final String dbName = "Pictures";
    private final String CREATE_SQL = "CREATE TABLE " + this.dbName + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " image TEXT)";
    private final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS " + this.dbName;

    public DBAPI() { /* Empty Constructor */}

    public DBAPI(Context context)
    {
        this.context = context;
        this.db = context.openOrCreateDatabase(this.dbName, Context.MODE_PRIVATE, null);

        createDB();
    }

    /**
     * We reset the database everytime we start the app because I was lazy and didn't want
     * to troubleshoot the database if things didn't work or I wrote the wrong SQL statement
     */
    private void createDB()
    {
        try
        {
            this.db.execSQL(this.DROP_TABLE_SQL);
            this.db.execSQL(this.CREATE_SQL);
        }
        catch (SQLiteException e)
        {
            System.out.println("Couldn't create the Pictures table");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inserts a newly taken image into the database. The image bitmap must first be converted
     * to a base64 string so that we don't have to insert it as a blob. Blobs are bad
     * @param base64Img
     * @throws SQLiteException
     */
    public void insertImg(String base64Img) throws SQLiteException
    {
        String insert = "INSERT INTO " + this.dbName + "(image) VALUES ('" + base64Img + "')";
        db.execSQL(insert);
    }

    /**
     * Using the entered user id, find a corresponding picture. If one isn't found or an
     * error is thrown, return that to the calling function to deal with it
     * @param id
     * @return
     * @throws SQLiteException
     */
    public String getPic(String id) throws SQLiteException
    {
        String select = "SELECT image FROM " + this.dbName + " WHERE id=" + id;
        Cursor cr = db.rawQuery(select, null);

        String returnedImg = "MASSIVE FUCKING SQL ERROR";

        if(cr.moveToFirst())
        {
            do {
                returnedImg = cr.getString(cr.getColumnIndex("image"));
            }
            while (cr.moveToNext());
            cr.close();
        }
        return returnedImg;
    }

    /**
     * Putting blobs into databases is never a good idea especially with bitmaps in SQLite
     * so we are going to encode it into a base64 string so it is more stable
     * @param bmp
     * @return
     */
    public String encodeToBase64(Bitmap bmp)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Could be Bitmap.CompressFormat.PNG or Bitmap.CompressFormat.WEBP
        byte[] bai = baos.toByteArray();

        String base64Image = Base64.encodeToString(bai, Base64.DEFAULT);
        return base64Image;
    }

    /**
     * Decode the base64 string back into the original image bitmap
     * @param base64Img
     * @return
     */
    public Bitmap decodeFromBase64(String base64Img)
    {
        byte[] data = Base64.decode(base64Img, Base64.DEFAULT);
        Bitmap bm;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;
        bm = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
        return bm;
    }


    /**
     * Check if the database exist and can be read.
     *
     * @return true if it exists and can be read, false if it doesn't
     */
    private boolean dataBaseExists()
    {
        SQLiteDatabase checkDB = null;
        try
        {
            checkDB = SQLiteDatabase.openDatabase(this.dbName, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        }
        catch (SQLiteException e)
        {
            // database doen't exist yest so the statement below will return false
        }
        return checkDB != null;
    }
}
