package com.example.myapplication.utils;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;

public class FileUtils {
    public static JsonArray readJsonFromAssets(FragmentActivity fragmentActivity) {
        Context context = fragmentActivity.getApplicationContext();
        JsonArray jsonArray = new JsonArray();
        BufferedReader bufferedReader = null;
        try {
            InputStream is = context.getAssets().open("goods.txt");
            Reader reader = new InputStreamReader(is);
            bufferedReader = new BufferedReader(reader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            jsonArray = new Gson().fromJson(stringBuilder.toString(), JsonArray.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    public static void writeJsonToInternalStorage(FragmentActivity fragmentActivity, JsonArray jsonArray) {
        Context context = fragmentActivity.getApplicationContext();
        FileOutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            outputStream = context.openFileOutput("goods1.txt", Context.MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(outputStream);
            // 将JsonArray转换为String

            objectOutputStream.writeObject(jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
