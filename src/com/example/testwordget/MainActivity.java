package com.example.testwordget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URL;
import java.io.InputStreamReader;
import java.io.Reader;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private String BASE_URL = "http://words.bighugelabs.com/api/2/95723594121b497f2f7f62013fc84eaa";
	private String STANFORD_URL = "http://nlp.stanford.edu:8080/parser/index.jsp?query=";
	private String newSentence = ""; 
	
	private Set<String> articles = new HashSet<String>(Arrays.asList("a","an","the","some"));
	private Map<String,String> partOfSpeech = new HashMap<String,String>();
	
	private final AndroidHttpClient httpClient = AndroidHttpClient
			.newInstance("joe-android");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TextView text = (TextView)findViewById(R.id.textView1);
		text.setText("the");
		replaceSentence("The quick brown dog jumps over the fence");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//also note changed xml to allow internet use
	//------------------------------------------------------------
	//----------------PASS THE SENTENCE INTO HERE-----------------
	//------------------------------------------------------------
	
	private void replaceSentence(String sentence){
		sentence = sentence.replaceAll("\\.","");
		sentence = sentence.replaceAll("\\,","");
		sentence = sentence.replaceAll("\\?","");
		sentence = sentence.replaceAll("\\!","");
		String[] words  = sentence.split("\\s+");
		for (int i = 0; i < words.length; i++) Log.d("poop", "Here " + words[i]);
		getPartOfSpeech(sentence);
	}
	
	
	// Get's part of speech by quering the stanford nlp server
	// uses a different get request.. get request string
	private void getPartOfSpeech(final String sentence){

		new GetRequestString(getPOSRequest(URLEncoder.encode(sentence))) {
			@Override
			protected void onPostExecute(String string) {
				String html = Html.fromHtml(string).toString();
				
				String[] words  = sentence.split("\\s+");
				
				
				for (int i = 0; i < words.length; i++){
					String match = words[i]+"/";
					int index1 = html.indexOf(match);
					int index2 = html.indexOf("\n", index1);
					String matchPOS = html.substring(index1 + match.length(), index2);
					partOfSpeech.put(words[i], matchPOS);
				}
				
				replaceWords(words, 0);
			}
		}.execute();
	}
	
	
	
	
	//------------------------------------------------------------
	//----------------FINISHES BELOW-----------------
	//------------------------------------------------------------
	
	
	private void replaceWords(final String[] sentence, final int index){
		if(index == sentence.length){
			//------------------------------------------------------------
			//newSentence is complete here
			//call a new function and pass newSentence as a parameter.
			//have it read newSentence
			//------------------------------------------------------------
			
			TextView text = (TextView)findViewById(R.id.textView1);
			text.setText(newSentence);
			Log.d("poop",newSentence);
			return;
		}
		
		final String word = sentence[index];
		
		new GetRequest(getWordMatch(word)) {
			@Override
			protected void onPostExecute(JSONObject json) {
				String max = "";
				String pos = partOfSpeech.get(word);
				if(json == null){
					max = word;
				}else if(pos.indexOf("NN") != -1){
					max = getMaxNoun(json);
				}else if(pos.indexOf("VB") != -1){
					max = getMaxVerb(json);
				}else if(pos.indexOf("JJ") != -1){
					max = getMaxAdjective(json);
				}else{
					max = word;
				}
				newSentence = newSentence + max + " "; 
				replaceWords(sentence, index + 1);
			}
		}.execute();
	}
	
	
    //-----------------------------------------
	// String/JSON Helper Functions
	//-----------------------------------------
	
	//Returns largest word in JSONArray
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
	
	
	private String getMaxNoun(JSONObject json){
		String noun = "";
		if(json.has("noun")){
			try {
				JSONArray nouns = json.getJSONObject("noun").getJSONArray("syn");
				noun = maxWord(nouns);
			} catch (JSONException e) {
			}
		}
		return noun;
	}
	
	private String getMaxVerb(JSONObject json){
		String verb = "";
		if(json.has("verb")){
			try {
				JSONArray verbs = json.getJSONObject("verb").getJSONArray("syn");
				verb = maxWord(verbs);
			} catch (JSONException e) {
			}
		}
		return verb;
	}
	
	private String getMaxAdjective(JSONObject json){
		String adjective = "";
		if(json.has("adjective")){
			try {
				JSONArray adjectives = json.getJSONObject("adjective").getJSONArray("syn");
				adjective = maxWord(adjectives);
			} catch (JSONException e) {
			}
		}
		return adjective;
	}
	
	
	
    //-----------------------------------------
	//HTML GET SHIT
	//-----------------------------------------
	private HttpUriRequest getPOSRequest(String sentence){
		String url = STANFORD_URL + sentence;
		return new HttpGet(url);
	}
	
	
	private HttpUriRequest getWordMatch(String word){
		String url = BASE_URL + "/" + word +  "/json";
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
    
    private class GetRequestString extends AsyncTask<Void, Void, String> {
		private HttpUriRequest getRequest;
		
		public GetRequestString(HttpUriRequest req) {
			this.getRequest = req;
		}
		
		@Override
		protected String doInBackground(Void... requests) {
			try {
				HttpResponse response = httpClient.execute(getRequest);
				if (response != null) {
					try {
						Reader reader = new InputStreamReader(response.getEntity().getContent());
						
						char buf[] = new char[16000];
						StringBuilder builder = new StringBuilder();
						while (reader.read(buf) > 0) {
							builder.append(buf);
						}
						return builder.toString();
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
