package com.lz.sectionrecycleview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lz.sectionrecycleview.section.SectionParameters;
import com.lz.sectionrecycleview.section.SectionedRecyclerViewAdapter;
import com.lz.sectionrecycleview.section.StatelessSection;

import java.util.ArrayList;
import java.util.List;



public class Example4Fragment extends Fragment {

    private SectionedRecyclerViewAdapter sectionAdapter;
    String LoadMoretag;
    boolean isload =true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ex4, container, false);

        sectionAdapter = new SectionedRecyclerViewAdapter();


        for(char alphabet = 'A'; alphabet <= 'Z';alphabet++) {
            List<String> contacts = getContactsWithLetter(alphabet);

            if (contacts.size() > 0) {
                sectionAdapter.addSection(new ExpandableContactsSection(String.valueOf(alphabet), contacts));
            }
        }
        LoadMoretag  = sectionAdapter.addSection(new loadMoreSection("加载中...."));
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));






        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                Log.d("onScrollStateChanged", "onScrollStateChanged: -------------------------");
                //当前RecyclerView显示出来的最后一个的item的position
                int lastPosition = -1;
                //当前状态为停止滑动状态SCROLL_STATE_IDLE时
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if(layoutManager instanceof GridLayoutManager){
                        //通过LayoutManager找到当前显示的最后的item的position
                        lastPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    }else if(layoutManager instanceof LinearLayoutManager){
                        lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    }else if(layoutManager instanceof StaggeredGridLayoutManager){
                        //因为StaggeredGridLayoutManager的特殊性可能导致最后显示的item存在多个，所以这里取到的是一个数组
                        //得到这个数组后再取到数组中position值最大的那个就是最后显示的position值了
                        int[] lastPositions = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                        ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(lastPositions);
                        lastPosition = findMax(lastPositions);
                    }
                    //时判断界面显示的最后item的position是否等于itemCount总数-1也就是最后一个item的position
                    //如果相等则说明已经滑动到最后了
                    Log.d("onScrollStateChanged", "lastPosition: -------------------------"+lastPosition);
                    Log.d("onScrollStateChanged", "recyclerView.getLayoutManager().getItemCount(): -------------------------"+recyclerView.getLayoutManager().getItemCount());
                    if(lastPosition ==recyclerView.getLayoutManager().getItemCount()-1){
                        Toast.makeText(Example4Fragment.this.getActivity(), "滑动到底了", Toast.LENGTH_SHORT).show();
                        isload=false;
                        for(char alphabet = 'A'; alphabet <= 'C';alphabet++) {
                            List<String> contacts = getContactsWithLetter(alphabet);

                            if (contacts.size() > 0) {
                                sectionAdapter.addSection(new ExpandableContactsSection(String.valueOf(alphabet), contacts));
                            }
                        }
                        sectionAdapter.removeSection(LoadMoretag);
                        sectionAdapter.notifyItemRemoved(recyclerView.getLayoutManager().getItemCount());
                        recyclerView.scrollToPosition(lastPosition+1);
                        LoadMoretag  = sectionAdapter.addSection(new loadMoreSection("加载中...."));
                        isload=true;
                    }
                }
            }
        });
        recyclerView.setAdapter(sectionAdapter);
        return view;
    }


    //找到数组中的最大值
    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = ((AppCompatActivity) getActivity());
            if (activity.getSupportActionBar() != null)
                activity.getSupportActionBar().setTitle(R.string.nav_example4);
        }
    }

    private List<String> getContactsWithLetter(char letter) {
        List<String> contacts = new ArrayList<>();

        for (String contact : getResources().getStringArray(R.array.names)) {
            if (contact.charAt(0) == letter) {
                contacts.add(contact);
            }
        }

        return contacts;
    }







    private class  loadMoreSection extends  StatelessSection{

      private String StrloadMore;

        public loadMoreSection(String loadMore) {
            super(new SectionParameters.Builder(R.layout.section_loadmore)
                    .build());

            this.StrloadMore = loadMore;
        }

        @Override
        public int getContentItemsTotal() {
            return isload ? 1:0;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {

            return new LoadMoreViewHolder(view);

        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {

            final LoadMoreViewHolder loadMoreHolder = (LoadMoreViewHolder) holder;
            loadMoreHolder.tvItem.setText(StrloadMore);
            loadMoreHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(Example4Fragment.this.getActivity(), "dianjile", Toast.LENGTH_SHORT).show();
                   /* isload = false;
                    sectionAdapter.removeSection(LoadMoretag);*/
                }
            });

        }
    }


    private class ExpandableContactsSection extends StatelessSection {

        String title;
        List<String> list;
        boolean expanded = true;

        ExpandableContactsSection(String title, List<String> list) {
            super(new SectionParameters.Builder(R.layout.section_ex4_item)
                    .headerResourceId(R.layout.section_ex4_header)
                    .build());

            this.title = title;
            this.list = list;

        }

        @Override
        public int getContentItemsTotal() {
            return expanded? list.size() : 0;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            String name = list.get(position);

            itemHolder.tvItem.setText(name);
            itemHolder.imgItem.setImageResource(name.hashCode() % 2 == 0 ? R.mipmap.ic_face_black_48dp : R.mipmap.ic_tag_faces_black_48dp);

            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), String.format("Clicked on position #%s of Section %s", sectionAdapter.getPositionInSection(itemHolder.getAdapterPosition()), title), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);

            headerHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expanded = !expanded;
                    headerHolder.imgArrow.setImageResource(
                            expanded ? R.mipmap.ic_keyboard_arrow_up_black_18dp : R.mipmap.ic_keyboard_arrow_down_black_18dp
                    );
                    sectionAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView tvTitle;
        private final ImageView imgArrow;

        HeaderViewHolder(View view) {
            super(view);

            rootView = view;
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            imgArrow = (ImageView) view.findViewById(R.id.imgArrow);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final ImageView imgItem;
        private final TextView tvItem;

        ItemViewHolder(View view) {
            super(view);

            rootView = view;
            imgItem = (ImageView) view.findViewById(R.id.imgItem);
            tvItem = (TextView) view.findViewById(R.id.tvItem);
        }
    }



    private class LoadMoreViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView tvItem;

        LoadMoreViewHolder(View view) {
            super(view);
            rootView = view;
            tvItem = (TextView) view.findViewById(R.id.loadmore);
        }
    }

}
