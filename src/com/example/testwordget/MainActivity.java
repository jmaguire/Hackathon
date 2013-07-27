package com.example.testwordget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private String BASE_URL = "http://words.bighugelabs.com/api/2/95723594121b497f2f7f62013fc84eaa";
	private String newSentence = ""; 
	
	private final AndroidHttpClient httpClient = AndroidHttpClient
			.newInstance("joe-android");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TextView text = (TextView)findViewById(R.id.textView1);
		text.setText("the");
		replaceSentence("The girl is cute");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	private String maxWord(JSONArray array) throws JSONException{
		String val = null;
		int maxLength = -1;
		for(int i = 0; i < array.length(); i++){
			String curr = array.getString(i);
			if(curr.length() > maxLength){
				val = curr;
				maxLength = curr.length();
			}	
		}
		return val;		
	}
	
	private String maxThree(String one, String two, String three){	
		if(one.length() >= two.length()){
			return one.length() >= three.length() ? one : three;
		}
		return two.length() >= three.length() ? two : three;
	}
	
	private String getMaxWord(JSONObject json){
		String noun = "";
		String verb = "";
		String adjective = "";
		
		//if more nouns assume is nouns
		if(json.has("noun")){
			try {
				JSONArray nouns = json.getJSONObject("noun").getJSONArray("syn");
				noun = maxWord(nouns);
				//Log.d("poop",noun);
			} catch (JSONException e) {
			}
		}
		if(json.has("verb")){
			try {
				JSONArray verbs = json.getJSONObject("verb").getJSONArray("syn");
				verb = maxWord(verbs);
				//Log.d("poop",verb);
			} catch (JSONException e) {
			}
		}
		if(json.has("adjective")){
			try {
				JSONArray adjectives = json.getJSONObject("adjective").getJSONArray("syn");
				adjective = maxWord(adjectives);
				//Log.d("poop",adjective);
			} catch (JSONException e) {
			}
		}
		String max = maxThree(noun,verb,adjective);
		return max;
	}

	private void replaceSentence(String sentence){
		String[] words  = sentence.split("\\s+");
		replaceWords(words, 0);
	}
	
	
	private void replaceWords(final String[] sentence, final int index){
		if(index == sentence.length){
			//CALL METHOD TO READ THIS SHIT
			Log.d("poop",newSentence);
			return;
		}
		final String word = sentence[index];
		new GetRequest(getWordMatch(word)) {
			@Override
			protected void onPostExecute(JSONObject json) {
				String max = "";
				if(json != null){
					max = getMaxWord(json);
				} else{
					max = word;
				}
				newSentence = newSentence + max + " "; 
				replaceWords(sentence, index + 1);
			}
		}.execute();
	}
	
	private HttpUriRequest getWordMatch(String word){
		String url = BASE_URL + "/" + word +  "/json";
		Log.d("poop",word);
		return new HttpGet(url);
	}
	
    private class GetRequest extends AsyncTask<Void, Void, JSONObject> {
		private HttpUriRequest getRequest;
		
		public GetRequest(HttpUriRequest req) {
			this.getRequest = req;
		}
		
		@Override
		protected JSONObject doInBackground(Void... requests) {
			try {
				HttpResponse response = httpClient.execute(getRequest);
				if (response != null) {
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
						String builder = reader.readLine();
						JSONTokener tokener = new JSONTokener(builder.toString());
						JSONObject json = new JSONObject(tokener);
						return json;
					} catch (Exception e) {
						return null;
					}
				} else {
					return null;
				}
			} catch (IOException e) {
				return null;
			}
		}
		
	}

	
}
