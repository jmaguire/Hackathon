package com.example.sesquipedalian;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	//run pigLatin on a string. will convert into piglatin
	
	private static String pigLatin(String s) {
	    String latin = "";
	    int i = 0;
	    while (i<s.length()) {

	      // Take care of punctuation and spaces
	      while (i<s.length() && !isLetter(s.charAt(i))) {
	        latin = latin + s.charAt(i);
	        i++;
	      }

	      // If there aren't any words left, stop.
	      if (i>=s.length()) break;

	      // Otherwise we're at the beginning of a word.
	      int begin = i;
	      while (i<s.length() && isLetter(s.charAt(i))) {
	        i++;
	      }

	      // Now we're at the end of a word, so translate it.
	      int end = i;
	      latin = latin + pigWord(s.substring(begin, end));
	    }
	    return latin;
	  }
	    private static boolean isLetter(char c) {
	    return ( (c >='A' && c <='Z') || (c >='a' && c <='z') );
	  }
	  private static String pigWord(String word) {
	    int split = firstVowel(word);
	    return word.substring(split)+word.substring(0, split)+"ay";
	  }
	  private static int firstVowel(String word) {
	    word = word.toLowerCase();
	    for (int i=0; i<word.length(); i++)
	      if (word.charAt(i)=='a' || word.charAt(i)=='e' ||
	          word.charAt(i)=='i' || word.charAt(i)=='o' ||
	          word.charAt(i)=='u')
	        return i;
	    return 0;
	  }

}
