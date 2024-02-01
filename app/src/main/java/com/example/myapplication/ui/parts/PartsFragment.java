package com.example.myapplication.ui.parts;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.ui.goods.GoodsDialogFragment;
import com.example.myapplication.utils.ApiConstant;
import com.example.myapplication.databinding.FragmentPartsBinding;
import com.example.myapplication.utils.FileUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

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
//                showAlertDialogGoodsAdd();
                DialogFragment newFragment = new GoodsDialogFragment();
                newFragment.show(requireActivity().getSupportFragmentManager(), "dialog");
            }
        });

        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 当用户点击搜索或按下回车键时触发
                if (query != null && query.length() > 0) {
                    JsonArray goodsList = FileUtils.readJsonFromAssets(requireActivity());
                    JsonArray goodsChildrenArray = null;
                    for (JsonElement jsonElement : goodsList) {
                        if (jsonElement.getAsJsonObject().get("p-value").getAsString().equals(query)
                                || jsonElement.getAsJsonObject().get("p-value-simple").getAsString().equals(query)
                        ) {
                            goodsChildrenArray = jsonElement.getAsJsonObject().get("children").getAsJsonArray();
                        }
                    }
                    if (goodsChildrenArray != null) {
                        if (goodsChildrenArray.size() > 0){
                            showAlertDialogGoodsDetail(getActivity(),goodsList,goodsChildrenArray,1); // 显示弹窗，这里需要实现showAlertDialog方法来创建和显示弹窗
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

        Button weatherButton = view.findViewById(R.id.weather_button);
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里编写点击确定按钮后的逻辑
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getWeather();
                    }
                });
                thread.start(); // 启动线程
            }
        });

        Button newsButton = view.findViewById(R.id.news_button);
        newsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里编写点击确定按钮后的逻辑
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getNews();
                    }
                });
                thread.start(); // 启动线程
            }
        });
        return view;
    }

    //库存==============================================================


    private void showAlertDialogGoodsDetail(FragmentActivity activity, JsonArray goodsList, final JsonArray goodsChildrenArray, final int page) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("总计 " + goodsChildrenArray.size() + "当前第 " + page);
        JsonObject goods = goodsChildrenArray.get(page - 1).getAsJsonObject();
        builder.setMessage("品牌：" + goods.get("brand") + "\n" + "规格：" + goods.get("specs") + "\n" + "进货价：￥" + goods.get("price") + "\n" + "进货日期：" + goods.get("date"));
        final int[] finalPage = {page};
        builder.setPositiveButton("使用", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 实现使用逻辑，减少库存
                goodsChildrenArray.remove(page - 1);
                // 重写goods.xml文件
                FileUtils.writeJsonToInternalStorage(activity,goodsList);
                //如果还有则才弹窗
                if (goodsChildrenArray.size() > 0){
                    showAlertDialogGoodsDetail(getActivity(), goodsList, goodsChildrenArray, 1);
                }
            }
        });
        builder.setNeutralButton("下一个", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finalPage[0]++;
                if (finalPage[0] > goodsChildrenArray.size()){
                    //如果翻到界外去了 就回到第一页
                    showAlertDialogGoodsDetail(getActivity(), goodsList, goodsChildrenArray, 1);
                }else {
                    showAlertDialogGoodsDetail(getActivity(), goodsList, goodsChildrenArray, finalPage[0]);
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

    //天气=========================================================================================

    @SuppressLint("HandlerLeak")
    private final Handler weatherHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
//                Toast.makeText(getActivity(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("今日天气");
//                JsonObject goods = goodsArray.get(page - 1).getAsJsonObject();
                JsonObject body = new Gson().fromJson(msg.obj.toString(), JsonObject.class);
                JsonArray lives = body.get("lives").getAsJsonArray();
                JsonObject weatherJson = lives.get(0).getAsJsonObject();
                builder.setMessage("所在地 " + weatherJson.get("province") + "-" + weatherJson.get("city") + "\n"
                    + "天气 " + weatherJson.get("weather") + "\n"
                        + "温度 " + weatherJson.get("temperature") + "℃" +  "\n"
                        + "风向 " + weatherJson.get("winddirection") + "\n"
                        + "风力指数 " + weatherJson.get("windpower") + "\n"
                        + "湿度 " + weatherJson.get("humidity") + "%" + "\n"
                        + "更新时间 " + weatherJson.get("reporttime")
                );
//                builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                });
                builder.show();
            } else if (msg.what == 0) {
                Toast.makeText(getActivity(), "请检查网络", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void getWeather() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(String.format(ApiConstant.WEATHER_API, ApiConstant.AIR_FRIEND_WEB))
                .build();
        final Response[] result = {null};
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 处理错误响应
                Toast.makeText(getActivity(), "请检查网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // 处理响应数据
                    System.out.println(responseBody);
                    Message message = Message.obtain(); // 创建消息对象
                    if (response.isSuccessful()) {
                        message.what = 1;
                        message.obj = responseBody; // 设置消息的what字段为1，表示上传成功
                    } else {
                        message.what = 0; // 设置消息的what字段为0，表示上传失败
                    }
                    weatherHandler.sendMessage(message); // 通过Handler发送消息到主线程
                } else {
                    // 处理错误响应
                }
            }
        });
    }

    //新闻=========================================================================================

    @SuppressLint("HandlerLeak")
    private final Handler newsHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                JsonObject body = new Gson().fromJson(msg.obj.toString(), JsonObject.class);
                JsonObject data = body.get("data").getAsJsonObject();
                showAlertDialogNews(data,-1);
            } else if (msg.what == 0) {
                Toast.makeText(getActivity(), "请检查网络", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void showAlertDialogNews(JsonObject data, int page){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(data.get("date").getAsString() + " Daily News");

        JsonArray newsArrays = data.get("news").getAsJsonArray();
        String sentence = data.get("weiyu").getAsString();
        if (page == -1){
            builder.setMessage(sentence);
        }else {
            builder.setMessage(newsArrays.get(page).getAsString());
        }

        final int[] finalPage = {page};
//        builder.setNeutralButton("关闭", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        });
        builder.setPositiveButton("下一个", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finalPage[0]++;
                if (finalPage[0] > newsArrays.size() - 1){
                    //如果翻到界外去了 就回到第一页
                    showAlertDialogNews(data, 0);
                }else {
                    showAlertDialogNews(data, finalPage[0]);
                }
            }
        });

        builder.setNegativeButton("上一个", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finalPage[0]--;
                if (finalPage[0] < 0){
                    //如果翻到界外去了 就回到第一页
                    showAlertDialogNews(data, newsArrays.size() - 1);
                }else {
                    showAlertDialogNews(data, finalPage[0]);
                }
            }
        });

        builder.show();
    }

    private void getNews() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(String.format(ApiConstant.NEWS_API, ApiConstant.ALAPI_KEY))
                .build();
        final Response[] result = {null};
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 处理错误响应
                Toast.makeText(getActivity(), "请检查网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // 处理响应数据
                    Message message = Message.obtain(); // 创建消息对象
                    if (response.isSuccessful()) {
                        message.what = 1;
                        message.obj = responseBody; // 设置消息的what字段为1，表示上传成功
                    } else {
                        message.what = 0; // 设置消息的what字段为0，表示上传失败
                    }
                    newsHandler.sendMessage(message); // 通过Handler发送消息到主线程
                } else {
                    // 处理错误响应
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}