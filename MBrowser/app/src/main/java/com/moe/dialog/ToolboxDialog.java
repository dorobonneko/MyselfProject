package com.moe.dialog;
import android.content.Context;
import com.moe.Mbrowser.R;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.GridLayoutManager;
import com.moe.bean.MenuItem;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import com.moe.adapter.MenuAdapter;
import android.view.View;
import de.greenrobot.event.EventBus;
import com.moe.internal.ToolManager;
import com.moe.webkit.WebViewManagerView;
import com.moe.bean.MenuOptions;

public class ToolboxDialog extends android.app.Dialog implements MenuAdapter.OnItemClickListener
{
	private RecyclerView rv;
	private MenuAdapter ta;
	private List<MenuItem> lmi=new ArrayList<>();
	private final static String xmlns="http://schemas.android.com/apk/res/android";

	public final static int SHOW=0xff10;
	public ToolboxDialog(Context context){
		super(context,R.style.Dialog);
		try
		{
			parser(R.menu.toolbox);
		}
		catch (XmlPullParserException e)
		{}
		catch (IOException e)
		{}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setWindowAnimations(R.style.PopupWindowAnim);
		super.onCreate(savedInstanceState);
		setContentView(rv=new RecyclerView(getContext()),new ViewGroup.LayoutParams(getWindow().getWindowManager().getDefaultDisplay().getWidth(),ViewGroup.LayoutParams.WRAP_CONTENT));
		GridLayoutManager glm=new GridLayoutManager(getContext(),5);
		glm.setAutoMeasureEnabled(true);
		rv.setBackgroundResource(R.color.window_background);
		rv.setLayoutManager(glm);
		rv.setAdapter(ta=new MenuAdapter(lmi));
		rv.setOverScrollMode(rv.OVER_SCROLL_NEVER);
		ta.notifyDataSetChanged();
		ta.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(View v)
	{
		switch(v.getId()){
			case R.id.toolbox_webpageSarch:
				ToolManager.getInstance().findToggle(true);
				break;
			case R.id.toolbox_webpageSave:
				((WebViewManagerView)ToolManager.getInstance().getContent().getCurrentView()).saveWebArchive();
				break;
			case R.id.toolbox_videoFind:
				((WebViewManagerView)ToolManager.getInstance().getContent().getCurrentView()).videoFind();
				break;
			case R.id.toolbox_webSource:
				((WebViewManagerView)ToolManager.getInstance().getContent().getCurrentView()).watchSource();
				break;
			case R.id.toolbox_javascript:
				EventBus.getDefault().post(JavaScriptDialog.SHOW);
				break;
			case R.id.toolbox_stop:
				((WebViewManagerView)ToolManager.getInstance().getContent().getCurrentView()).blockUrl();
				break;
			case R.id.toolbox_network_log:
				EventBus.getDefault().post(MenuOptions.NETWORKLOG);
				break;
		}
		dismiss();
	}

	
	private void parser(int resId) throws XmlPullParserException, IOException{
		XmlPullParser xml=getContext().getResources().getXml(resId);
		int type;
		while((type=xml.next())!=xml.END_DOCUMENT){
			switch(type){
				case xml.START_TAG:
					if(xml.getName().equals("item")){
						MenuItem mi=new MenuItem();
						mi.setId(Integer.parseInt(xml.getAttributeValue(xmlns,"id").substring(1)));
						String icon=xml.getAttributeValue(xmlns,"icon");
						if(icon!=null)
							mi.setIcon(getContext().getResources().getDrawable(Integer.parseInt(icon.substring(1))));
						String title=xml.getAttributeValue(xmlns,"title");
						if(title.startsWith("@"))
							title=getContext().getResources().getString(Integer.parseInt(title.substring(1)));
						mi.setSummory(title);
						lmi.add(mi);
					}
					break;
				case xml.END_TAG:
					
					break;
			}
		}

	}
}
