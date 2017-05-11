package com.moe.utils;
import android.view.View;
import com.moe.dialog.SearchDialog;
import android.widget.TextView;
import com.moe.widget.ProgressBar;
import android.widget.ImageButton;
import com.moe.widget.ViewFlipper;
import com.moe.Mbrowser.R;
import de.greenrobot.event.EventBus;
import com.moe.fragment.MenuFragment;
import com.moe.fragment.WindowFragment;
import com.moe.widget.WebView;
import android.support.v4.content.LocalBroadcastManager;
import com.moe.bean.Message;
import android.support.design.widget.AppBarLayout;
import com.moe.database.BookMarks;
import com.moe.database.DataBase;
import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.widget.ImageView;
import com.moe.bean.MenuOptions;

public class ToolManager implements View.OnClickListener,ViewFlipper.OnChangeListener,WebView.OnStateListener
{

	private int index=0;
	private SearchDialog search;
	private TextView title;
	private ProgressBar pb;
	private ImageButton back,forward,home,win,menu,exit;
	private ImageView bookmark,refresh;
	private View toolbar;
	private ViewFlipper content;
	private static ToolManager tm;
	private AppBarLayout abl;
	private BookMarks bm;
	public static final int HOME = 0xff0007;
	private ToolManager(View v){
		search=new SearchDialog(v.getContext());
		bm=DataBase.getInstance(v.getContext());
		toolbar = v.findViewById(R.id.mainview_searchbar);
        toolbar.setOnClickListener(this);
        title = (TextView)toolbar.findViewById(R.id.mainview_title);
        pb=(ProgressBar)v.findViewById(R.id.mainview_progress);
        v.findViewById(R.id.mainview_bar_home).setOnClickListener(this);
        back=(ImageButton)v.findViewById(R.id.mainview_bar_pre);
        back.setOnClickListener(this);
        forward=(ImageButton)v.findViewById(R.id.mainview_bar_next);
        forward.setOnClickListener(this);
        win=(ImageButton)v.findViewById(R.id.mainview_bar_win);
        win.setOnClickListener(this);
        v.findViewById(R.id.mainview_bar_menu).setOnClickListener(this);
        Theme.registerTheme(v.findViewById(R.id.mainview_bar));
        refresh=(ImageButton)v.findViewById(R.id.mainview_refresh);
		refresh.setOnClickListener(this);
		home=(ImageButton)v.findViewById(R.id.mainview_bar_home);
		home.setOnClickListener(this);
		menu=(ImageButton)v.findViewById(R.id.mainview_bar_menu);
		menu.setOnClickListener(this);
		content=(ViewFlipper)v.findViewById(R.id.main_content);
		content.registerOnChangeListener(this);
		v.findViewById(R.id.mainview_popwin).setOnClickListener(this);
		abl=(AppBarLayout)v.findViewById(R.id.main_view_appbarlayout);
		bookmark=(ImageView)v.findViewById(R.id.mainview_fav);
		bookmark.setOnClickListener(this);
		exit=(ImageButton)v.findViewById(R.id.main_view_exit);
		exit.setOnClickListener(this);
	}

	public void refresh()
	{
		((WebView)content.getCurrentView()).reload();
	}

	public static void destroy()
	{
		tm=null;
	}
	public static void init(View v) throws IllegalAccessException{
		if(tm!=null)throw new IllegalAccessException("禁止多次初始化工具管理器"); 
		tm=new ToolManager(v);
	}
	public static ToolManager getInstance(){
		if(tm==null)throw new NullPointerException("工具管理器未初始化");
		return tm;
	}
	@Override
    public void onClick(View p1)
    {
        switch (p1.getId())
        {
            case R.id.mainview_searchbar:
				// String url="";
				EventBus.getDefault().post(MenuFragment.HIDE);
				EventBus.getDefault().post(WindowFragment.CLOSE);
				search.show(((WebView)content.getCurrentView()).getUrl());
                //搜索
                break;
            case R.id.mainview_bar_menu:
                EventBus.getDefault().post(MenuFragment.SHOW);
				abl.setExpanded(true,true);
                break;
            case R.id.mainview_popwin:
                //关闭popwin
                EventBus.getDefault().post(WindowFragment.CLOSE);
                abl.setExpanded(true,true);
				break;
            case R.id.mainview_bar_home:
                EventBus.getDefault().post(WindowFragment.CLOSE);
				EventBus.getDefault().post(HOME);
                //((WebView)content.getCurrentView()).loadUrl();
                break;
            case R.id.mainview_bar_pre:
                EventBus.getDefault().post(WindowFragment.CLOSE);
                ((WebView)content.getCurrentView()).goBack();
                break;
            case R.id.mainview_bar_next:
                EventBus.getDefault().post(WindowFragment.CLOSE);
                ((WebView)content.getCurrentView()).goForward();
                break;
            case R.id.mainview_bar_win:
                EventBus.getDefault().post(WindowFragment.OPEN);
                break;
			case R.id.mainview_refresh:
				WebView wv=(WebView)content.getCurrentView();
				if(wv.getState())
					wv.stopLoading();
					else
					wv.reload();
				break;
			case R.id.mainview_fav:
				String url=((WebView)content.getCurrentView()).getUrl();
				if(bm.isBookmark(url))
				{
					bm.deleteBookmark(new String[]{url});
					bookmark.setImageResource(R.drawable.ic_star_outline);
				}
				else if(url!=null&&url.length()>0){
					/*ByteArrayOutputStream baos=new ByteArrayOutputStream();
					Bitmap b=((WebView)content.getCurrentView()).getFavicon();
					if(b!=null)b.compress(Bitmap.CompressFormat.PNG,100,baos);
					bm.insertBookmark(url,((WebView)content.getCurrentView()).getTitle(),baos.toByteArray());
					try
					{
						baos.close();
					}
					catch (IOException e)
					{}*/
					bm.insertBookmark(url,((WebView)content.getCurrentView()).getTitle());
					bookmark.setImageResource(R.drawable.ic_star);
					}
				break;
			case R.id.main_view_exit:
				EventBus.getDefault().post(MenuOptions.EXIT);
				break;
        }
    }
	@Override
	public void onAdd(WebView vf, int index)
	{
		win.setImageBitmap(ImageDraw.squareImage(content.getChildCount()));
	}

	@Override
	public void onRemove(int index)
	{
		
		win.setImageBitmap(ImageDraw.squareImage(content.getChildCount()));
		
	}

	@Override
	public void onToggle(int index)
	{
		WebView wv=(WebView)content.getCurrentView();
		if(wv!=null)wv.setOnStateListener(null);
		wv=(WebView)content.getChildAt(index);
		if(wv==null)return;
		title.setText(wv.getTitle());
		if(wv.getState()){
			pb.setVisibility(pb.VISIBLE);
			pb.setProgress(wv.getProgress());
			refresh.setImageResource(R.drawable.ic_close);
		}else{
		pb.setVisibility(pb.GONE);
		refresh.setImageResource(R.drawable.menu_refresh);
		}
		checkButtonState(wv);
		wv.setOnStateListener(this);
		this.index=index;
	}
	@Override
	public void onProgress(int p2)
	{
		pb.setProgress(p2);
	}

	@Override
	public void onStart(String url)
	{
		pb.setProgress(0);
		pb.setVisibility(pb.VISIBLE);
		refresh.setImageResource(R.drawable.ic_close);
		title.setText(url);
		if(bm.isBookmark(url))bookmark.setImageResource(R.drawable.ic_star);
		else bookmark.setImageResource(R.drawable.ic_star_outline);
	}

	@Override
	public void onEnd(String url, String title)
	{
		refresh.setImageResource(R.drawable.menu_refresh);
		this.title.setText(title);
		pb.setVisibility(pb.GONE);
		checkButtonState((WebView)content.getCurrentView());
		
	}

	@Override
	public void onReceiverTitle(String title)
	{
		this.title.setText(title);
		EventBus.getDefault().post(Message.obitMessage(0,index));
		checkButtonState((WebView)content.getCurrentView());
	}
	
	public void checkButtonState(WebView wv){
		if(wv.canGoBack())
			back.setImageResource(R.drawable.bar_back);
			else
			back.setImageResource(R.drawable.bar_back_normal);
		if(wv.canGoForward())
			forward.setImageResource(R.drawable.bar_next);
			else
			forward.setImageResource(R.drawable.bar_next_normal);
	if(bm.isBookmark(wv.getUrl()))bookmark.setImageResource(R.drawable.ic_star);
	else bookmark.setImageResource(R.drawable.ic_star_outline);
	}
}