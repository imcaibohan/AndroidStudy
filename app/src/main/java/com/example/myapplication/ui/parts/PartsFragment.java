package com.example.myapplication.ui.parts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.utils.GaodeConstant;
import com.example.myapplication.databinding.FragmentPartsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PartsFragment extends Fragment {

    private FragmentPartsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PartsViewModel partsViewModel =
                new ViewModelProvider(this).get(PartsViewModel.class);

        binding = FragmentPartsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        final TextView textView = binding.textParts;
        partsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("提示")
                        .setMessage("这是一个提示框")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 在这里编写点击确定按钮后的逻辑
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Response weather = getWeather();
                                        boolean isUploadSuccessful = weather.isSuccessful(); // 替换为实际的文件路径
                                        Message message = Message.obtain(); // 创建消息对象
                                        if (isUploadSuccessful) {
                                            message.what = 1;
                                            message.obj = weather.body(); // 设置消息的what字段为1，表示上传成功
                                        } else {
                                            message.what = 0; // 设置消息的what字段为0，表示上传失败
                                        }
                                        handler.sendMessage(message); // 通过Handler发送消息到主线程
                                    }
                                });
                                thread.start(); // 启动线程
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 在这里编写点击取消按钮后的逻辑
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 当用户点击搜索或按下回车键时触发
                if (query != null && query.length() > 0) {
                    JsonArray goodsArray = getGoodsArray(query);
                    if (goodsArray != null) {
                        if (goodsArray.size() > 0){
                            showAlertDialog(goodsArray,1); // 显示弹窗，这里需要实现showAlertDialog方法来创建和显示弹窗

                        }else {
                            Toast.makeText(getActivity(), query + "未入库", Toast.LENGTH_SHORT).show();

                        }

                    }else {
                        Toast.makeText(getActivity(), query + "未入库", Toast.LENGTH_SHORT).show();
                    }
                } else { // 如果用户没有输入轮胎型号，可以提示用户输入或什么都不做
                    Toast.makeText(getActivity(), "请输入要查找的东西", Toast.LENGTH_SHORT).show();
                }
                return true; // 表示处理了这个事件
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 当用户在搜索框中输入内容时触发
                return false; // 表示没有处理这个事件
            }
        });
        return view;
    }


    private JsonArray getGoodsArray(String query) {
        try {
            JsonArray goodsList = readJsonFromAssets();
            for (JsonElement jsonElement : goodsList) {
                if (jsonElement.getAsJsonObject().get("p-value").getAsString().equals(query)
                        || jsonElement.getAsJsonObject().get("p-value-simple").getAsString().equals(query)
                ) {
                    return jsonElement.getAsJsonObject().get("children").getAsJsonArray();
                }
            }
            return null;
        } catch (Exception ignore) {
            return null;
        }
    };

    private void showAlertDialog(final JsonArray goodsArray, final int page) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("总计 " + goodsArray.size() + "当前第 " + page);
        JsonObject goods = goodsArray.get(page - 1).getAsJsonObject();
        builder.setMessage("品牌：" + goods.get("brand") + "\n" + "规格：" + goods.get("specs") + "\n" + "进货价：￥" + goods.get("price") + "\n" + "进货日期：" + goods.get("date"));
        final int[] finalPage = {page};
        builder.setPositiveButton("使用", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 实现使用逻辑，减少库存
                goodsArray.remove(page - 1);
                //如果还有则才弹窗
                if (goodsArray.size() > 0){
                    showAlertDialog(goodsArray, 1);
                }
            }
        });
        builder.setNeutralButton("下一个", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finalPage[0]++;
                if (finalPage[0] > goodsArray.size()){
                    //如果翻到界外去了 就回到第一页
                    showAlertDialog(goodsArray, 1);
                }else {
                    showAlertDialog(goodsArray, finalPage[0]);
                }
            }
        });
        builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    public JsonArray readJsonFromAssets() {
        Context context = requireActivity().getApplicationContext();
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Toast.makeText(getActivity(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
            } else if (msg.what == 0) {
                Toast.makeText(getActivity(), "文件上传失败！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Response getWeather() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(String.format(GaodeConstant.WEATHER_API,GaodeConstant.AIR_FRIEND_WEB))
                .build();
        final Response[] result = {null};
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // 处理响应数据
                    System.out.println(responseBody);
                    result[0] = response;
                } else {
                    // 处理错误响应
                }
            }
        });
        return result[0]; // 示例中返回true表示文件上传成功，你可以根据实际情况进行修改和扩展逻辑。
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}