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
 * Credits go also to various authors of tutorials,
 * vogella, mkyong; @ stackoverflow and the Android Team
 *  
 * Version: 16/2/2013
 */

package twenkid.android.basic.news3;

import java.util.ArrayList;
import java.util.Arrays;

import twenkid.android.basic.news3.ArticlesCache.ArticleItem;


import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BasicArrayAdapter extends ArrayAdapter<String> {

	private final Context context;

	public ArrayList<String> values;
	public ArticlesCache articlesCache;
	private final int maxItems = 50;

	public BasicArrayAdapter(Context context, String[] values) {
		super(context, R.layout.fragment_detail_list_imageview, new String[]{values[0]}); //If sent, puts the list twice 12-2-2013
		this.context = context;

		this.values = new ArrayList<String>(maxItems); //(Arrays.asList(values));  //values; !!!** ZARADI TOVA!!! **
		for (int i=1; i<values.length; i++){
			this.values.add(values[i]);
		}

		articlesCache = new ArticlesCache();       
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.fragment_detail_list_imageview, parent, false);
		}

		TextView textView = (TextView) convertView.findViewById(R.id.label);
		ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);

		textView.setText(values.get(position));

		Log.d("BEFORE String s = values.get(position);", "?");
		String s = values.get(position);

		Log.d("BEFORE  ArticleItem item = articlesCache...", "?");

		ArticleItem item = null;
		String title = articlesCache.GetTitleByIndex(position);
		if (title!=null){
			item = articlesCache.GetArticleByTitle(title);
			Log.d("title = ? articlesCache.GetArticleByTitle(title) = ", title);    
		}

		Log.d("AFTER  ArticleItem item = articlesCache...", "?");
		if (item!=null) 
		{        
			Log.d("GetVIEW item!=null !", item.address);
			if (item.fave) {
				imageView.setImageResource(R.drawable.star); //The favorites are starred
				Log.d("Fave ", "TRUE");
			}
			else {imageView.setImageResource(R.drawable.ic_launcher); //Just a bullet, if not a favorite 
			Log.d("Fave ", "FALSE");
			}
		}
		else {
			imageView.setImageResource(R.drawable.ic_launcher); //or something blank?
			Log.d("item=NULL! ", "NULL");
		}

		return convertView;
	}

	@Override
	public void add(String s){
		values.add(s);
		Log.d("Adapter add? " + s, values.get(values.size()-1));
		notifyDataSetChanged();	  
	}

	@Override
	public int getCount(){
		return values.size();
	}


	@Override
	public void clear(){
		values.clear();
		//articlesCache.clear(); //Refactor and Debug, remove "values"
	}

} 
