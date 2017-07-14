package com.google.android.gms.samples.vision.barcodereader;

// системный класс для  реализации диалоговых окон
import android.app.AlertDialog;
// системный класс для управления intent (в данном случае управление переходами между activity)
import android.content.DialogInterface;
import android.content.Intent;
// системный класс для доступа к ресурсам приложения
// ситемный класс для реализации включения|паузы|выключения Activity
import android.os.Bundle;
// системный класс для реализации логов приложения

// системный класс, используемый для отображения информации в окне приложения и взаимодействия с пользователем
import android.app.Activity;
// системный класс, используемый для определения местоположения объекта класса View в окне приложения
import android.view.ContextThemeWrapper;
// системный класс, для заполнения объекта класса View xml данными
// системный класс, для присвоения данных об элементах окна объектам класса
import android.view.View;
// системный класс, для присвоения данных об окне объектам класса
// класс, объект которого служит контейнером для хранения данных списка ( объект класса ArrayList) в объекты Listview


// всплывающее окно

// класс для работы с информациией о статусе, считанного ранее баркода
// класс для работы с продуктами
// класс для работы со сравнением
// класс для работы с данными в сравнении ( тип товаров в сравнении и их названия (модели)
// класс для работы Parcel с объектами класса Product
// класс, объект которого хранит всю информацию о баркодах


/**
 * В этой Activity хранятся и обрабатываются все текущие данные приложения.
 * С помощью данной Activity происходит взаимодействие со всеми остальным Activity.
 * **/
public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "BarcodeMain";// название процесса для логов
    public static final String codeScan = "scan intent";
    public static final String codeScanBack = "scan intent back";




    public static final String codeToScannerStart = "Start to scan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int id = getIntent().getIntExtra(MainActivity.codeScanBack,0);
        if(id != 0){
            if(id != R.string.barcode_failure){
                createWatchSlotsProductDialog(id);
            }else {
                ProductActivity.makeToast(id, this);
            }
        }
         // Все объектам свойсво onClick
        findViewById(R.id.read_barcode).setOnClickListener(this);


    }

    protected void createWatchSlotsProductDialog(final int slotName) {
        String text = "Вы выбрали ячейку № "+slotName+
                "\n Желаете просмотреть товары, привязанные к ячейке?";

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(
                this,R.style.Theme_AppCompat_Light_Dialog_Alert));
        builder.setTitle("Просмотр содержимого ячейки")
                .setMessage(ProductActivity
                                .setForeignColor(text, 20, 20+String.valueOf(slotName).length(),
                                        R.color.fbutton_default_shadow_color,getResources()
                                )
                )

                .setPositiveButton("Да",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                                Intent intent = new Intent(MainActivity.this, WatchDataActivity.class);
                                intent.putExtra(ProductActivity.codeWatchSlotHistory,slotName);
                                startActivity(intent);
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
    }


    @Override
    public void onClick(View v) {
            if(v.getId() ==R.id.read_barcode ) {
                // launch barcode activity.
                // привязываем intent к ProductActivity
                Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                intent.putExtra(codeScan,codeToScannerStart);
                startActivity(intent);

            }else if(v.getId() == R.id.watch_all_products){

                Intent intent = new Intent(MainActivity.this, WatchDataActivity.class);
                intent.putExtra(ProductActivity.codeProductsList,true);
                startActivity(intent);
            }

    }



}
