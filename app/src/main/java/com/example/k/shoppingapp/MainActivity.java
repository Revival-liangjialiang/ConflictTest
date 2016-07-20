package com.example.k.shoppingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.k.shoppingapp.Adapter.MyRecyclerAdapter;
import com.example.k.shoppingapp.Fragment.Male_zone_fragment;
import com.example.k.shoppingapp.Fragment.Women_zone_fragment;
import com.example.k.shoppingapp.Fragment.principal_sheet_fragment;
import com.example.k.shoppingapp.Other.ImageSize;
import com.example.k.shoppingapp.Other.SystemBarTintManager;
import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.adapter.LoopPagerAdapter;
import com.jude.rollviewpager.hintview.IconHintView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity{
    ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.titlec);// 通知栏颜色
        }
        ViewGroup mContentView = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 使其不为系统 View 预留空间.
            ViewCompat.setFitsSystemWindows(mChildView, true);
        }
        //以下是

    }

    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
    public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        //设置ViewPager碎片里面的View
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Women_zone_fragment();
                case 1:
                    return new principal_sheet_fragment();
                case 2:
                    return new Male_zone_fragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
        //设置TabLayout里的按钮Text
        @Override
        public CharSequence getPageTitle(int position) {
            Log.i("ok","position="+position);
            switch (position) {
                case 0:
                    return "第一个";
                case 1:
                    return "第二个";
                case 2:
                    return "第二个";
                default:
                    return "";
            }
        }
    }
}
