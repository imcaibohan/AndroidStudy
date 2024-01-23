package com.example.myapplicationstudysimple;

/*
  android.os.Bundle: 用于保存和恢复 Fragment 的状态。
  android.view.LayoutInflater: 用于将 XML 布局文件转换为 View 对象。
  android.view.ViewGroup: 用于存放 View 对象。
  androidx.fragment.app.Fragment: AndroidX 版本的 Fragment。
  androidx.navigation.fragment.NavHostFragment: 用于在 Fragment 之间导航。
  com.example.myapplicationstudysimple.databinding.FragmentFirstBinding: 数据绑定类，用于绑定 XML 布局和 Java/Kotlin 代码。
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplicationstudysimple.databinding.FragmentFirstBinding;

/**
 * 这个类继承了 androidx.fragment.app.Fragment，表示它是一个 Fragment。
 * 这是一个简单的 Android Fragment，其中有一个按钮用于导航到另一个 Fragment。使用了 Android 的 Data Binding 和 Navigation Components 技术。
 */
public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    /**
     * 当 Fragment 的视图被创建时，这个方法会被调用。
     * 使用 FragmentFirstBinding.inflate() 方法来初始化数据绑定。
     * binding.getRoot() 返回绑定后的视图根对象。
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    /**
     * 当 Fragment 的视图已经创建并且与 Activity 的视图树关联时，这个方法会被调用。
     * 为名为 "buttonFirst" 的按钮设置点击监听器。当按钮被点击时，使用 Navigation Components 从当前 Fragment（FirstFragment）导航到另一个 Fragment（SecondFragment）。
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    /**
     * 当 Fragment 的视图被销毁时，这个方法会被调用。
     * 将 binding 设置为 null，释放资源。
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}