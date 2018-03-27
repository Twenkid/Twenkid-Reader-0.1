/*
 * WebViewActivity.java
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
 * mkyong, stackoverflow, the Android Team
 *  
 * Version: 16/2/2018
 */

package twenkid.android.basic.news3;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;


public class WebViewActivity extends Activity {
	private WebView webView;
	String lastUrl="http://mail.bg";
	
	static boolean mLoadData = false; //Must be with a Bundle, but there were problems - if not fixed, use this
	static String mLoadDataContent = " "; //Must be with a Bundle, but there were problems - if not fixed, use this
	
	
	public static void SetLoadDataContent(String html){
		mLoadDataContent = html;		
		//mLoadData = true;
	}
	
	public static void SetLoadData(boolean loadData){
		mLoadData = loadData;		
	}
	
	//private Context context
	 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		
		//lastUrl = getString(R.string.feed_address); 		
		
		/*
		 * // Problem with sending a bundle, will be fixed in a future version
		  // Using static methods in version 0.1		 
		if (savedInstanceState!=null) {
		  lastUrl = (String) savedInstanceState.get("1"); //** Problem with sending the bundle!
		}
		*/
 
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		
		if (mLoadData) {
			//webView.loadData(mLoadDataContent, "text/html", "UTF-8"); //"http://www.google.com"); text/html
			webView.loadDataWithBaseURL(this.getFilesDir().getAbsolutePath(), mLoadDataContent, "text/html", "UTF-8", this.getFilesDir().getAbsolutePath()); //no history //"http://www.google.com"); text/html
			Log.d("WEBVIEW", this.getFilesDir().getAbsolutePath());
			
		}
		else webView.loadUrl(lastUrl); //"http://www.google.com");
 
		//webView.saveWebArchive(filename); //Test Later
	}
	
		/*
	public void setUrl(String url) {
		//Toast.makeText(this.getApplicationContext(), lastUrl + "setUrl?", 3); // + "mItem.content = "+ mItem.content,
		  
		  if (url!=lastUrl) //don't reload the same page 
		{ 
			lastUrl = url;			
		   webView.loadUrl(url);
	  	}
	  	
			  	
	}
	*/
	
}

