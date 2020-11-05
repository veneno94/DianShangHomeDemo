package com.dianshang.demo.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.dianshang.demo.R;
import com.dianshang.demo.activity.SecondLevelActivity;
import com.dianshang.demo.adapter.BaseRecyclerAdapter;
import com.dianshang.demo.adapter.SmartViewHolder;
import com.dianshang.demo.recyclerview.HeadFootRecyclerView;
import com.dianshang.demo.refresh.ClassicsHeader;
import com.dianshang.demo.util.StatusBarUtil;
import com.scwang.smart.refresh.header.TwoLevelHeader;
import com.scwang.smart.refresh.header.listener.OnTwoLevelListener;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.scwang.smart.refresh.layout.simple.SimpleMultiListener;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;

import java.util.Arrays;
import java.util.Collection;

import static com.dianshang.demo.R.mipmap.bg_image_5;
import static com.dianshang.demo.R.mipmap.image_weibo_home_1;

/**
 * 使用示例-嵌套滚动-整体
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements AdapterView.OnItemClickListener, OnRefreshListener {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private SmartPagerAdapter mAdapter;
    private AppBarLayout appBarLayout;

    private Handler handler = new Handler();

    private boolean isFlag;


//    private BaseRecyclerAdapter<Item> mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        final View floor = root.findViewById(R.id.second_floor);
        final View toolbar = root.findViewById(R.id.toolbar);
        final TwoLevelHeader header = root.findViewById(R.id.header);
        final ClassicsHeader classics = root.findViewById(R.id.classics);


        final RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
        refreshLayout.setOnMultiListener(new SimpleMultiListener() {

            @Override
            public void onHeaderMoving(RefreshHeader header, boolean isDragging, float percent, int offset, int headerHeight, int maxDragHeight) {
                toolbar.setAlpha(1 - Math.min(percent, 1));
                floor.setTranslationY(Math.min(offset - floor.getHeight() + toolbar.getHeight(), refreshLayout.getLayout().getHeight() - floor.getHeight()));
            }

            @Override
            public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {
                if (oldState == RefreshState.TwoLevel) {
                    Log.i("feeeerffff", "TwoLevel");
                } else if (oldState == RefreshState.TwoLevelFinish) {
                    Log.i("feeeerffff", "TwoLevelFinish");
                } else if (oldState == RefreshState.TwoLevelReleased) {
                    Log.i("feeeerffff", "TwoLevelReleased");
                    header.finishTwoLevel();
                }
            }
        });

        refreshLayout.setDragRate(0.8f);
        header.setMaxRage(8f);

        /**
         * 设置是否开启二级刷新
         * @param enabled 是否开启
         * @return TwoLevelHeader
         */
        header.setEnableTwoLevel(true);
//        floor.setVisibility(View.GONE);
        classics.setHeaderTextColor(R.color.aaa);

        /**
         * 是否禁止在二极状态时上滑关闭状态回到初态
         * @param enabled 是否启用
         * @return TwoLevelHeader
         */
//        header.setEnablePullToCloseTwoLevel(false);

        /*
         * 主动打开二楼
         */
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                header.openTwoLevel(true);
                isFlag = !isFlag;
                scrollToTop(isFlag);
            }
        });

        header.setOnTwoLevelListener(new OnTwoLevelListener() {
            @Override
            public boolean onTwoLevel(@NonNull RefreshLayout refreshLayout) {
                Toast.makeText(getContext(), "触发二楼事件", Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getActivity(), SecondLevelActivity.class));
//                        getActivity().overridePendingTransition(R.anim.slide_bottom_in, 0);
                    }
                }, 500);
                return true;//true 将会展开二楼状态 false 关闭刷新
            }
        });


//        refreshLayout.setRefreshHeader(new ClassicsHeader(getActivity()));
//        refreshLayout.setHeaderHeight(60);

        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setOnRefreshListener(this);


        Banner banner = root.findViewById(R.id.banner);
        banner.setImageLoader(new BannerImageLoader());
        banner.setImages(Arrays.asList(bg_image_5,image_weibo_home_1));
        banner.start();

        appBarLayout = root.findViewById(R.id.appbar);
        mTabLayout = root.findViewById(R.id.tableLayout);
        mViewPager = root.findViewById(R.id.viewPager);
        mViewPager.setAdapter(mAdapter = new SmartPagerAdapter(Item.values()));
        mTabLayout.setupWithViewPager(mViewPager, true);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new BaseRecyclerAdapter<Integer>(loadModels(), R.layout.item_image) {
            @Override
            protected void onBindViewHolder(SmartViewHolder holder, Integer model, int position) {
                holder.image(R.id.imageView, model);
            }
        });


        //状态栏透明和间距处理
        StatusBarUtil.immersive(getActivity());
        StatusBarUtil.setMargin(getActivity(), root.findViewById(R.id.classics));
        StatusBarUtil.setPaddingSmart(getActivity(), root.findViewById(R.id.toolbar));
        StatusBarUtil.setPaddingSmart(getActivity(), root.findViewById(R.id.container));


    }


    public void scrollToTop(boolean flag) {
        CoordinatorLayout.Behavior behavior =
                ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).getBehavior();
        if (behavior instanceof AppBarLayout.Behavior) {
            AppBarLayout.Behavior appBarLayoutBehavior = (AppBarLayout.Behavior) behavior;
            if (flag) {
                appBarLayoutBehavior.setTopAndBottomOffset(0); //快速滑动到顶部
            } else {
                int hight = appBarLayout.getHeight() - mTabLayout.getHeight();
                appBarLayoutBehavior.setTopAndBottomOffset(-hight);//快速滑动实现吸顶效果
            }
        }
    }

    private Collection<Integer> loadModels() {
        return Arrays.asList(R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.finishRefresh();
                refreshLayout.resetNoMoreData();//setNoMoreData(false);

                for (SmartFragment fragment : mAdapter.fragments) {
                    if (fragment != null) {
                        fragment.onRefresh();
                    }
                }

            }
        }, 300);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    private class BannerImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource((Integer) path);
        }
    }

    public enum Item {
        NestedInner(R.string.app_name2, SmartFragment.class),
        NestedOuter(R.string.app_name3, SmartFragment.class),
        NestedOuter2(R.string.app_name4, SmartFragment.class),
        NestedOuter3(R.string.app_name5, SmartFragment.class),
        NestedOuter4(R.string.app_name6, SmartFragment.class),
        NestedOuter5(R.string.app_name7, SmartFragment.class),
        ;
        public int nameId;
        public Class<? extends Fragment> clazz;

        Item(@StringRes int nameId, Class<? extends Fragment> clazz) {
            this.nameId = nameId;
            this.clazz = clazz;
        }
    }


    private class SmartPagerAdapter extends FragmentStatePagerAdapter {

        private final Item[] items;
        private final SmartFragment[] fragments;

        SmartPagerAdapter(Item... items) {
            super(getChildFragmentManager());
            this.items = items;
            this.fragments = new SmartFragment[items.length];
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(items[position].nameId);
        }

        @Override
        public Fragment getItem(int position) {
            if (fragments[position] == null) {
                fragments[position] = new SmartFragment();
            }
            return fragments[position];
        }
    }

    public static class SmartFragment extends Fragment {

        private HeadFootRecyclerView mRecyclerView;
        private BaseRecyclerAdapter<Integer> mAdapter;
        private Handler handler = new Handler();


        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return mRecyclerView = new HeadFootRecyclerView(inflater.getContext());
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mRecyclerView.setStaggeredGridLayout(2);
            mRecyclerView.setRecylcerViewAdapter(mAdapter = new BaseRecyclerAdapter<Integer>(loadModels(), R.layout.item_image) {
                @Override
                protected void onBindViewHolder(SmartViewHolder holder, Integer model, int position) {
                    holder.image(R.id.imageView, model);
                }
            });

            mRecyclerView.setOnPullLoadMoreListener(new HeadFootRecyclerView.PullLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.loadMore(loadModels());
                            if (mAdapter.getItemCount() > 15) {
                                mRecyclerView.setNoMore();
                            } else {
                                mRecyclerView.setPullLoadMoreCompleted();
                            }
                        }
                    }, 2000);
                }
            });

        }


        private Collection<Integer> loadModels() {
            return Arrays.asList(R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2, R.mipmap.image_weibo_home_1, R.mipmap.image_weibo_home_2);
        }


        public void onRefresh() {
            if (mAdapter != null && mRecyclerView != null) {
                mAdapter.refresh(loadModels());
                mRecyclerView.onRefresh();
                mRecyclerView.setPullLoadMoreCompleted();

            }
        }

    }
}
