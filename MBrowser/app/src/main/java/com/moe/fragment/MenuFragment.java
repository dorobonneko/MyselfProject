package com.moe.fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.support.v4.view.ViewPager;
import com.moe.utils.Theme;
import android.widget.LinearLayout;
import android.util.TypedValue;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import com.moe.adapter.ViewPagerAdapter;
import java.util.ArrayList;
import android.support.v7.widget.GridLayoutManager;
import com.moe.bean.MenuItem;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import com.moe.adapter.MenuAdapter;
import com.moe.Mbrowser.R;
import android.support.v7.widget.StaggeredGridLayoutManager;
import com.moe.widget.MenuGridLayoutManager;
import com.moe.widget.MenuViewPager;
import android.widget.Toast;
import android.view.Gravity;
import android.widget.AdapterView.OnItemClickListener;
import de.greenrobot.event.EventBus;
import com.moe.bean.MenuOptions;
import com.moe.utils.ToolManager;
import android.content.SharedPreferences;
import com.moe.widget.WebView;
import com.moe.dialog.ToolboxDialog;

public class MenuFragment extends FragmentPop implements MenuAdapter.OnItemClickListener
{
public final static int SHOW=0XFF0002;
public final static int HIDE=0XFF0003;
public final static int SHUTDOWN=0xff005;
private ViewPagerAdapter vpa;
private ArrayList<View> av=new ArrayList<>();
private int groupSize=0;//计算几组
private SharedPreferences shared,moe;
private final static String xmlns="http://schemas.android.com/apk/res/android";
private ToolboxDialog td;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
		ViewPager vp=new MenuViewPager(getActivity().getApplicationContext(),av);
        LinearLayout.LayoutParams ll=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        //ll.setMargins(5,5,5,5);
        vp.setLayoutParams(ll);
        //Theme.registerTheme(vp);
		vp.setAdapter(vpa=new ViewPagerAdapter(av));
		try
		{
			parser(R.menu.menu);
		}
		catch (XmlPullParserException e)
		{}
		catch (IOException e)
		{}
		vp.setBackgroundResource(R.color.window_background);
        return vp;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
		shared=getContext().getSharedPreferences("webview",0);
		moe=getContext().getSharedPreferences("moe",0);
		td=new ToolboxDialog(getActivity());
		updateBlockImage();
		updateFull();
    }

	@Override
	public void onItemClick(View v)
	{
		
		switch(v.getId()){
			case R.id.menu_nightmode:
				EventBus.getDefault().post(MenuOptions.NIGHTMODE);
				EventBus.getDefault().post(HIDE);
				break;
			case R.id.menu_bookmark:
				EventBus.getDefault().post(MenuOptions.BOOKMARKS);
				EventBus.getDefault().post(SHUTDOWN);
				break;
			case R.id.menu_fullscreen:
				EventBus.getDefault().post(MenuOptions.FULLSCREEN);
				EventBus.getDefault().post(HIDE);
				updateFull();
				break;
			case R.id.menu_refresh:
				ToolManager.getInstance().refresh();
				EventBus.getDefault().post(HIDE);
				break;
			case R.id.menu_noimage:
				shared.edit().putBoolean(WebView.Setting.BLOCKIMAGES,!shared.getBoolean(WebView.Setting.BLOCKIMAGES,false)).commit();
				updateBlockImage();
				EventBus.getDefault().post(HIDE);
				break;
			case R.id.menu_download:
				EventBus.getDefault().post(MenuOptions.DOWNLOAD);
				EventBus.getDefault().post(SHUTDOWN);
				break;
			case R.id.menu_autoscreen:
				shared.edit().putBoolean(WebView.Setting.WIDEVIEW,!shared.getBoolean(WebView.Setting.WIDEVIEW,true)).commit();
				Toast.makeText(getActivity(),shared.getBoolean(WebView.Setting.WIDEVIEW,true)==true?"自适应布局已开启":"自适应布局已关闭",Toast.LENGTH_SHORT).show();
				EventBus.getDefault().post(HIDE);
				break;
			case R.id.menu_desktop:
				shared.edit().putBoolean(WebView.Setting.DESKTOP,!shared.getBoolean(WebView.Setting.DESKTOP,false)).commit();
				Toast.makeText(getActivity(),shared.getBoolean(WebView.Setting.DESKTOP,true)==true?"桌面模式已开启":"桌面模式已关闭",Toast.LENGTH_SHORT).show();
				ToolManager.getInstance().refresh();
				EventBus.getDefault().post(HIDE);
				break;
			case R.id.menu_toolbox:
				td.show();
				EventBus.getDefault().post(SHUTDOWN);
				break;
			default:
				EventBus.getDefault().post(HIDE);
			break;
		}
		
	}
private void updateFull(){
	MenuAdapter ma=((MenuAdapter)((RecyclerView)av.get(0)).getAdapter());
	MenuItem mi=ma.get(2);
	if(moe.getBoolean("full",false))
		mi.setIcon(getResources().getDrawable(R.drawable.menu_fullscreen_exit));
	else
		mi.setIcon(getResources().getDrawable(R.drawable.menu_fullscreen));
	ma.notifyItemChanged(2);
}
private void updateBlockImage(){
	MenuAdapter ma=((MenuAdapter)((RecyclerView)av.get(0)).getAdapter());
	MenuItem mi=ma.get(4);
	if(shared.getBoolean(WebView.Setting.BLOCKIMAGES,false))
		mi.setIcon(getResources().getDrawable(R.drawable.menu_image_broken));
	else
		mi.setIcon(getResources().getDrawable(R.drawable.menu_picture));
		ma.notifyItemChanged(4);
}
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed()
    {
        // TODO: Implement this method
        return false;
    }

    private void parser(int resId) throws XmlPullParserException, IOException{
		XmlPullParser xml=getResources().getXml(resId);
		int type;
		RecyclerView rv=null;
		ArrayList<MenuItem> ami = null;
		MenuAdapter ma = null;
		while((type=xml.next())!=xml.END_DOCUMENT){
			switch(type){
				case xml.START_TAG:
					if(xml.getName().equals("group")){
						groupSize++;
						if(groupSize%2!=0){
							rv=new RecyclerView(getActivity());
							ami=new ArrayList<MenuItem>();
							rv.setNestedScrollingEnabled(false);
							rv.setOverScrollMode(rv.OVER_SCROLL_NEVER);
							ViewPager.LayoutParams vl= new ViewPager.LayoutParams();
							vl.width=vl.MATCH_PARENT;
							vl.height=vl.WRAP_CONTENT;
							rv.setLayoutParams(vl);
							rv.setAdapter(ma=new MenuAdapter(getActivity(),ami));
							GridLayoutManager glm=new GridLayoutManager(getActivity(),5);
							rv.setLayoutManager(glm);
							rv.setHasFixedSize(true);
							glm.setAutoMeasureEnabled(true);
							av.add(rv);
						}
					}
					if(xml.getName().equals("item")){
						MenuItem mi=new MenuItem();
						mi.setId(Integer.parseInt(xml.getAttributeValue(xmlns,"id").substring(1)));
						String icon=xml.getAttributeValue(xmlns,"icon");
						if(icon!=null)
						mi.setIcon(getResources().getDrawable(Integer.parseInt(icon.substring(1))));
						String title=xml.getAttributeValue(xmlns,"title");
						if(title.startsWith("@"))
							title=getResources().getString(Integer.parseInt(title.substring(1)));
						mi.setSummory(title);
						ami.add(mi);
					}
					break;
					case xml.END_TAG:
						if(xml.getName().equals("group")&&groupSize%2!=0){
						ma.notifyDataSetChanged();
						ma.setOnItemClickListener(this);
						}
						break;
			}
		}
		vpa.notifyDataSetChanged();
		
	}
}
