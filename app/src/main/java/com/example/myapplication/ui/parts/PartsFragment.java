package com.example.myapplication.ui.parts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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


    // 检查是否存在和库存情况的逻辑，这里需要你根据实际情况来实现这个方法
    private JsonArray getGoodsArray(String query) { /* 实现检查的逻辑 */
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

    private void showAlertDialog(JsonArray goodsArray, int page) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("总计 " + goodsArray.size() + "当前第 " + page);
        JsonObject goods = goodsArray.get(page - 1).getAsJsonObject();
        builder.setMessage("品牌：" + goods.get("brand") + "\n" + "规格：" + goods.get("specs") + "\n" + "进货价：" + goods.get("price") + "\n" + "进货日期：" + goods.get("date"));
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

    public static class Goods {
        private String key;

        private String value;

        private Long stock;



        public Long getStock() {
            return stock;
        }

        public void setStock(Long stock) {
            this.stock = stock;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Goods(JsonObject jsonObject) {
            this.key = jsonObject.get("key").getAsString();
            this.value = jsonObject.get("value").getAsString();
            this.stock = jsonObject.get("stock").getAsLong();
        }
    }

    public JsonArray readJsonFromAssets() {
        Context context = requireActivity().getApplicationContext();
        JsonArray jsonArray = new JsonArray();
        BufferedReader bufferedReader = null;
        try {
            InputStream is = context.getAssets().open("goods.json");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}