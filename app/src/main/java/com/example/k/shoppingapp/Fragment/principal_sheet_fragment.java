package com.example.k.shoppingapp.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.k.shoppingapp.Adapter.MyRecyclerAdapter;
import com.example.k.shoppingapp.MainActivity;
import com.example.k.shoppingapp.Other.ImageSize;
import com.example.k.shoppingapp.Other.ImageURLs;
import com.example.k.shoppingapp.R;
import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.adapter.LoopPagerAdapter;
import com.jude.rollviewpager.hintview.IconHintView;

import java.util.HashMap;

/**
 * Created by k on 2016/7/19.
 */
public class principal_sheet_fragment extends Fragment implements View.OnClickListener {
    //列数等于3
    private int columsCount = 3;
    private RecyclerView recyclerView;
    //高速缓存的大小
    private int cacheSize;
    //跨度宽
    private int spanWidth;
    //缓存
    private LruCache<String, Bitmap> cache;
    private HashMap<String, ImageSize> sizeHashMap = new HashMap<String, ImageSize>();
    //初始页面大小
    private int initpageSize = 25;
    //刷新大小
    private int refreshSize = 10;
    //项计数
    private int itemCount = 0;
    //所有已加载的
    private boolean allLoaded;
    //没有更多的
    private boolean noMore = false;
    private Context context;
    private MyRecyclerAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    //请求队列
    private RequestQueue requestqueue;
    //任务数
    private int taskCount = 0;
    //磁盘缓存大小，这里是50B*1024*1024,5M
    private int diskCacheSize = 50 * 1024 * 1024;
    Button nvzhuang, nanzhuang, nvxie, nanxie, kuzi, maozi, diannao, shouji, shipin, qinglvzhuang;
    private RollPagerView mRollViewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.principal_sheet_layout, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRollViewPager = (RollPagerView) getActivity().findViewById(R.id.roll_view_pager);
        mRollViewPager.setAnimationDurtion(500);
        mRollViewPager.setAdapter(new TestLoopAdapter(mRollViewPager));
        mRollViewPager.setHintView(new IconHintView(getActivity(), R.drawable.point_focus, R.drawable.point_normal));
        nvzhuang = (Button) getActivity().findViewById(R.id.nvzhuang);
        nanzhuang = (Button) getActivity().findViewById(R.id.nanzhuang);
        nvxie = (Button) getActivity().findViewById(R.id.nvxie);
        nanxie = (Button) getActivity().findViewById(R.id.nanxie);
        kuzi = (Button) getActivity().findViewById(R.id.kuzi);
        maozi = (Button) getActivity().findViewById(R.id.maozi);
        diannao = (Button) getActivity().findViewById(R.id.diannao);
        shouji = (Button) getActivity().findViewById(R.id.shouji);
        shipin = (Button) getActivity().findViewById(R.id.shipin);
        qinglvzhuang = (Button) getActivity().findViewById(R.id.qinglvzhuang);
        nvzhuang.setOnClickListener(this);
        nanzhuang.setOnClickListener(this);
        nvxie.setOnClickListener(this);
        nanxie.setOnClickListener(this);
        kuzi.setOnClickListener(this);
        maozi.setOnClickListener(this);
        diannao.setOnClickListener(this);
        shouji.setOnClickListener(this);
        shipin.setOnClickListener(this);
        qinglvzhuang.setOnClickListener(this);
        try {
            context = getContext();
            recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerview);
            //有固定的大小
            recyclerView.setHasFixedSize(true);
            //设置布局方式为StaggeredGridLayout，垂直排列列数为3
            layoutManager = new StaggeredGridLayoutManager(columsCount, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            //获取每列宽度
            WindowManager wm = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
            //得到默认的显示
            Display display = wm.getDefaultDisplay();
            //display.getWidth():得到屏幕的宽度像素值/3
            spanWidth = display.getWidth() / columsCount;
            //初始化Volley RequestQueue实例
            requestqueue = Volley.newRequestQueue(context);
            //设置内存缓存
            setLruCache();
            //初始化图片缓存
            refreshLruCache(initpageSize);
            adapter = new MyRecyclerAdapter(context, itemCount, requestqueue, spanWidth);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new MyRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    Toast.makeText(context, "you clicked " + position, Toast.LENGTH_SHORT).show();
                }
            });
            adapter.setOnItemLongClickListener(new MyRecyclerAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClick(View v, int position) {
                    Toast.makeText(context, "LongClick", Toast.LENGTH_SHORT).show();
                }
            });
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    //判断是否停止滚动
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        //判断当前加载是否完成
                        if (allLoaded) {
                            //得到每一列最后一个可见的元素的Position
                            int[] lastvisibalItem = layoutManager.findLastVisibleItemPositions(null);
                            int lastposition;
                            if (columsCount != 1) {
                                lastposition = Math.max(lastvisibalItem[0], lastvisibalItem[1]);
                                for (int i = 2; i < columsCount; i++) {
                                    //获取整个视图可见元素中Position的最大值
                                    lastposition = Math.max(lastposition, lastvisibalItem[i]);
                                }
                            } else {
                                lastposition = lastvisibalItem[0];
                            }
                            if ((lastposition + 1) == itemCount) {
                                //当最后一个可见元素的Position与加载的元素总数相等时，判断滑到底部，更新缓存、加载更多，
                                //为什么加11为不是10呢，这是因为lastposition要比实际项数少一，它是数组形式表示的数，从0开始，所以多加一，以消除误差
                                if ((lastposition + 11) <= ImageURLs.imageUrls.length) {
                                    //当还剩余十个以上元素待加载时，加载10个元素
                                    refreshLruCache(refreshSize);
                                    Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (!noMore) {
                                        //当剩余元素不足十个时，加载剩余元素并提示
                                        int remaining = ImageURLs.imageUrls.length - lastposition - 1;
                                        refreshLruCache(remaining);
                                        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
                                        //没有更多图片
                                        noMore = true;
                                    } else {
                                        //没有更多图片时提示
                                        Toast.makeText(context, "No more pictrues", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }catch(Exception e){
            Log.i("ok","有错误！");
        }
    }

    private class TestLoopAdapter extends LoopPagerAdapter {
        public int[] imgs = {
                R.drawable.a,
                R.drawable.b,
                R.drawable.w,
                R.drawable.d,
                R.drawable.h
        };

        public TestLoopAdapter(RollPagerView viewPager) {
            super(viewPager);
        }

        @Override
        public View getView(ViewGroup container, int position) {
            ImageView view = new ImageView(container.getContext());
            view.setId(position);
            view.setOnClickListener(principal_sheet_fragment.this);
            view.setImageResource(imgs[position]);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return view;
        }

        @Override
        public int getRealCount() {
            return imgs.length;
        }

    }

    //图片点击事件
    public void onClick(View v) {
        switch (v.getId()) {
            case 0:
                Toast.makeText(getActivity(), "1", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(getActivity(), "2", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(getActivity(), "3", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(getActivity(), "4", Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(getActivity(), "5", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nvzhuang:
                Toast.makeText(getActivity(), "女装", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nanzhuang:
                Toast.makeText(getActivity(), "男装", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nvxie:
                Toast.makeText(getActivity(), "女鞋", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nanxie:
                Toast.makeText(getActivity(), "男鞋", Toast.LENGTH_SHORT).show();
                break;
            case R.id.kuzi:
                Toast.makeText(getActivity(), "裤子", Toast.LENGTH_SHORT).show();
                break;
            case R.id.maozi:
                Toast.makeText(getActivity(), "帽子", Toast.LENGTH_SHORT).show();
                break;
            case R.id.diannao:
                Toast.makeText(getActivity(), "电脑", Toast.LENGTH_SHORT).show();
                break;
            case R.id.shouji:
                Toast.makeText(getActivity(), "手机", Toast.LENGTH_SHORT).show();
                break;
            case R.id.shipin:
                Toast.makeText(getActivity(), "食品", Toast.LENGTH_SHORT).show();
                break;
            case R.id.qinglvzhuang:
                Toast.makeText(getActivity(), "情侣装", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
    private void refreshLruCache(final int refreshNum) {
        //加载状态设置为未完成
        allLoaded = false;
        //refreshNum:一次需要下载的数量
        if (refreshNum == 0) {
            allLoaded = true;
        }
        //itemCount：初始值为0
        for (int i = itemCount; i < itemCount + refreshNum; i++) {
            final int finalI = i;
            requestqueue.add(new ImageRequest(ImageURLs.imageUrls[i], new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    //将返回的Bitmap加入内存缓存
                    cache.put(ImageURLs.imageUrls[finalI], response);
                    ImageSize imageSize = new ImageSize(response.getWidth(), response.getHeight());
                    sizeHashMap.put(ImageURLs.imageUrls[finalI], imageSize);
                    //所有任务完成后将缓存传入Adapter并更新视图
                    taskCount++;
                    if (taskCount == refreshNum) {
                        adapter.setLruCache(cache);
                        adapter.setSizeHashMap(sizeHashMap);
                        //更新元素个数
                        itemCount = itemCount + refreshNum;
                        adapter.setItemCount(itemCount);
                        adapter.notifyDataSetChanged();
                        taskCount = 0;
                        //加载状态设置为全部完成
                        allLoaded = true;
                    }
                }
            }, spanWidth, 0, ImageView.ScaleType.CENTER_CROP, null, null));
        }

    }

    private void setLruCache() {
        //maxMemory()返回本进程的最大内存，以字节（B）为单位
        int maxCacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //动用进程的六分之一内存
        cacheSize = maxCacheSize / 5;
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //getByteCount()：返回位图所占内存大小，以字节（B）为单位，除以1024等于?KB
                return value.getByteCount() / 1024;
            }
        };
    }
}