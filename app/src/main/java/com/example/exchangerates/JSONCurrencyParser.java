package com.example.exchangerates;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JSONCurrencyParser {

    public static ArrayList<Currency> parse(FileInputStream fileInputStream) {
        ArrayList<Currency> currencies = new ArrayList<>();
        try {
            String jsonText = readText(fileInputStream);
            JSONObject jsonRoot = new JSONObject(jsonText);

            // Получаем наименования всех валют в виде масива строк
            JSONObject jsonValute = jsonRoot.getJSONObject("Valute");
            JSONArray jsonArray = jsonValute.names();
            String[] currencyNames = new String[jsonArray.length()];
            for(int i=0;i < jsonArray.length();i++) {
                currencyNames[i] = jsonArray.getString(i);
            }
            // Заполняем ArrayList данными на основе полученых наименований валют
            for (String currencyName : currencyNames) {
                Currency currency = new Currency();
                JSONObject jsonCurrency = jsonValute.getJSONObject(currencyName);
                currency.setId(jsonCurrency.getString("ID"));
                currency.setNumCode(jsonCurrency.getInt("NumCode"));
                currency.setCharCode(jsonCurrency.getString("CharCode"));
                currency.setNominal(jsonCurrency.getInt("Nominal"));
                currency.setName(jsonCurrency.getString("Name"));
                currency.setValue((float) jsonCurrency.getDouble("Value"));
                currency.setPrevious((float) jsonCurrency.getDouble("Previous"));
                currencies.add(currency);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currencies;
    }

private static String readText(FileInputStream fileInputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
        StringBuilder sb = new StringBuilder();
        String s;
        while((s = br.readLine()) != null) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }
}
