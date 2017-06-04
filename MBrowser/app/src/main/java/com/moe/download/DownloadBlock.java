package com.moe.download;
import com.moe.entity.DownloadInfo;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.InputStream;
import com.moe.database.Download;
import java.io.File;
import com.moe.Mbrowser.R;
import de.greenrobot.event.EventBus;
import android.text.TextUtils;
import okhttp3.ResponseBody;

public class DownloadBlock extends Thread
{
	private DownloadTask dt;
	private DownloadInfo di;
	private Response response;
	private RandomAccessFile raf;
	private InputStream is;
	private boolean pause=false;
	public DownloadBlock(DownloadTask dt,DownloadInfo di){
		this.dt=dt;
		this.di=di;
		setPriority(Thread.MIN_PRIORITY);
		start();
	}
	public boolean isSuccess(){
		return di.isSuccess();
	}
	@Override
	public void run()
	{
		if(!isSuccess()){
		Request.Builder request=new Request.Builder();
			if (!TextUtils.isEmpty(dt.getTaskInfo().getCookie()))
				request.addHeader("Cookie", dt.getTaskInfo().getCookie());
			if (!TextUtils.isEmpty(dt.getTaskInfo().getUserAgent()))
				request.addHeader("User-Agent", dt.getTaskInfo().getUserAgent());
			//if(ti.getSourceUrl()!=null)
			//request.addHeader("Referer",ti.getSourceUrl());
			request.addHeader("Accept","*/*");
			request.addHeader("Connection","close");
			request.addHeader("Icy-MetaData","1");
			request.addHeader("Range","bytes="+di.getCurrent()+"-"+(di.getEnd()==0?"":di.getEnd()));
		try
		{
			response = dt.getOkHttpCliebt().newCall(request.url(di.getUrl()).build()).execute();
			ResponseBody rb=response.body();
			is=rb.byteStream();
			if(di.getEnd()==0){
				long length=0;
				try
				{
					length = Long.parseLong(response.header("Content-Length"));
				}
				catch (Exception e)
				{length=rb.contentLength();}
				di.setEnd(length);
			}
			dt.getDownload().updateDownloadInfoWithData(di);
			byte[] b=new byte[Integer.parseInt(dt.getContext().getResources().getTextArray(R.array.buffer)[dt.getSharedPreferences().getInt(Download.Setting.BUFFER,Download.Setting.BUFFER_DEFAULT)].toString())];
			File file=new File(dt.getTaskInfo().getDir(),dt.getTaskInfo().getTaskname());
			if(dt.getTaskInfo().getM3u8()){
				if(file.exists())file.delete();
				file.mkdirs();
			file=new File(file,di.getNo()+"");}
			raf=new RandomAccessFile(file,"rw");
			if(dt.getTaskInfo().getM3u8()&&!file.exists())
				raf.setLength(di.getEnd());
			int len;
			switch(response.code()){
				case 200:
					raf.seek(di.getCurrent());
					is.skip(di.getCurrent());
					break;
				case 206:
					raf.seek(di.getCurrent());
					break;
				default:
				raf.close();
				is.close();
				new IOException();
				break;
			}
			
			while((len=is.read(b))!=-1){
				raf.write(b,0,len);
					di.setCurrent(di.getCurrent()+len);
					dt.getDownload().updateDownloadInfo(di);
					EventBus.getDefault().post(dt.getTaskInfo());
				if(pause==true)return;
			}
		}
		catch (IOException e)
		{
			if(!pause)
			dt.itemFinish(this);
			
			return;
		}finally{
			try
			{
				if(raf!=null)
				raf.close();
			}
			catch (IOException e)
			{}
			try
			{
				if(is!=null)
					is.close();
			}
			catch (IOException e1)
			{}
			if(response!=null)
				response.close();
		}
		}
		di.setSuccess(true);
		dt.itemFinish(this);
	}
	public void pause(){
		pause=true;
		if(!isSuccess()){
			try
			{
				if (raf != null)
					raf.close();
			}
			catch (IOException e)
			{}
			
		}
	}
}
