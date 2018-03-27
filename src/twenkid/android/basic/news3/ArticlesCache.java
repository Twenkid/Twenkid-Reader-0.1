/*
 * Author: Todor Iliev Arnaudov
 * License: MIT (C) 2018
 * Part of "Twenkid Reader 0.1"
 * http://research.twenkid.com
 * http://artificial-mind.blogspot.com
 * 
 * To do: The image cache should be implemented better,
 * with more robust hash and file names. 
 *  
 * Version: 16/2/2013
 */
package twenkid.android.basic.news3;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet; //*

public class ArticlesCache {
	
   class ArticleItem{
	   public String address; //must be private ... 
	   public String title;
	   public String content;	   
	   public boolean fave;
	   public boolean offline;
	   public String thumbnail;
	   public String pubDate = "Unknown Date";
	   public String creator = "Unknown Author";
	   
     //+ Timestamp	   
	   ArticleItem(String address, String title, String content, String thumbnail, 
			       String pubDate, String creator, boolean fave, boolean offline){
		   
		   this.address = address;
		   this.title = title;
		   this.content = content;
		   this.thumbnail = thumbnail;
		   this.fave = fave;
		   this.offline = offline;
		   this.pubDate = pubDate;
		   this.creator = creator;
	  }
	   
	   //public String get
	   	  
   }
   
   HashMap<String, ArticleItem> articles; //Title, Article
   HashMap<String, String> imageCache; //Title, imageFile (img0.png, img1.png ... (in this version attaches png always, doesn't care for the original extension
   ArrayList<String> articleTitles; //Keeps them in the order they come, in order to be aligned with the Listview representation
   int imageCounter = 0;
   String address; //Common address of the feed 
   String header, footer; //Header and Footer of the feed 
   String absolutePath; //Path in Android file system, e.g. file:///data/data/twenkid.basic.news3/files/img0.ong
   boolean useImageCache; //Turn On/Off using cached images -- some images don't appear when cached/loaded from the local storage, for unknown reason as of 16/2/2013 
   
   public void SetUseImageCache(boolean use){ 
	   useImageCache = use;
   }
   
   /*
    * Used internally when building the image cache
    */
   public int GetCounterIndex(String title){
	   if (!imageCache.containsKey(title)){		   
		   return imageCounter++;
	   }
	   else return -1;
   }
   
   /*
    * Gets or constructs a file name of a cached image
    */
   public String GetCacheName(String title){ 
	   if (!imageCache.containsKey(title)){
		   String file = "img" + imageCounter + ".png"; //doesn't matter if it's jpg etc.
		   return file;
	   }
	   else return imageCache.get(title); 
   }
   
   public boolean IsImageCached(String title){
	   return imageCache.containsKey(title); //   
   }
     
   public void AddImageItem(String title){
	   if (imageCache.containsKey(title)) {
		   Log.d("AddImageItem", "Already Added!");
		   return; //{ //return; //Already in the cache
	   }
	   
	   imageCache.put(title, GetCacheName(title) ); //file);
	   imageCounter++;	   
   }
   
   /*
    * Set from the Activity for proper access to offline images
    */
   public void SetAbsolutePath(String absPath){
	   absolutePath = absPath;
   }
   
   public ArticlesCache(){
	   articles = new  HashMap<String, ArticleItem>();	   
	   articleTitles = new ArrayList<String>();
       imageCache = new HashMap<String, String>();
       SetUseImageCache(true);
   }
   
   //Short AddItem, No author and date added
   public void AddItem(String title, String address, String content, String thumbnail, boolean fave, boolean offline){
	   if (!articles.containsKey(title)) { //return; //Already in the cache
	   
	   articles.put(title, new ArticleItem(address, title, content, thumbnail, " ", " ", fave, offline));
	   articleTitles.add(title);
	   }
   }
   
   //Long AddItem, includes Date and Author
   public void AddItem(String title, String address, String content, String thumbnail, String pubDate, String creator, boolean fave, boolean offline){
	   if (!articles.containsKey(title)) { //return; //Already in the cache
	   
	   articles.put(title, new ArticleItem(address, title, content, thumbnail, pubDate, creator, fave, offline));
	   articleTitles.add(title);
	   }
   }
   
   public String GetAddressByTitle(String title){
	   if (articles.containsKey(title)) {		   
		   return articles.get(title).address;
	   }
	   return "Not Found";	   
   }

   public ArticleItem GetArticleByTitle(String title){ 
	   if (articles.containsKey(title)) {		   
		   return articles.get(title);
	   }
	   return null;	   
   }

   public int GetTitlesCount(){
	   return articleTitles.size();
   }

   public String GetTitleByIndex(int index){		  
	   if ( (index>=articles.size()) || (index <0)) return null;		    			   
	   return articleTitles.get(index);	   
   }

   public String GetContentByIndex(int index){		   	   
	   if ( (index>=articles.size()) || (index <0)) return " "; //not null, continue -- add exceptions or deal with, later
	   return GetArticleByTitle(GetTitleByIndex(index)).content; 	   	 
   }

   public boolean SetFaveByIndex(int index, boolean fave){
	   if ( (index >= articles.size() || (index<0))) return false; //illegal parameters, don't throw exception, just ignore -- -- add exceptions or deal with, later
	   GetArticleByTitle(GetTitleByIndex(index)).fave = fave; 
	   return true;   

   }

   public boolean SetOfflineByIndex(int index, boolean offline){ //Sets whether the item will be saved locally 
	   if ( (index >= articles.size() || (index<0))) {
		   Log.d("SetOfflineByIndex", "if ( (index >= articles.size() || (index<0)))");
		   return false; //illegal parameters, don't throw exception, just ignore
	   }
	   GetArticleByTitle(GetTitleByIndex(index)).offline = offline; 
	   Log.d("SetOfflineByIndex", "GetArticleByTitle(GetTitleByIndex(index)).offline = offline; ");
	   return true;   		   
   }

   public void SetFooter(String footer){
	   this.footer = footer;	   
   }

   public void SetHeader(String header){
	   this.header = header;
   }

   public String GetCompositeHtmlByIndex(int index){

	   String result;
	   String headerHTMLA = "<html><body><h3>";
	   String linkA = "<a href=\"";
	   String linkB = "\">";
	   String linkC = "</a>";
	   String headerHTMLB = "</h3>";
	   String footerHTMLB = "</body></html>";		   		  
	   String pathToImg;

	   if ( (index >= articles.size() || (index<0))) {
		   result = header + "<br/><strong>ERROR! Illegal Index!</strong><br/>" + footer;			   
	   }
	   else{
		   result = header +  GetContentByIndex(index) + footer;
		   String title = GetTitleByIndex(index);
		   ArticleItem item =  GetArticleByTitle(title);
		   if ((useImageCache) && imageCache.containsKey(title)){
			   pathToImg = "file:///" + absolutePath + "/" + imageCache.get(title) + "\""; 
		   }
		   else pathToImg = item.thumbnail + "\"";
		   Log.d("PathToImg", pathToImg);
		   result = headerHTMLA + linkA + GetAddressByTitle(title) + linkB + GetTitleByIndex(index) + linkC 
				   + headerHTMLB + "<h4>" + item.creator + "</h4>" + "<h5>" + item.pubDate + "</h5>" + 				   
				   "<img src=\"" +  pathToImg + " border=\"0\"></img> " +                        
				   GetContentByIndex(index) + footerHTMLB;	
		   Log.d("HTML", result);
 
	   }

	   return result;
   }

   public void clear(){
	   articles.clear(); //Title, Article
	   articleTitles.clear(); //Sorted!		   
	   //imageCache.clear(); //When Initializing, check files? -- Don't clear images for now.
   }
   
}
