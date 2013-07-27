package com.example.testwordget;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
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
	
	private final AndroidHttpClient httpClient = AndroidHttpClient
			.newInstance("joe-android");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TextView text = (TextView)findViewById(R.id.textView1);
		text.setText("test");
		searchWord("love");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	private void searchWord(String word){
		new GetRequest(getWordMatch(word)) {
			@Override
			protected void onPostExecute(JSONObject json) {
				System.out.print(json);
			}
		}.execute();
	}
	
	private HttpUriRequest getWordMatch(String word){
		String url = BASE_URL + "/" + word +  "/json";
		//Log.d("poop",BASE_URL);
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
					Log.d("poop","i am here");
					try {
						Reader reader = new InputStreamReader(response.getEntity().getContent());
						char buf[] = new char[4096];
						StringBuilder builder = new StringBuilder();
						while (reader.read(buf) > 0) {
							builder.append(buf);
						}
						Log.d("poop",builder.toString());
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
