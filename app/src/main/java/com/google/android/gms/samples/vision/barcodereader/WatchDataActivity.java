package com.google.android.gms.samples.vision.barcodereader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.samples.vision.barcodereader.PrBarcode.Product;

import java.util.ArrayList;


public class WatchDataActivity extends Activity implements View.OnClickListener{

    private TableLayout watchTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_data);

        // Все объектам свойсво onClick
        findViewById(R.id.back).setOnClickListener(this);
        watchTable = (TableLayout)findViewById(R.id.tableLayout);


        if(getIntent().getBooleanExtra(ProductActivity.codeProductsList,false)){
            createAllProductsTable();
        }else if((getIntent().getStringExtra((ProductActivity.codeWatchProductHistory)) != null)){
            Product data = new Product(getIntent().getStringExtra((ProductActivity.codeWatchProductHistory)),this);
            createProductHistoryTable(data);
        }else if((getIntent().getIntExtra((ProductActivity.codeWatchSlotHistory),0) != 0)){
            createSlotHistoryTable((getIntent().getIntExtra((ProductActivity.codeWatchSlotHistory),0)));
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();
        }
    }

    public void createProductHistoryTable(Product data) {
        String[] values = {"№ п/п", "Ячейка №", "Добавлено", "Удалено"};
        SparseArray<ArrayList<String>> product = data.getProductHistory();

        TableRow tableRow = new TableRow(this);

        for (int count = 0; count < 4; count++) {
            String text = values[count];

            TextView productName = new TextView(this);//ячейка ряда в таблице
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.setMargins(0, 0, 20, 30);
            productName.setLayoutParams(params);

            productName.setTextSize(25);
            productName.setGravity(Gravity.CENTER);//устанавливаем ориентации
            productName.setText(text);
            tableRow.addView(productName);// добавляем ячейку в ряд
            tableRow.setBackgroundResource(R.color.trans_success_stroke_color);
        }
        watchTable.addView(tableRow);// добавляем ячейку в ряд

        for (int number = 0; number < product.size(); number++) {
            tableRow = new TableRow(this);

            for (int count = 0; count < 4; count++) {
                String text = product.get(number).get(count);
                TextView productData = setTextViewParams(text);//ячейка ряда в таблице
                tableRow.addView(productData);// добавляем ячейку в ряд
                tableRow.setBackgroundResource(R.color.fbutton_color_clouds);
            }
            watchTable.addView(tableRow,number+1);
        }

    }

    public void createSlotHistoryTable(Integer slot) {
        String[] values = {"№ п/п", "Ячейка №", "Добавлено"};
        SparseArray<ArrayList<String>> product = new Product(this).getSlotHistory(slot);

        TableRow tableRow = new TableRow(this);

        for (int count = 0; count < 3; count++) {
            String text = values[count];

            TextView productName = new TextView(this);//ячейка ряда в таблице
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.setMargins(0, 0, 20, 30);
            productName.setLayoutParams(params);

            productName.setTextSize(25);
            productName.setGravity(Gravity.CENTER);//устанавливаем ориентации
            productName.setText(text);
            tableRow.addView(productName);// добавляем ячейку в ряд
            tableRow.setBackgroundResource(R.color.trans_success_stroke_color);
        }
        watchTable.addView(tableRow);// добавляем ячейку в ряд

        for (int number = 0; number < product.size(); number++) {
            tableRow = new TableRow(this);

            for (int count = 0; count < 3; count++) {
                String text = product.get(number).get(count);
                TextView productData = setTextViewParams(text);//ячейка ряда в таблице
                tableRow.addView(productData);// добавляем ячейку в ряд
                tableRow.setBackgroundResource(R.color.fbutton_color_clouds);
            }
            watchTable.addView(tableRow,number+1);
        }

    }



    public TextView setTextViewParams(String text){
        TextView productData = new TextView(this);//ячейка ряда в таблице
        productData.setGravity(Gravity.CENTER);//устанавливаем ориентацию
        productData.setTextSize(20);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(0, 0, 35, 55);
        productData.setLayoutParams(params);

        productData.setText(text);

        return productData;
    }

    /**
     * Метод заполняет таблицу с характеристиками
     **/
    public void createAllProductsTable() {
        SparseArray<ArrayList<String>> product = new Product(this).getProducts();

        String[] values = {"  ", "Продукт: ", "Ячейка № ", "Добавлено: ", "Удалено: "};

        for (int number = 0; number < product.size(); number++) {
            // Ряд с номером и названием
            TableRow tableRow = new TableRow(this);

            for (int count = 0; count < 2; count++) {
                String text = values[count] + product.get(number).get(count);

                TextView productName = new TextView(this);//ячейка ряда в таблице
                productName.setTextSize(25);
                productName.setGravity(Gravity.START);//устанавливаем ориентацию


                if(count==1){
                    TableRow.LayoutParams params = new TableRow.LayoutParams();
                    params.span = 2;

                    productName.setLayoutParams(params);
                }


                productName.setText(text);
                tableRow.addView(productName);// добавляем ячейку в ряд
                tableRow.setBackgroundResource(R.color.trans_success_stroke_color);
            }
            watchTable.addView(tableRow, 2 * number);// добавляем ячейку в ряд

            // Ряд с значениями (номер слота, дата добавления, дата удаления)
            tableRow = new TableRow(this);

            if(!product.get(number).get(2).equals("-1")) {
                for (int count = 2; count < 5; count++) {
                    String text = values[count] + product.get(number).get(count);

                    TextView productData = setTextViewParams(text);//ячейка ряда в таблице

                    tableRow.addView(productData);// добавляем ячейку в ряд
                    tableRow.setBackgroundResource(R.color.fbutton_color_clouds);
                }
            }else {
                String text = "В данный момент товар не привязан к какой либо ячейке";
                TextView productData = setTextViewParams(text);//ячейка ряда в таблице

                TableRow.LayoutParams params = (TableRow.LayoutParams)productData.getLayoutParams();
                params.span = 3;
                productData.setLayoutParams(params);

                tableRow.addView(productData);// добавляем ячейку в ряд
                tableRow.setBackgroundResource(R.color.fbutton_color_clouds);
            }
            watchTable.addView(tableRow, 2 * number + 1);
        }
    }

}