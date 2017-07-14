package com.google.android.gms.samples.vision.barcodereader.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DatabaseHelper extends SQLiteOpenHelper {

        private static final String TAG = SQLiteOpenHelper.class.getSimpleName();
        private String DB_PATH = null;

        // название бд
        private static final String DB_NAME = "DataBase.db";
        private SQLiteDatabase myDataBase;
        private final Context myContext;

        // Mode
        private static final int Open = 0;
        private static final int OpenAndUpdate = 1;


        /**
         * <p> Открывает базу данных и потготавливает ее к работе
         * </p>
         * @param context  текущие настройки приложения
         *                 @see Context
         * @param Mode настройки 0 - открыть 1 - обновить и открыть
         * **/
        public DatabaseHelper(Context context,int Mode) {
                super(context, DB_NAME, null, 1);
                this.myContext = context;
                this.DB_PATH = "/data/data/"+ context.getPackageName() + "/databases/";


                if (Mode == Open) {
                        try {
                                openDataBase();

                        } catch (SQLException sqle) {
                                Log.e(TAG,"Filed open db", sqle);
                        }
                }
                if(Mode == OpenAndUpdate){
                        try {
                                openDataBase();
                                DBUpdate();

                        } catch (SQLException sqle) {
                                Log.e(TAG,"Filed open|update db", sqle);
                        }
                }

        }


        /**
         * <p> Создает системную бд, если ее не существовало
         * </p>
         * @exception IOException если не удается создать указанную бд
         * **/
        private void createDataBase() throws IOException {
                boolean dbExist = checkDataBase();

                if (!dbExist) {
                        getReadableDatabase();
                        copyDataBase();
                }
        }


        /**
         * Проверяет на существование указанную бд
         *
         * @see SQLiteDatabase
         * **/
        private boolean checkDataBase() {

                SQLiteDatabase checkDB = null;
                try {
                        String myPath = DB_PATH + DB_NAME;
                        // открывает базу данных для чтения
                        checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
                } catch (SQLiteException e) {
                        Log.e(TAG, "Couldn't open " + DB_NAME, e);
                }

                if (checkDB != null) {
                        checkDB.close();
                }
                return checkDB != null;
        }


        /**
         * <p> Копирует локальную бд из <part>./java/assets/*.db</part> в системную
         * </p>
         * @see OutputStream исходящий потое
         * @see InputStream входящий поток
         * @exception IOException если не удается копировать указанную бд
         * **/
        private void copyDataBase() throws IOException{
                //Открываем локальную БД как входящий поток
                InputStream myInput = myContext.getAssets().open(DB_NAME);

                //Путь к созданной БД
                String outFileName = DB_PATH + DB_NAME;

                //Открываем пустую базу данных как исходящий поток
                OutputStream myOutput = new FileOutputStream(outFileName);

                //перемещаем байты из входящего файла в исходящий
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer))>0){
                        myOutput.write(buffer, 0, length);
                }

                //закрываем потоки
                myOutput.flush();
                myOutput.close();
                myInput.close();
        }


        /**
         * <p> Открывает указанную базу данных
         * </p>
         * @see SQLiteDatabase
         * @exception SQLException если не удается открыть указанную бд
         * **/
        private void openDataBase() throws SQLException {

                try {
                        this.createDataBase();
                } catch (IOException ioe) {
                        throw new Error("Unable to create database");
                }

                String myPath = DB_PATH + DB_NAME;
                myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }


        @Override
        public synchronized void close() {
                if (myDataBase != null)
                        myDataBase.close();
                super.close();
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }


        /**
         * <p> Обновляет текущую базу данных
         * </p>
         * **/
        private void DBUpdate() throws SQLException{
                try {
                        copyDataBase();
                } catch (IOException e) {
                        throw new Error("Filed to copy database");

                }
        }


        /**
        * <p>Принимает на вход <var>table</var> и <var>selection</var>
         * Осуществляет поиск по таблице с именем <var>table</var> по полю _id
         * со значением равным <var>selection</var>,
         * </p>
        * @param table название таблицы (типа String)
         *
         *
        * @param selection значение поля _id, по которому осуществляется поиск в таблице <var>table</var>(типа String)
         * @return объект класса Cursor идет на выход
         * **/
        public Cursor getProductById(String table,String selection) {
                selection = "_id = " + selection;
                return myDataBase.query(table, null,selection, null, null, null, null);
        }

    /**
     * <p>Принимает на вход <var>table</var>,<var>column_name</var> и <var>selection</var>
     * Осуществляет поиск по таблице с именем <var>table</var> по полю
     * со значением равным <var>selection</var>,
     * </p>
     * @param table название таблицы (типа String)
     *
     * @param column_name название поля (типа String)
     * @param selection значение поля , по которому осуществляется поиск в таблице <var>table</var>(типа String)
     * @return объект класса Cursor идет на выход
     * **/
    public Cursor getProduct(String table,String column_name,String selection) throws SQLiteException {
        selection = column_name+" = '" + selection+"'";
        return myDataBase.query(table, null,selection, null, null, null, null);
    }


        /**
         * <p>Принимает на вход <var>table</var> и <var>selection</var>
         * </p>
         * @param table название таблицы (типа String)
         * @return объект класса Cursor идет на выход (Возвращает все записи таблицы <var>table</var>)
         *
         * **/
        public Cursor getAll(String table)throws SQLiteException {
                return myDataBase.query(table, null, null, null, null, null, null);
        }

        public Cursor queryWhere(String table,String where)throws SQLiteException {
                return myDataBase.query(table, null, where, null, null, null, null);
        }
}
