package com.pp.gridpager.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pp.gridpager.R;

import java.util.List;

public class GridPagerAdapter extends RecyclerView.Adapter<GridPagerAdapter.ViewHolder> {


    private final int row;
    private final int colum;
    private final int pageCount ;
    private final int orientation;
    private final Context mContext;
    private final  List<String> data ;

    public GridPagerAdapter(@Nullable Context context, int orientation, int colum, int row , List<String> data) {
        this.mContext = context;
        this.orientation = orientation;
        this.colum = colum;
        this.row = row;
        this.data = data ;
        this.pageCount = colum * row;
        final int emptyCount = pageCount - data.size() % pageCount;
        for(int i = 0 ; i < emptyCount ; i++){
            this.data.add("null");
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_snaphelp, viewGroup, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (GridLayoutManager.VERTICAL == orientation) {
            layoutParams.height = viewGroup.getHeight() / row;
        } else {
            layoutParams.width = viewGroup.getWidth() / colum;
        }

        view.setLayoutParams(layoutParams);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {

        int startFirst = i / pageCount ; //起始位置
        int pageStartFirst =  i % pageCount ; // 页面相对位置

        int x = pageStartFirst / row  ;
        int y = pageStartFirst % row  ;

        final int realPos = startFirst * pageCount + y * colum + x ;
        final String text = data.get(realPos) + " [" + i + "]" ;



        viewHolder.tv_content.setText(text);

        viewHolder.tv_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "click :" + data.get(realPos),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_content = itemView.findViewById(R.id.item_tv_content);
        }
    }
}