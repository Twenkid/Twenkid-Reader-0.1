/*
 * MyListActivity.java
 * 
 * Author: Todor Iliev Arnaudov
 * License: MIT (C) 2018
 * Part of "Twenkid Reader 0.1"
 * http://research.twenkid.com
 * http://artificial-mind.blogspot.com
 * 
 * To do: The image cache should be implemented better,
 * with more robust hash and file names. 
 *  
 * Credits go also to various authors of tutorials,
 * vogella, mkyong; @ stackoverflow and the Android Team
 *  
 * Special Thanks to Ivan Djefferov for suggesting me to
 * create the application. :)
 * 
 * Version: 16/2/2013
 
 * Copyright (C) 2018 Todor Iliev Arnaudov

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package twenkid.android.basic.news3;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
//import java.io.ByteArrayInputStream;
//import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import twenkid.android.basic.news3.ArticlesCache.ArticleItem;
import twenkid.android.basic.news3.MyListActivity.State.Mode;

//import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.Environment;
import android.os.Handler;
//import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class MyListActivity extends ListActivity {
	//static 
	//ArrayAdapter<String> adapter; 
	BasicArrayAdapter adapter;
	State mState;
	String feedAddress;

	private static final int DIALOG_LOADING = 20, DIALOG_REMOVECACHED = 21;

	ProgressDialog progressBar; //For Initial Loading
	private int progressBarStatus = 0;
	private int fileSize = 0; //1000000;
	private Handler progressBarHandler = new Handler();
	boolean mWorkOffline = false; //!!!!!!!!!
	private int mPosition = 0;
	private boolean mOfflineMode = false;

	static enum LoadMode { APPEND, LOAD };
	
	final String ARTICLE_CACHE = "twreader.txt";

	static class State{
		enum Mode { ADD_FAVE, REMOVE_FAVE, READ, LOADING}; 
		Mode state, prevState;
		public Mode Get() { return state; }
		public void Set(Mode state) {
			if ( (state != prevState) && ( (state == Mode.READ) && ( (prevState == Mode.REMOVE_FAVE) || (prevState == Mode.ADD_FAVE))))
			{
				//SaveCache();	
			}
			this.state = state;
		}
		public State(Mode state) {this.state = state; this.prevState = state;}
		public State() {this.state = Mode.READ; this.prevState = state;}		  
	}

	public void Init(){
		mState = new State();
		mState.state = Mode.LOADING; //If no connection -- load from cache, else from the RSS feed
		feedAddress = getString(R.string.feed_address);
		adapter.articlesCache.address = feedAddress;		
	}

	public void Reload(){

		progressBar = new ProgressDialog(MyListActivity.this);

		progressBar.setCancelable(true);
		progressBar.setMessage("Loading...");
		progressBar.setProgressStyle(ProgressDialog. STYLE_HORIZONTAL);  //STYLE_SPINNER
		progressBar.setProgress(0);
		progressBar.setMax(100);
		progressBar.show();

		progressBarStatus = 0;

		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		DownloadWebPageTask task = new DownloadWebPageTask();
		task.execute(new String[] { "http://feeds.feedburner.com/Mobilecrunch" });
		//Toast.makeText(this, " task.execute?...", Toast.LENGTH_LONG).show();	

		new Thread(new Runnable() {
			public void run() {
				while (progressBarStatus < 100) {

					// process some tasks
					progressBarStatus = doSomeTasks();

					// your computer is too fast, sleep 1 second
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// Update the progress bar
					progressBarHandler.post(new Runnable() {
						public void run() {
							progressBar.setProgress(progressBarStatus);
						}
					});
				}

				if (progressBarStatus >= 100) {

					// sleep 2 seconds, so that you can see the 100%
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// close the progress bar dialog
					progressBar.dismiss();
				}
			}
		}).start();


		mState.state = State.Mode.READ;

	}

	void RemoveFromCacheDialog(int position){
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.menu_deletefromcache);
		builder.setCancelable(true); //true);
		builder.setPositiveButton(R.string.menu_stringYes, new OkOnClickListener());
		builder.setNegativeButton(R.string.menu_stringNo, new CancelOnClickListener());
		AlertDialog dialog = builder.create();
		dialog.show();		 
	}

	public void onCreate(Bundle icicle) {

		final Context context = this;
		super.onCreate(icicle);
		String[] values = new String[] { "TechCrunch Loading..." };

		adapter = new BasicArrayAdapter(this, //getActivity().
				values); //android.R.layout.simple_list_item_1,

		setListAdapter(adapter);
		Init();	

		ListView listview = getListView();	    

		listview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {

				if (mWorkOffline){
					//...set html from cache, ... set in webview ...

				}

				if (mState.Get() ==  State.Mode.REMOVE_FAVE){
					//(BasicArrayAdapter) 
					adapter.articlesCache.SetFaveByIndex(position, false); //Yes, it shouldn't be public etc.	 
					adapter.notifyDataSetChanged();
					mPosition = position;
					RemoveFromCacheDialog(position);
					return;
					//adapter.
				}
				else
					if (mState.Get() ==  State.Mode.ADD_FAVE){
						mPosition = position;
						adapter.articlesCache.SetFaveByIndex(position, true);
						adapter.articlesCache.SetOfflineByIndex(position, true);	 //!!!!*
						adapter.notifyDataSetChanged();
						return;
					}	                		                	               
					else if (mState.Get() ==  State.Mode.READ)
					{
						//addToAdapter("GUZ!");
						Bundle arguments = new Bundle();
						arguments.putString("1", feedAddress);

						WebViewActivity.SetLoadDataContent(adapter.articlesCache.GetCompositeHtmlByIndex(position));
						WebViewActivity.SetLoadData(true);            			            		

						Intent intent = new Intent(context, WebViewActivity.class);
						startActivity(intent);
					}

			}
		}
				);

		Reload();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case R.id.menu_read:
			Toast.makeText(this, "Read Mode - Click to Open...", Toast.LENGTH_LONG).show();
			mState.Set(State.Mode.READ);
			break;
			
		//case R.id.menu_settings:
		//	adapter.clear();
		//	adapter.notifyDataSetChanged();
		//	Toast.makeText(this, "Debug - clear ... Menu Settings? -- Open Another Activity/Dialog...", Toast.LENGTH_LONG).show();
		//	break;
			
		case R.id.menu_workoffline:
			mState.Set(State.Mode.READ); // Plus another mode!!!
			mOfflineMode = true;
			LoadArticlesCache(MyListActivity.LoadMode.LOAD, ARTICLE_CACHE); 	        		      
			break;
			
		case R.id.menu_workonline:					
			mState.Set(State.Mode.READ); // Plus another mode!!!
			mOfflineMode = false;
			Reload();			
			break;
			
		case R.id.menu_addfave:
			mState.Set(State.Mode.ADD_FAVE); //  enum Mode { ADD_FAVE, REMOVE_FAVE, READ, LOADING};
			break;
			
		case R.id.menu_removefave:			
			mState.Set(State.Mode.REMOVE_FAVE); //  enum Mode { ADD_FAVE, REMOVE_FAVE, READ, LOADING};

			break;
			
		case R.id.menu_savecache:
			SaveArticlesCache();
			break;
			
		case R.id.menu_loadcache:
			LoadArticlesCache(MyListActivity.LoadMode.APPEND, ARTICLE_CACHE); // "twreader.txt"); //Without parameters -- checks state ...  
			break;
			
		case R.id.menu_useimagecache:
			adapter.articlesCache.SetUseImageCache(true);  
			LoadImageCache();
			break;
			
		case R.id.menu_dontuseimagecache:
			adapter.articlesCache.SetUseImageCache(false);  			 	        	  	        
			break;	           

		case R.id.menu_killapp:
			MyListActivity.this.finish();
			break;	  
		}

		return super.onOptionsItemSelected(item);	        
	}

	void addToAdapter(String s){
		adapter.add(s);
		adapter.notifyDataSetChanged();
		Log.d("MyListActivity", "addToAdapter? " + s);
		//Toast.makeText(this, "addToAdapter?..." + s, Toast.LENGTH_LONG).show();	 		 
	}

	public void SetListViewContent(ArrayList<String> arrayList){ //13-2-2013, Temp
		adapter.clear();

		int n=50;

		for (int i=0; i< adapter.articlesCache.GetTitlesCount(); i++){
			adapter.add(adapter.articlesCache.GetTitleByIndex(i));
			n--;
			if (n==0) break;
		}

		adapter.notifyDataSetChanged();
		setListAdapter(adapter);
		Toast.makeText(this, "Menu SetListViewContent?...", Toast.LENGTH_LONG).show();	 

	}

	void LoadImageCache(){

		new Thread(new Runnable() { //Download Image Cache -- only when going online/reloading?
			public void run() {

				Log.d("IMAGESDOWNLOAD", "START");
				int counter = 0;
				for (String title: adapter.articlesCache.articleTitles){
					Log.d("CYCLE", title);
					///*
					ArticleItem item = adapter.articlesCache.articles.get(title);
					FileDownloader fileDownload = new FileDownloader();

					if (!adapter.articlesCache.IsImageCached(title))
					{
						String file = adapter.articlesCache.GetCacheName(title);
						fileDownload.downloadFileToDisk(item.thumbnail, file);
						adapter.articlesCache.AddImageItem(title);
						Log.d("IMAGESDOWNLOAD_ADDED" + title, file);
					}  	                  
				}}}).start();
	}

	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {

		ArrayList<String> lines = new ArrayList<String>(20);

		@Override
		protected String doInBackground(String... urls) {
			StringBuilder responseBuilder = new StringBuilder(); // "";
			lines.clear();

			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);

				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					int nmax = 9999, n=0; //TEMP LIMIT
					while (((s = buffer.readLine()) != null)  && n<nmax) {
						//Log.d("Download...", " " + n);
						if ((n%50) == 0) { 
							Log.d("Download...", " " + n);
							fileSize+=15000;
						}
						//response += s;
						responseBuilder.append(s);
						lines.add(s);	
						n++;
					}

				} catch (Exception e) {
					//NO! ... printstack trace, but continue!
					e.printStackTrace();
				}
			}

			String response = responseBuilder.toString();
			response.replace('\n', ' ');
			response.replace('\r', ' ');
			response.replace('\t', ' ');  //Regex matches at line boundaries, if not cleaned, the matching couldn't happen with one expression

			String patternIn = "(itemtitle\">.{0,5}<a href=\".{50})(.{30})"; //)</a>";
			patternIn = "itemtitle\">.{0,5}<a href=\"(.{5,90})\">(.{4,90})</a>"; //)</a>";
			patternIn = "<a href=\"(.{5,90})\">(.{4,90})</a>"; //)</a>";
			patternIn = "<title>.+</title>";
			patternIn = "<title>(.{1,150})</title>.{0,2}<link>(.{1,199})</link>"; //<title>(.+{1,150})" +"</title>.{0,2}<link>(.+{1,199})</link>";
			String titlePattern, linkPattern;

			String htmlSource = response.toString();
			Pattern pattern =  Pattern.compile(patternIn);
			Matcher matcher = pattern.matcher(htmlSource); //response.toString());

			String itemTitlePattern = "itemtitle\">";
			int start=0;
			int startPrev = start-1;

			String foundTitle, foundLink;

			Pattern patternItem =  Pattern.compile("<item>.+</item>"); //da ne hvane vsichki!
			Matcher matcherItem = pattern.matcher(htmlSource);
			String itemContent;

			int firstItemStart = htmlSource.indexOf("<item>");

			String header = "--- HEADER NOT FOUND ---!";
			//<?xml-stylesheet type="text/xsl" media="screen" href="/~d/styles/rss2full.xsl"?><?xml-stylesheet type="text/css" media="screen" href="http://feeds.feedburner.com/~d/styles/itemcontent.css"?><rss xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:wfw="http://wellformedweb.org/CommentAPI/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:sy="http://purl.org/rss/1.0/modules/syndication/" xmlns:slash="http://purl.org/rss/1.0/modules/slash/" xmlns:georss="http://www.georss.org/georss" xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#" xmlns:media="http://search.yahoo.com/mrss/" xmlns:feedburner="http://rssnamespace.org/feedburner/ext/1.0" version="2.0">

			if (firstItemStart!=-1){
				header = htmlSource.substring(0, firstItemStart);	    		 
			}
			adapter.articlesCache.SetHeader(header);	

			String footer = "-- FOOTER NOT FOUND-- ";
			int footerStart = htmlSource.indexOf("</channel>");
			if (footerStart!=-1) footer = htmlSource.substring(footerStart);
			adapter.articlesCache.SetFooter(footer);
			//</channel>
			//</rss>
			int itemStart=0, itemEnd=0;

			matcher.find(); //two times
			matcher.find();
			//First: 	    	
			//<channel><title>TechCrunch » Mobile</title>
			//<link>http://techcrunch.com</link>

			//<url>TechCrunch » Mobile</title>
			//<link>http://techcrunch.com</link>

			String descriptionTag = "<description>";
			String thumbnailImg;
			String pubDate;
			String creator;

			Pattern thumbnailPattern = Pattern.compile("<media:thumbnail url=\"(.{10,180})\".{1,5}/>");

			Matcher thumbnailMatcher = thumbnailPattern.matcher(htmlSource);

			Pattern pubDatePattern = Pattern.compile("<pubDate>(.{2,50})</pubDate>");	    	
			Matcher pubDateMatcher = pubDatePattern.matcher(htmlSource);

			Pattern creatorPattern = Pattern.compile("<dc:creator>(.{2,90})</dc:creator>");	    	
			Matcher creatorMatcher = creatorPattern.matcher(htmlSource);

			//15-2-2013: In future version: create a class and an array of patterns and matchers for
			//more elegant code!			    		    		 	    		   

			int counter = 0;
			while (matcher.find()){  //start
			
				if ((itemStart!=-1) && (itemEnd!=-1)) {
					itemStart = htmlSource.indexOf("<item>", itemStart);
					itemEnd = htmlSource.indexOf("</item>", itemEnd);
				}

				if (thumbnailMatcher.find(itemStart)) thumbnailImg =  thumbnailMatcher.group(1);
				else thumbnailImg = "http://research.twenkid.com/img/twenkid-gmail-200-wh.jpg";

				//If there's error or missing date in the feed, add time now :)
				if (pubDateMatcher.find(itemStart)) pubDate =  pubDateMatcher.group(1);
				else pubDate = Calendar.getInstance().getTime().toString();

				//If there's error or missing date in the feed, add time now :)
				if (creatorMatcher.find(itemStart)) creator =  creatorMatcher.group(1);
				else creator = "Unknown Author";		    				    			    			    				  

				//Find pubDate and creator		    		
				Log.d("THUMBNAIL?", thumbnailMatcher.group(1)); // 


				if ((itemStart!=-1) && (itemEnd!=-1)){
					int description = htmlSource.indexOf("<description>", itemStart);

					int contentencoded = htmlSource.indexOf("]]></content:encoded>", itemStart); //End of meaningful
					//The rest is mediainfo -- pictures -- next version! In 0.1 - only the thumbnail

					if ( (description!=-1) && (contentencoded!=1)) itemContent = htmlSource.substring(description, contentencoded);
					else itemContent = htmlSource.substring(itemStart, itemEnd);

					itemStart++;
					itemEnd++;
				}	 
				else itemContent = "--- <strong>CONTENT NOT FOUND!</strong> ---";

				if (adapter.articlesCache.GetArticleByTitle(matcher.group(1))==null) { //title

					adapter.articlesCache.AddItem(matcher.group(1), matcher.group(2), itemContent, thumbnailImg, pubDate, creator, false, false);                      
					counter++;
				}
				//Log.d("FOUND? All", matcher.group(0));	    		
				//Log.d("FOUND 1 ?", matcher.group(1));
				//Log.d("FOUND 2?", matcher.group(2));	
				//Log.d("CONTENT", itemContent); 
			}

			adapter.articlesCache.SetAbsolutePath(getFilesDir().getAbsolutePath()); //15-2-2013		      

			fileSize = 1000000; //MORE ELEGANT!
			return response;	     
		}

		@Override
		protected void onPostExecute(String result) {	    	
			progressBarStatus = 100;
			Log.d("OnPostExec?", "DownloadTask");
			SetListViewContent(lines);
			Log.d("OnPostExec?", lines.get(0));

			LoadImageCache();	    	  	    	  	 
		}
	}	  

	//Makes the progress bar moves -- improve
	public int doSomeTasks() { 

		while (fileSize <= 1000000) {


			if (fileSize == 100000) {
				return 10;
			} else if (fileSize == 200000) {
				return 20;
			} else if (fileSize == 300000) {
				return 30;
			}
			else return fileSize/100000;
			// ...add your own	 
		}

		return 100;	 
	}	

	private final class CancelOnClickListener implements
	DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			//Toast.makeText(getApplicationContext(), "Activity will continue",
			//		Toast.LENGTH_LONG).show();
		}
	}

	private final class OkOnClickListener implements
	DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {		     
			adapter.articlesCache.SetOfflineByIndex(mPosition, false);		    	
		}
	}

	public void SaveArticlesCache(){

		String eol ="\n"; // System.getProperty("line.separator"); /?EOL must be "n", not like in Vogella!!!!
		BufferedWriter writer = null;

		//In future refactoring: ArticleItem item = adapter.articlesCache. --> ArticleItem
		try {//twreader.txt
			writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(ARTICLE_CACHE, android.content.Context.MODE_PRIVATE))); //WORLD_READABLE))); //"twreader.txt"

			writer.write("Twenkid Reader" + eol);
			writer.write("Version 0.1 14.2.2013" + eol);

			writer.write(adapter.articlesCache.address + eol); // + delimiter); //Eol);
			writer.write(adapter.articlesCache.header + eol);// + delimiter); //Eol);
			writer.write(adapter.articlesCache.footer + eol); // + delimiter); //Eol);

			writer.write(adapter.articlesCache.articleTitles.size() + eol);

			for (String title: adapter.articlesCache.articleTitles){
				Log.d("CYCLE-WRITEFILE", title);
				///*
				ArticleItem item = adapter.articlesCache.articles.get(title); //Use more in future refactoring, shorter code  :)

				if (adapter.articlesCache.articles.get(title).offline) //offline) //{
				{

					writer.write(title + eol);
					writer.write(adapter.articlesCache.articles.get(title).address + eol);
					writer.write(adapter.articlesCache.articles.get(title).thumbnail + eol);
					writer.write(adapter.articlesCache.imageCache.get(title) + eol); //path to image cache
					writer.write(adapter.articlesCache.articles.get(title).pubDate + eol);
					writer.write(adapter.articlesCache.articles.get(title).creator + eol);

					if (adapter.articlesCache.articles.get(title).fave) writer.write("Fave" + eol);  //Delimiter -- offline is implied true
					else writer.write("Regular" + eol);  //Delimiter 

					writer.write(adapter.articlesCache.articles.get(title).content + eol);
					Log.d("WRITEFILE", title + ", " + item.title + ", " + adapter.articlesCache.articles.get(title).address);
				}
			}			       			      
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}		  

	public void LoadArticlesCache(LoadMode loadMode, String path){

		if (loadMode == MyListActivity.LoadMode.LOAD){
			adapter.clear();		
			adapter.notifyDataSetChanged();
			adapter.articlesCache.clear();
			Toast.makeText(this, " Load Articles Cache?...", Toast.LENGTH_LONG).show();
		}
		else Toast.makeText(this, " Append Articles Cache?...", Toast.LENGTH_LONG).show();

		BufferedReader reader = null;			 
		StringBuilder stringBuilder = new StringBuilder();

		try {
			reader = new BufferedReader(new InputStreamReader(openFileInput(path))); //"twreader.txt"))); // android.content.Context.MODE_WORLD_WRITEABLE)));				 

			String line="";
			StringBuffer buffer = new StringBuffer();
			int lineCounter = 0;

			stringBuilder.append(reader.readLine());
			stringBuilder.append(reader.readLine());

			adapter.articlesCache.address = reader.readLine(); //				  
			stringBuilder.append(adapter.articlesCache.address);

			adapter.articlesCache.header = reader.readLine(); //line;
			stringBuilder.append(adapter.articlesCache.header);
			adapter.articlesCache.footer = reader.readLine();

			stringBuilder.append(adapter.articlesCache.footer);

			Log.d("READFILE", adapter.articlesCache.address);
			Log.d("READFILE", adapter.articlesCache.header);
			Log.d("READFILE", adapter.articlesCache.footer);

			line = reader.readLine(); //number of titles
			Log.d("READFILE Num of Items?", line);

			lineCounter++; //don't use below?

			String title, address, content, thumbnail, image, pubDate, creator, fave;

			boolean bFave;
			int i = 0;
			while ((title = reader.readLine()) != null) {					  
				address = reader.readLine();
				thumbnail = reader.readLine();
				image =  reader.readLine();//path to image cache

				pubDate = reader.readLine();
				creator = reader.readLine();
				fave = reader.readLine();
				content = reader.readLine();

				bFave = fave.equalsIgnoreCase("Fave");

				Log.d("READFILE", "Title: "+ title + "\n" + "Thumbnail: " + thumbnail + "\n FAVE: " + bFave + "\n CONTENT: " + content +  "\n Creator: " + creator +  "\n PubDate: " + pubDate);

				if (loadMode == MyListActivity.LoadMode.APPEND){
					ArticleItem item = adapter.articlesCache.GetArticleByTitle(title);
					if (item!=null) continue;			       						  				    	  
				}
				//If LOAD or APPEND, but the item doesn't exist already:
				adapter.articlesCache.AddItem(title, address, content, thumbnail, pubDate, creator, bFave, true); // offline is true by default, since it's loaded from the source
				adapter.articlesCache.AddImageItem(title); 

				adapter.add(title);

				stringBuilder.append("\n" + i + title + ", " + thumbnail + "FAVE: " + fave + address + thumbnail + content);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	

		//Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_LONG).show();
		adapter.notifyDataSetChanged();
		ListView listview = getListView();
		listview.invalidate();		    
	}		  


	/* Download Images to the cache */
	public class FileDownloader {

		public void downloadFileToDisk(String url, String path) {
			BufferedWriter writer = null;
			InputStream inputStream = null;
			java.io.FileOutputStream fos = null; 
			java.io.BufferedOutputStream bout = null;
			OutputStreamWriter osw = null; /// new OutputStreamWriter(fos);
			Log.d("FileDownloader!", "START");

			try {
				HttpURLConnection con = (HttpURLConnection) new URL(url)
				.openConnection();
				inputStream = con.getInputStream();
				final int maxBuffer = 1024;
				byte buffer[] = new byte[maxBuffer];
				int offset = 0;
				int length = maxBuffer/2;
				int readBytes = 0;

				fos =  getApplicationContext().openFileOutput(path, Context.MODE_PRIVATE); //WORLD_READABLE); //android.content.Context.MODE_WORLD_READABLE
				bout = new BufferedOutputStream(fos, length); 				         

				while ( (readBytes = inputStream.read(buffer, offset, length))>=0){
					bout.write( buffer, 0, length); //offset); offset+= readBytes.. ?//Performance if recorded syncrhonously, or download all to a buffer and then stored at once?
					Log.d("FileDownloader!", "BUFFER?");
				}				
				Log.d("DOWNLOADED_IMAGE? to ", path);
			} catch (IOException e) {
				Log.d("IOException!", e.toString());
			} catch (IllegalStateException e) {
				Log.d("Incorrect URL IllegalStateException", url);
			} catch (Exception e) {
				Log.d("Error while retrieving data from ", url);					 
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (fos!=null)
						try {
							fos.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					if (bout!=null)
						try {
							bout.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					if (fos!=null)
						try {
							fos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					if (bout!=null)
						try {
							bout.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			}
		}
	}		

}