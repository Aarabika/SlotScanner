package com.google.android.gms.samples.vision.barcodereader;

// системный класс для управления intent (в данном случае управление переходами между activity)
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
// ситемный класс для реализации включения|паузы|выключения Activity
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
// системный класс для реализации логов приложения
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

// системный класс, используемый для отображения информации в окне приложения и взаимодействия с пользователем
import android.app.Activity;
// системный класс, используемый для определения местоположения объекта класса View в окне приложения
import android.view.ContextThemeWrapper;
import android.view.Gravity;
// системный класс, для присвоения данных об элементах окна объектам класса
import android.view.View;
import android.view.ViewGroup;


import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// класс для работы Parcel с объектами класса Product
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.barcodereader.PrBarcode.Product;
import com.google.android.gms.samples.vision.barcodereader.PrBarcode.ProductValidityData;
import com.google.android.gms.vision.barcode.Barcode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.ExecutionException;



/**
 * В этой Activity выводятся таблица со сравнением и навигационные кнопки. (очистить, вернуться назад в MainActivity)
 * напрямую связана с MainActivity
 * **/
public class ProductActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "CompareProducts";// название процесса для логов

    // названия соответсвующих parcel
    public static final String productInfo = "Product total information";

    // контрольное число для получения данных из BarcodeCaptureActivity
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final int RC_HANDLE_INTERNET_PERM = 3;


    public static final String codeProductsList = "watch products list";
    public static final String codeWatchProductHistory = "watch product's history";
    public static final String codeWatchSlotHistory = "watch slot's history";

    private TextView name;
    private Product product = null;
    private static boolean checkFirstUsage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_signin);

        overridePendingTransition(android.R.anim.cycle_interpolator, android.R.anim.slide_out_right);

        //Получение Parcel из MainActivity
        product = getIntent().getParcelableExtra(productInfo);//текущее сравнение

        // Проверяет разрешено ли использование камеры, если нет, то запрашивает разрешение
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            requestInternetPermission();
        }

        if (getIntent().getStringExtra(MainActivity.codeScan) != null){
            // привязываем intent к BarcodeCaptureActivity

            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            startActivityForResult(intent, RC_BARCODE_CAPTURE);// запускаем Activity, для получения результатов
            checkFirstUsage = true;
        }


        name = (TextView) findViewById(R.id.product_name);
        name.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.snap_to_slot) {
            // launch barcode activity.
            // привязываем intent к BarcodeCaptureActivity
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            startActivityForResult(intent, RC_BARCODE_CAPTURE);// запускаем Activity, для получения результатов
        }
        if (v.getId() == R.id.watch_history) {
            Intent intent = new Intent(ProductActivity.this, WatchDataActivity.class);
            intent.putExtra(ProductActivity.codeWatchProductHistory,product.getValue());
            startActivity(intent);
        }
        if (v.getId() == R.id.unsnap_from_slot) {
            if(product.unsnapProduct()){
                makeToast(R.string.success_unsnap);
                Button snap_to_slot = (Button) findViewById(R.id.unsnap_from_slot);
                snap_to_slot.setId(R.id.snap_to_slot);
                snap_to_slot.setText(R.string.snap_to_slot);
                TextView snap_description = (TextView) findViewById(R.id.snap_description);
                snap_description.setText(R.string.snap_description_no_snap);
                prepareData();



            } else {
                makeToast(R.string.bad_unsnapped);
            }
        }
    }
    /**
     * Метод очищает объекты <class>TextView,ListView</class>
     **/
    public static void clearForm(ViewGroup group) {
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {

            View view = group.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setText("");
            }
            //Если нужно будет удалаять сложные конструкции раскомитить это
            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                clearForm((ViewGroup) view);
        }
    }
    /**
     Посылает запрос на разрешение использования камеры, метод используется, если при инициализации камеры нет разрешения
     */
    private void requestInternetPermission() {
        Log.w(TAG, "Internet permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.INTERNET};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.INTERNET)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_INTERNET_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_INTERNET_PERM);
            }
        };

        Snackbar.make(findViewById(R.id.window),R.string.permission_internet_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }


    public static void makeToast(int id,Context param){
        // создаем объект класса высплывающее окно
        Toast toast = Toast.makeText(param,id,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM,0,50);// устанавливаем положение в окне
        toast.show();
    }

    public void makeToast(int id){
        // создаем объект класса высплывающее окно
        Toast toast = Toast.makeText(this,id,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM,0,50);// устанавливаем положение в окне
        toast.show();
    }

    @NonNull
    public static SpannableStringBuilder setForeignColor(String text, int begin, int end, int colorId, Resources resources){
        SpannableStringBuilder content = new SpannableStringBuilder(text);
        ForegroundColorSpan style = new ForegroundColorSpan(resources.getColor(colorId));
        content.setSpan(style, begin,end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return content;
    }

    protected void createProductSnapDialog(final int slotName) {
        String text = "Вы выбрали ячейку № "+slotName;

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(
                this,R.style.Theme_AppCompat_Light_Dialog_Alert));
        builder.setTitle("Привязка товара к ячейке")
                .setMessage(setForeignColor(text,20,text.length(),
                            R.color.fbutton_default_shadow_color,getResources()
                        )
                )
                .setPositiveButton("Привязать",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                                if(product.addProduct(slotName)){
                                    makeToast(R.string.success_added);
                                    prepareData();
                                }else {
                                    makeToast(R.string.bad_added);
                                }
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

    private static String executeAsyncGettingTitle(String url) {
        try {
            return new AsyncRequest().execute(url).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG,"Filed to execute async getting title",e);
        }
        return url;
    }
    private void returnToMainActivity(int toastValueId){
        Intent intent = new Intent(ProductActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.codeScanBack,toastValueId);
        startActivity(intent);
        finishAffinity();
    }

    private void prepareData(){
            ProductValidityData validityData = product.checkProductAvailability();

            if(validityData.product) {
                if (validityData.availability && validityData.slot != -1) {
                    TextView snap_description = (TextView) findViewById(R.id.snap_description);
                    String text = getResources().getString(R.string.snap_description_snap, validityData.slot);
                    snap_description.setText(setForeignColor(text, 41, text.length(),
                                R.color.text_color,getResources()
                            )
                    );

                    Button snap_to_slot = (Button) findViewById(R.id.snap_to_slot);
                    snap_to_slot.setId(R.id.unsnap_from_slot);
                    snap_to_slot.setText(R.string.unsnap_from_slot);
                }
                String text;// Текст поля your_choice (Вы выбрали)

                if (product.getValueFormat() == Barcode.URL) {
                    text = getResources().getString(R.string.your_choice, executeAsyncGettingTitle(product.getValue()));
                    Spannable s = (Spannable) Html.fromHtml(text);

                    for (URLSpan u : s.getSpans(0, s.length(), URLSpan.class)) {
                        s.setSpan(new UnderlineSpan() {
                            public void updateDrawState(TextPaint tp) {
                                tp.setUnderlineText(false);
                            }
                        }, s.getSpanStart(u), s.getSpanEnd(u), 0);
                    }
                    name.setText(s);
                } else {
                    text = getResources().getString(R.string.your_choice, product.getValue());
                    name.setText(setForeignColor(text, 11, text.length(),
                                R.color.fbutton_color_carrot,getResources()
                            )
                    );
                }
            }else {
                returnToMainActivity(Integer.parseInt(product.getValue()));
            }
    }

    //Использовать метод executeAsyncGettingTitle, так как android не разрешает использовать сетевые
    //запросы в главном потоке
    public static String getUrlTitle(String url){
        try {
            Document doc  = Jsoup.connect(url).get();

            url = "<a href='"+url+"'>"+doc.title()+"</a>";
        } catch (IOException e) {
            Log.e(TAG,"No connection");
            return "<a href='"+url+"'>"+url+"</a>";
        }
        return url;
    }
    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        // проверяем корректность срабатывания BarcodeCaptureActivity
        if (requestCode == RC_BARCODE_CAPTURE) {
            // проверяем корректность отсканированного баркода
            if (resultCode == CommonStatusCodes.SUCCESS) {
                // проверяем пришли ли данные из BarcodeCaptureActivity
                if (data != null) {
                    checkFirstUsage = false;
                    // получаем информацию для создания объекта класса Barcode
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.productCode);
                    Log.d(TAG, "Barcode value: " + barcode.displayValue +" Format: "+barcode.valueFormat);
                    if(product == null) {
                    product = new Product(barcode.displayValue,barcode.valueFormat,ProductActivity.this);
                    prepareData();
                    }else if(product.checkSlotName(barcode.displayValue)) {
                        createProductSnapDialog(Integer.parseInt(barcode.displayValue));
                    }else {
                        makeToast(R.string.incorrect);
                    }

                } else if(!checkFirstUsage){
                        makeToast(R.string.barcode_failure);
                        Log.d(TAG, "No barcode captured, intent data is null");
                    }else{
                        returnToMainActivity(R.string.barcode_failure);
                    }
            } else {
                Log.d(TAG, "Can't read barcode" + CommonStatusCodes.getStatusCodeString(resultCode));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
class AsyncRequest extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... url) {
        return ProductActivity.getUrlTitle(url[0]);
    }

}

