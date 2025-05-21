package com.example.evstations;

import static com.mappls.sdk.maps.Mappls.getApplicationContext;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mappls.sdk.maps.Mappls;

public class DBClass extends SQLiteOpenHelper {

    public static String dbname = "evstations";

    public static String url = "http://192.168.56.66/evstations/api/";

    public static String urlLogin = url + "login.php";
    public static String urlRegistration = url + "registration.php";
    public static String urlUsers = url + "users.php";
    public static String urlProfile = url + "profile.php";
    public static String urlUpdateProfile = url + "updateprofile.php";
    public static String urlStations = url + "stations.php";
    public static String urlFindStations = url + "findstations.php";
    public static String urlDeleteStation = url + "deletestation.php";

    public static String urlAddStation = url + "add_station.php";
    public static String urlStationSlots = url + "station_slots.php";
    public static String urlAddStationSlot = url + "add_slot.php";
    public static String urlAddVehicle = url + "add_vehicle.php";
    public static String urlVehicles = url + "vehicles.php";
    public static String urlBookSlot = url + "bookslot.php";
    public static String urlBookings = url + "bookings.php";
    public static String urlCancelBooking = url + "cancelbooking.php";



//    public static void checkCredentials()
//    {
//        MapplsAccountManager.getInstance().setRestAPIKey("3b85112cd7196f338bcfafc7a00eeff6");
//        MapplsAccountManager.getInstance().setMapSDKKey("3b85112cd7196f338bcfafc7a00eeff6");
//        MapplsAccountManager.getInstance().setAtlasClientId("96dHZVzsAutI2dYJ4tJlzaa886K6I2iQ2NLW6dFa93wUbjPFlH2qBebJx2uGbqpY730umVH6PLgAUc5tTw59Hw==");
//        MapplsAccountManager.getInstance().setAtlasClientSecret("lrFxI-iSEg8zNE_bzFpz_7Jv0B8_kx8n4wJEOhdu9W0_1NZKxj35xvdsj935LW-DeJyv5X_ATjCooSR1_czuwgAzC4JRYG7p");
//        Mappls.getInstance(getApplicationContext());
//    }


    public static SQLiteDatabase database;


    public DBClass(Context context){

        super(context, DBClass.dbname, null, 1);
    }

    public void onCreate(SQLiteDatabase arg) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    public static void execNonQuery(String query){
        //Execute Insert, Update, Delete, Create table queries
        //Log.e("Quesry", query);
        database.execSQL(query);
    }

    public static Cursor getCursorData(String query){
        //Log.d("SQuery", query);
        Cursor res =  database.rawQuery(query, null);
        return res;
    }

    public static String getSingleValue(String query) {
        try {
            Cursor res = getCursorData(query);
            String value = "";
            if (res.moveToNext()) {
                return res.getString(0);
            }
            return value;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static int getNoOfRows(String query){
        try {
            Cursor res = database.rawQuery(query, null);
            return res.getCount();
        }catch (Exception ex)
        {
            return 0;
        }
    }

    public static boolean checkIfRecordExist(String query){
        //Log.e("CheckQuery", query);
        Cursor res =  database.rawQuery(query, null );
        if(res.getCount() > 0)
            return true;
        else
            return false;
    }


    public static boolean doesTableExists(String tableName)
    {
        try{
            Cursor cursor = getCursorData("SELECT * FROM " + tableName);
            return true;
        }
        catch (Exception ex)
        {
            return  false;
        }
    }

    public static boolean doesFieldExist(String tableName, String fieldName)
    {
        try {
            String query = "SELECT " + fieldName + " FROM " + tableName;
            Cursor cursor = getCursorData(query);
            return  true;
        }
        catch (Exception ex)
        {
            return  false;
        }
    }


}
