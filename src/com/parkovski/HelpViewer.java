package com.parkovski;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpViewer extends Activity {

	private WebView mWebView;
	
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.help );
		mWebView = (WebView) findViewById( R.id.webview );
		
		mWebView.loadUrl( "file:///android_asset/help.html" );
	}

}
