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
		replaceSentence("The girl is cute");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//also note changed xml to allow internet use
	
	//Start of code
	private void replaceSentence(String sentence){
		String[] words  = sentence.split("\\s+");
		getPartOfSpeech(sentence);
	}
	
	
	
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
	
	
	
	
	
	
	private void replaceWords(final String[] sentence, final int index){
		if(index == sentence.length){
			// AT THIS POINT WE HAVE THE FINAL SENTENCE
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
	
	// String/JSON Helper Functions
    //-----------------------------------------
	//-----------------------------------------
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
	
	//Returns largest of three words
	private String maxThree(String one, String two, String three){	
		if(one.length() >= two.length()){
			return one.length() >= three.length() ? one : three;
		}
		return two.length() >= three.length() ? two : three;
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
	
	
	private String getMaxWord(JSONObject json){
		String noun = "";
		String verb = "";
		String adjective = "";
		
		int numNouns = 0;
		int numVerbs = 0;
		int numAdjectives = 0;
		
		//if more nouns assume is nouns
		if(json.has("noun")){
			try {
				JSONArray nouns = json.getJSONObject("noun").getJSONArray("syn");
				noun = maxWord(nouns);
				numNouns = nouns.length();
				//Log.d("poop",noun);
			} catch (JSONException e) {
			}
		}
		if(json.has("verb")){
			try {
				JSONArray verbs = json.getJSONObject("verb").getJSONArray("syn");
				verb = maxWord(verbs);
				numVerbs = verbs.length();
				//Log.d("poop",verb);
			} catch (JSONException e) {
			}
		}
		if(json.has("adjective")){
			try {
				JSONArray adjectives = json.getJSONObject("adjective").getJSONArray("syn");
				adjective = maxWord(adjectives);
				numAdjectives = adjectives.length();
				//Log.d("poop",adjective);
			} catch (JSONException e) {
			}
		}
		
		if(numNouns >= numVerbs){
			return numNouns >= numAdjectives ? noun : adjective;
		}else{
			return numVerbs >= numAdjectives ? verb : adjective;
		}
		
		/*
		String max = maxThree(noun,verb,adjective);
		return max;
		*/
	}
    //-----------------------------------------
	//-----------------------------------------
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
