package com.moe.fragment;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import java.util.ArrayList;
import com.moe.adapter.WinListAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import com.moe.widget.ViewFlipper;
import android.widget.LinearLayout;
import android.view.Gravity;
import com.moe.Mbrowser.R;
import android.widget.TextView;
import com.moe.bean.WindowEvent;
import de.greenrobot.event.EventBus;
import android.widget.*;
import com.moe.widget.*;
import com.moe.utils.ToolManager;
import com.moe.bean.Message;
import de.greenrobot.event.Subscribe;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.TypedValue;
import com.moe.utils.CustomDecoration;

public class WindowFragment extends FragmentPop implements WinListAdapter.OnItemClickListener,ViewFlipper.OnChangeListener
,View.OnClickListener
{

	public static final int CLOSE=0xff0001;
	public static final int OPEN=0XFF0004;
	private ViewFlipper vf;
	private RecyclerView rv;
	
	
    @Override
    public void onAdd(WebView vf, int index)
    {
		if(wla!=null)
        wla.notifyDataSetChanged();
    }

    @Override
    public void onRemove(int index)
 {
        wla.notifyDataSetChanged();
    }

    @Override
    public void onToggle(int index)
    {
		if(wla!=null){
        wla.selected(index);
		}
    }

private WinListAdapter wla;
    public void setViewFlipper(ViewFlipper content)
    {
        vf=content;
		vf.registerOnChangeListener(this);
            }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        
        View v=inflater.inflate(R.layout.window_view,container,false);
        rv=(RecyclerView)v.findViewById(R.id.windowview_list);
        LinearLayoutManager llm= new LinearLayoutManager(getActivity(),1,false);
        llm.setAutoMeasureEnabled(true);
		rv.setHasFixedSize(false);
        rv.setLayoutManager(llm);
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(wla=new WinListAdapter(getActivity(),vf));
        rv.setItemAnimator(new DefaultItemAnimator());
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        v.findViewById(R.id.windowview_add).setOnClickListener(this);
		ItemTouchHelper ith=new ItemTouchHelper(new Callback());
		ith.attachToRecyclerView(rv);
		rv.setBackgroundResource(R.color.window_background);
		((ViewGroup)v).getChildAt(1).setBackgroundResource(R.color.window_background);
		int width=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10,getResources().getDisplayMetrics());
		rv.setPadding(width,width,width,0);
		//v.setPadding(0,0,0,0);
		rv.addItemDecoration(new CustomDecoration((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5,getResources().getDisplayMetrics()),0x00000000));
              return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        EventBus.getDefault().register(this);
        super.onActivityCreated(savedInstanceState);
        wla.setOnItemClickListener(this);
        //初始化数据加监听
		wla.selected(vf.getDisplayedChild());
       }

    @Override
    public void onClick(View p1)
    {
        switch(p1.getId()){
            case R.id.windowview_add:
                EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_NEW_WINDOW));
                break;
        }
    }

	@Override
	public void onDetach()
	{
		EventBus.getDefault().unregister(this);
		super.onDetach();
	}

    
    @Override
    public void onItemClick(int index)
    {
        vf.removeViewAt(index);
    }


    @Override
    public boolean onBackPressed()
    {
        // TODO: Implement this method
        return false;
    }
	@Subscribe
    public void refresh(Message msg){
		if(msg.what==0)
			wla.notifyItemChanged(msg.data);
	}
	class Callback extends ItemTouchHelper.Callback
	{

		@Override
		public int getMovementFlags(RecyclerView p1, RecyclerView.ViewHolder p2)
		{
			return makeMovementFlags(0,ItemTouchHelper.START|ItemTouchHelper.END);
		}

		@Override
		public boolean onMove(RecyclerView p1, RecyclerView.ViewHolder p2, RecyclerView.ViewHolder p3)
		{
			// 不支持排序
			return false;
		}

		@Override
		public void onSwiped(RecyclerView.ViewHolder p1, int p2)
		{
			vf.removeViewAt(p1.getPosition());
		}

		@Override
		public boolean isItemViewSwipeEnabled()
		{
			return true;
		}
		
		
	}
}