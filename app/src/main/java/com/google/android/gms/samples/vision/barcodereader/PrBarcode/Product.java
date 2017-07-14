package com.google.android.gms.samples.vision.barcodereader.PrBarcode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.samples.vision.barcodereader.DataBase.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;


public class Product  {
    private static final String TAG = "ProductGetting";
    private static final String PRODUCTS_TABLE = "Products";
    private static final String SLOTS_TABLE = "Slots";
    private static final String SLOTS_TABLE_NAME = "name";
    private static final String DATA_TABLE = "Data";
    private static final String DATA_TABLE_TIME_REPLACE = "timeReplace";
    private static final String DATA_TABLE_TIME_ADDED = "timeAdded";
    private static final String DATA_TABLE_PRODUCT_ID = "productId";
    private static final String DATA_TABLE_CURRENT_SLOT = "currentSlot";
    private static final String ID = "_id";
    private static final String VALUE = "value";
    private static final String SLOT = "slot";
    private static final String NULL = " - ";


    // поля, для харектеристики товара
    private  String value;// Значение товара
    private   int valueFormat;

    /** доступ к базе данных
     * @see  DatabaseHelper
     * **/
    private DatabaseHelper db;


    public String getValue(){
        return value;
    }
    public String getValue(int id){
        Cursor product = db.getProductById(PRODUCTS_TABLE,Integer.toString(id));
        String return_value;
        if(product.moveToFirst()) {
            return_value =  product.getString(1);
            product.close();
        }else{
            return_value =  "";
        }
        return return_value;
    }

    private String getId(){
        Cursor product = db.getProduct(PRODUCTS_TABLE, VALUE, value);
        String return_value;
        if(product.moveToFirst()) {
            return_value =  product.getString(0);
            product.close();
        }else{
            return_value =  "";
        }
        return return_value;
    }
    public int getValueFormat(){
        return valueFormat;
    }

    /**
     * <p> Создает объект класса Product, класса DatabaseHelper
     * </p>
     * @param brText текстовое значение разбивается на <var>characters</var>, <var>type</var>,<var>partNumber</var>
     * @param context  текущие настройки приложения
      *                 @see Context
     * **/
    public Product (String brText,int brValue,Context context) {
        db = new DatabaseHelper(context, 0);
        value = brText;
        valueFormat= brValue;
    }

    public Product (Context context) {
        db = new DatabaseHelper(context, 0);
    }
    public Product (String brText,Context context) {
        value = brText;
        db = new DatabaseHelper(context, 0);
    }


    public SparseArray<ArrayList<String>> getSlotHistory(int slot) {
        SparseArray<ArrayList<String>> allProducts = new SparseArray<>();

        String where = DATA_TABLE_CURRENT_SLOT + " = " + slot +
                " AND " + DATA_TABLE_TIME_REPLACE + " IS NULL";
        Cursor product = db.queryWhere(DATA_TABLE, where);

        int number = 0;
        if (product.moveToFirst()) {
            do {

                ArrayList<String> current_product = new ArrayList<>();
                current_product.add(Integer.toString(number + 1));
                current_product.add(getValue(product.getInt(1)));

                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
                String value = format.format(product.getLong(3));
                current_product.add(value);

                allProducts.put(number, current_product);
                number++;

            } while (product.moveToNext());
            product.close();
        }
        return allProducts;

    }
    public SparseArray<ArrayList<String>> getProducts () {

        SparseArray<ArrayList<String>> allProducts = new SparseArray<>();

        Cursor data_table = db.getAll(PRODUCTS_TABLE);
        int number = 0;
        if(data_table.moveToFirst()){
            do{
                ArrayList<String> current_product = new ArrayList<>();
                current_product.add(Integer.toString(number+1));

                for (int count = 1;count<3;count++){
                    current_product.add(data_table.getString(count));
                }

                String where =DATA_TABLE_PRODUCT_ID+" = "+data_table.getString(0)+
                        " AND "+DATA_TABLE_TIME_REPLACE+" IS NULL";
                Cursor product_value = db.queryWhere(DATA_TABLE,where);

                String[] date_values = new String[2];

                if(product_value.moveToFirst()){
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.US);
                    date_values[0] = format.format(product_value.getLong(3));//
                    date_values[1] = NULL;

                }else{
                    for (int count = 0;count<2;count++){
                        date_values[count] = NULL;
                    }

                }
                current_product.addAll(Arrays.asList(date_values).subList(0, 2));

                allProducts.put(number,current_product);
                number++;

            }while (data_table.moveToNext());
            data_table.close();
        }
        return allProducts;
    }

    public SparseArray<ArrayList<String>> getProductHistory () {
        // данные для общей таблицы
        SparseArray<ArrayList<String>> allProducts = new SparseArray<>();
        String id = getId();
        if(!id.equals("")) {
            Cursor product = db.getProduct(DATA_TABLE, DATA_TABLE_PRODUCT_ID, id);
            int number = 0;
            if (product.moveToFirst()) {
                do {

                    ArrayList<String> current_product = new ArrayList<>();
                    current_product.add(Integer.toString(number + 1));
                    current_product.add(product.getString(2));

                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
                    for (int count = 3; count < 5; count++) {
                        String value = product.getLong(count) != 0 ? format.format(product.getLong(count)) : NULL;
                        current_product.add(value);
                    }
                    allProducts.put(number, current_product);
                    number++;

                } while (product.moveToNext());
                product.close();
            }
        }
        return allProducts;
    }

    public boolean unsnapProduct() {
        try {
            SQLiteDatabase cursor = db.getWritableDatabase();
            String id = getId();
            if(id.equals("")) {

                ContentValues updatedValues = new ContentValues();
                updatedValues.put(SLOT, -1);
                String where = ID + " = " + id;

                cursor.update(PRODUCTS_TABLE, updatedValues, where, null);

                updatedValues = new ContentValues();
                updatedValues.put(DATA_TABLE_TIME_REPLACE, new Date().getTime());
                where = DATA_TABLE_PRODUCT_ID + " = " + id + " AND "
                        + DATA_TABLE_TIME_REPLACE + " IS NULL";
                cursor.update(DATA_TABLE, updatedValues, where, null);

                cursor.close();
                return true;
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error during unsnapping", e);
        }

        return false;

    }

    public boolean checkSlotName(String value) {
        // проверяет на существование записи с именем partNumber

        Cursor cursor = db.getProduct(SLOTS_TABLE,SLOTS_TABLE_NAME,value);

        if (cursor.moveToFirst()) {;
            Log.d(TAG, "Barcode verified successful");
            cursor.close();
            return true;
        }

        return false;
    }


    public ProductValidityData checkProductAvailability(){
        Cursor product = db.getProduct(PRODUCTS_TABLE,VALUE,value);
        ProductValidityData return_value = new ProductValidityData();

        return_value.availability = product.moveToFirst();

        if(return_value.availability ) {
            return_value.slot = product.getInt(2);
            return_value.product = true;
        }else {
            return_value.product = !checkSlotName(value);
        }
        product.close();

        return return_value;
    }

    public boolean addProduct(int slot) {
        Cursor product = db.getProduct(PRODUCTS_TABLE, VALUE,value);

        try {
            SQLiteDatabase cursor = db.getWritableDatabase();

            if (!product.moveToFirst()) {
                ContentValues total_products = new ContentValues();
                total_products.put(VALUE, value);
                total_products.put(SLOT, slot);
                cursor.insert(PRODUCTS_TABLE, null, total_products);
                product.close();
                product = db.getProduct(PRODUCTS_TABLE, VALUE,value);
                product.moveToFirst();
            }else {
                ContentValues updatedValues = new ContentValues();
                updatedValues.put(SLOT, slot);
                String where = ID + "=" + product.getString(0);
                cursor.update(PRODUCTS_TABLE, updatedValues, where, null);
            }


            ContentValues own_product = new ContentValues();
            own_product.put(DATA_TABLE_PRODUCT_ID,product.getString(0));
            own_product.put(DATA_TABLE_TIME_ADDED, new Date().getTime());
            own_product.put(DATA_TABLE_CURRENT_SLOT, slot);
            cursor.insert(DATA_TABLE, null, own_product);

            product.close();
            cursor.close();
            return true;


        } catch (SQLiteException e) {
            Log.e(TAG, "Error during adding", e);
        }

        return false;

    }
}

