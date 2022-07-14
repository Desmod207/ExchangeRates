package com.example.exchangerates;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String URL_ADDRESS = "https://www.cbr-xml-daily.ru/daily_json.js";

    public Spinner spinner;
    public ArrayList<Currency> currencies;
    public EditText inputEdit;
    public TextView outputText;
    Button updateButton;
    ArrayList<String> currencyNames;
    boolean requiredUpdate = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Для удобства пользователя убераем фокус с EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Ищем загруженый файл с данными о валютах
        FileInputStream fis;
        String[] arrFiles = fileList();
        String fileName = null;
        if (arrFiles.length != 0) {
            // Смотрим дату создания файла, если текущая дата отличается
            // загружаем его снова, а устаревший файл удаляем
            fileName = arrFiles[0];
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            if (!fileName.equalsIgnoreCase(dateFormat.format(date))) {
                requiredUpdate = true;
                deleteFile(fileName);
            }
        } else {
            // Если файл не найден выставляем флаг на обновление файла с курсами валют
            requiredUpdate = true;
        }

        // Смотрим флаг обновления и если требуется обновляем файл
        if (requiredUpdate) {
            fis = downloadFile();
        } else {
            try {
                fis = openFileInput(fileName);
            }
            catch (FileNotFoundException e) {
                fis = downloadFile();
            }
        }
        ViewGroup linLayout = findViewById(R.id.linear_layout);
        // Делаем так чтобы клавиатура ввода скрывалась при касании за её пределами
        linLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (inputEdit.isFocused()) {
                    Rect outRect = new Rect();
                    inputEdit.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                        inputEdit.clearFocus();
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
            return false;
        });
        // Парсим данные из загруженного файла
        currencies = JSONCurrencyParser.parse(fis);
        currencyNames = new ArrayList<>();
        for (Currency currency : currencies) {
            currencyNames.add(currency.getCharCode());
            TextView textView = new TextView(this);
            String str = currency.getCharCode() + "\n" + currency.getNominal() + " " + currency.getName() + " - " + currency.getValue() + " рублей";
            textView.setText(str);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            textView.setPadding(0,10,0,10);
            linLayout.addView(textView);
        }
        try {
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currencyNames);

        spinner = findViewById(R.id.currency_list);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                convert();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        inputEdit = findViewById(R.id.converter_input);
        inputEdit.clearFocus();
        inputEdit.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                convert();
            }
        });

        outputText = findViewById(R.id.converter_output);

        updateButton = findViewById(R.id.update_button);
        updateButton.setOnClickListener(view -> {
            downloadFile();
            this.recreate();
        });
    }

    // Конвертируем введёные в inputEdit данные и записываем их в outputText
    private void convert() {
        String str = String.valueOf(spinner.getSelectedItem());
        float convertedSum = 0;
        for (Currency currency : currencies) {
            if (currency.getCharCode().equalsIgnoreCase(str)) {
                if (!inputEdit.getText().toString().equalsIgnoreCase("")) {
                    convertedSum = currency.getValue() / currency.getNominal();
                    convertedSum *= Float.parseFloat(inputEdit.getText().toString());
                }
            }
        }
        outputText.setText(String.format("%s %s", convertedSum, getResources().getString(R.string.rub)));
    }

    // Качаем файл, задаём имя файлу в виде текущей даты, затем возвращаем поток для чтения из загруженного файла
    private FileInputStream downloadFile() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String fileName = dateFormat.format(date);
        final FileInputStream[] fis = {null};
        Runnable task = () -> {
            try {
                URL url = new URL(MainActivity.URL_ADDRESS);
                BufferedInputStream bis = new BufferedInputStream(url.openStream());
                FileOutputStream fileOutput = openFileOutput(fileName, MODE_PRIVATE);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = bis.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, length);
                }
                fis[0] = openFileInput(fileName);
                fileOutput.close();
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();

        return fis[0];
    }
}