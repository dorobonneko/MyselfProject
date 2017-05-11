package com.moe.fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import com.moe.Mbrowser.HomeActivity;
import java.util.ArrayList;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.DefaultItemAnimator;
import com.moe.adapter.IconAdapter;
import com.moe.bean.SubSiteBean;
import android.widget.FrameLayout;
import android.support.v4.widget.NestedScrollView;
import android.view.MotionEvent;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import com.moe.utils.Theme;
import android.graphics.drawable.Drawable;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import android.widget.TextView;
import java.util.LinkedList;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.widget.LinearLayout;
import android.support.v4.app.FragmentTransaction;
import com.moe.widget.ViewFlipper;
import com.moe.widget.ProgressBar;
import com.moe.dialog.SearchDialog;
import android.support.design.widget.CoordinatorLayout;
import com.moe.bean.WindowEvent;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import com.moe.widget.*;
import android.util.*;
import com.moe.utils.*;
import android.os.Message;
import com.moe.dialog.AddDialog;
import com.moe.database.HomePage;
import com.moe.database.DataBase;

public class MainFragment extends Fragment implements FragmentPop.OnHideListener,AddDialog.OnAddListener
{
	private HomePage hp;
	private AddDialog ad;
    private ViewGroup pop;
    private ViewFlipper content,menutool;
	private FragmentPop current,menu=new MenuFragment(),window=new WindowFragment();
	private boolean isfull=false;
	public void setPadding(boolean p0)
	{
		isfull=p0;
		if(pop!=null){
			if(p0)
				pop.setPadding(0,0,0,(int)getResources().getDimension(R.dimen.actionBarSize));
				else
			pop.setPadding(0,0,0,getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"))+(int)getResources().getDimension(R.dimen.actionBarSize));
		}
	}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v= inflater.inflate(R.layout.main_view, null);
		try
		{
			ToolManager.init(v);
		}
		catch (IllegalAccessException e)
		{}
        content = (ViewFlipper)v.findViewById(R.id.main_content);
		menutool = (ViewFlipper)v.findViewById(R.id.mainview_ViewFlipper);
		pop=(ViewGroup)v.findViewById(R.id.mainview_popwin);
        Theme.registerTheme(v.findViewById(R.id.mainview_searchbar));
                return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
		ad=new AddDialog(getActivity());
        EventBus.getDefault().register(this);
        super.onActivityCreated(savedInstanceState);
		setPadding(isfull);
        pop.setLayoutParams(new CoordinatorLayout.LayoutParams(getActivity().getWindowManager().getDefaultDisplay().getWidth(),getActivity().getWindowManager().getDefaultDisplay().getHeight()));
        //注册fragment隐藏监听
        menu.setOnHideListener(this);
        window.setOnHideListener(this);
        ((WindowFragment)window).setViewFlipper(content);
        menutool.setInAnimation(getActivity(),R.anim.bottom_up);
		menutool.setOutAnimation(getActivity(),R.anim.up_up);
        Theme.updateTheme(0xff66ffcc);
        //默认启动一个空白窗口
        openNewWindow();
       LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(new TextQuery(),new IntentFilter("com.moe.search"));
	  ad.setOnAddLostener(this);
	  hp=DataBase.getInstance(getActivity());
    }

	@Override
	public void onAdd(String url, String title, String dir)
	{
		hp.insertItem(url,title);
		((WebView)content.getCurrentView()).reload();
	}

    
@Subscribe
public void onEvent(WindowEvent event){
    switch(event.what){
        case event.WHAT_NEW_WINDOW:
            openNewWindow();
            break;
        case event.WHAT_URL_NEW_WINDOW_BACKGROUND:
			openNewWindowInBackground(event.obj.toString());
            break;
		case event.WHAT_URL_NEW_WINDOW:
			openNewWindow(event.obj.toString());
			break;
        case event.WHAT_JS_NEW_WINDOW:
			((WebView.WebViewTransport)((Message)event.obj).obj).setWebView(openNewWindow());
			((Message)event.obj).sendToTarget();
            break;
		case event.WHAT_TOGGLE_WINDOW:
			openWindow(event.obj);
			break;
		case event.WHAT_URL_WINDOW:
			openUrl(event.obj.toString());
			break;
    }
}
@Subscribe
public void close(Integer close){
	switch(close){
		case WindowFragment.CLOSE:
		hidePop();
		break;
		case WindowFragment.OPEN:
			if(!hidePop()){
				pop.setVisibility(pop.VISIBLE);
				if (window.isAdded())
					getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.popin, R.anim.popout).show(window).commit();
				else
					getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.popin, R.anim.popout).add(R.id.mainview_popwin, window).show(window).commit();
			}
			break;
		case MenuFragment.SHOW:
			//打开菜单
			int time=0;
			if(current!=null&&!current.isHidden())
				time=150;
			hidePop();
			pop.postDelayed(new Runnable(){

					@Override
					public void run()
					{
						pop.setVisibility(pop.VISIBLE);
						if (menu.isAdded())
							getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.popin, R.anim.popout).show(menu).commit();
						else{
							getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.popin, R.anim.popout).add(R.id.mainview_popwin, menu).show(menu).commit();}
						menutool.showNext();

					}
				}, time);
			
			break;
		case MenuFragment.HIDE:
			hidePop();
			break;
		case MenuFragment.SHUTDOWN:
			getFragmentManager().beginTransaction().hide(menu).commit();
			break;
		case ToolManager.HOME:
			((WebView)content.getCurrentView()).goHome();
			break;
		}
}
    //隐藏弹出视图
    private boolean hidePop()
    {
        if (current != null && !current.isHidden())
        {
            getFragmentManager().beginTransaction().setCustomAnimations(R.anim.popin, R.anim.popout).hide(current).commit();
            return true;
        }
        return false;
    }


    @Override
    public void hide(FragmentPop f, boolean b)
    {

        if (b)
        {

            current = null;
            if (f == menu)menutool.showNext();
            pop.setVisibility(pop.INVISIBLE);
        }
        else
            current = f;
    }
    //当前窗口打开url
    public void openUrl(String url)
    {
       ((WebView)content.getCurrentView()).loadUrl(url);
    }
    //后台打开一个网页窗口
    public void openNewWindowInBackground(String url)
    {
        WebView wv=new WebView(getActivity(),ad);
		int index=content.getChildCount();
        content.addView(wv,index);
		wv.loadUrl(url);
    }
//打开一个网页窗口并跳转过去
    public WebView openNewWindow(String url)
    {
        WebView wv=new WebView(getActivity(),ad);
		int index=content.getChildCount();
        content.addView(wv,index);
		content.setDisplayedChild(index);
		wv.loadUrl(url);
		return wv;
    }
//打开一个空白窗口并跳转过去
    public WebView openNewWindow()
    {
        WebView wv=new WebView(getActivity(),ad);
		int index=content.getChildCount();
        content.addView(wv,index);
       content.setDisplayedChild(index);
	   return wv;
        }
//跳转窗口
    public boolean openWindow(int position)
    {
        content.setDisplayedChild(position);
        return false;
    }
    
   

    @Override
    public boolean onBackPressed()
    {
        if(!hidePop())
            if(((WebView)content.getCurrentView()).canGoBack())
            {  ((WebView)content.getCurrentView()).goBack();
            return true;
            }else
            return false;
        return true;
    }

    @Override
    public void onDestroyView()
    {
        Theme.unRegisterTheme(getView().findViewById(R.id.mainview_searchbar));
        Theme.unRegisterTheme(getView().findViewById(R.id.mainview_bar));
        super.onDestroyView();
    }

	@Override
	public void onDetach()
	{
		ToolManager.destroy();
		Theme.clear();
		EventBus.getDefault().unregister(this);
		super.onDetach();
	}
	
    class TextQuery extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context p1, Intent p2)
        {
            String text=p2.getStringExtra("text");
            if(text.indexOf("://")!=-1||text.indexOf(".")!=-1){
                if(text.indexOf("://")!=-1)
                    openUrl(text);
                    else
                    openUrl("http://"+text);
            }else
            openUrl("http://m.sm.cn/s?q="+text);
        }

        
    }
}

