package com.example.myapplication.ui.goods;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;
import com.example.myapplication.utils.FileUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class GoodsDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_form_in_constraintlayout, null); // 加载对话框布局。
        builder.setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 在这里处理保存逻辑。您可以通过以下方式获取EditText的值：
                        EditText detailInput = view.findViewById(R.id.keyEditText); // 获取详情输入框的值。
                        String key = detailInput.getText().toString().trim(); // 获取文本值。
                        // 在这里执行保存操作，例如保存到数据库或进行其他处理。
                        //先查有没有p-key存在的情况
                        JsonArray goods = FileUtils.readJsonFromAssets(requireActivity());
                        boolean exist = false;
                        for (JsonElement good : goods) {
                            if (good.getAsJsonObject().get("p-key").getAsString().equals(key)){
                                exist = true;
                                //更新处理
                                Toast.makeText(getActivity(), "请检查网络", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        if (!exist){
                            //新增处理
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 在这里处理取消逻辑。您可以根据需要执行相应的操作。
                    }
                });
        return builder.create(); // 创建对话框。
    }
}
