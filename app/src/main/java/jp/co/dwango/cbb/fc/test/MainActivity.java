// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.fc.test;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jp.co.dwango.cbb.db.DataBus;
import jp.co.dwango.cbb.db.WebViewDataBus;
import jp.co.dwango.cbb.dc.DataChannel;
import jp.co.dwango.cbb.fc.FunctionChannel;
import jp.co.dwango.cbb.fc.FunctionChannelCallback;

public class MainActivity extends AppCompatActivity {
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// KITKAT以上の場合は Chrome でのデバッグを有効にする
		if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}
		setContentView(R.layout.activity_main);
		WebView webView = (WebView) findViewById(R.id.web_view);
		assert webView != null;

		// デバッグログ出力を有効化
		DataBus.logging(true);
		DataChannel.logging(true);
		FunctionChannel.logging(true);

		// DataBusを用いるWebViewを指定してインスタンス化
		final WebViewDataBus dataBus = new WebViewDataBus(this, webView, true);

		// DataChannelを作成
		final DataChannel dataChannel = new DataChannel(dataBus);

		// FunctionChannelを作成
		final FunctionChannel functionChannel = new FunctionChannel(dataChannel);

		// MyClassJavaのインスタンス を bind
		functionChannel.bind("MyClassJava", new MyClassJava());

		// JavaScript側のメソッドを正常に実行するボタンを準備
		View v = findViewById(R.id.send_request);
		assert v != null;
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				JSONArray args = new JSONArray().put("One").put(2).put("3");
				Log.d("MyApp", "invoking: MyClassJS.foo()");
				functionChannel.invoke("MyClassJS", "foo", args, new FunctionChannelCallback() {
					@Override
					public void onResult(boolean isError, Object result) {
						String text = "isError: " + isError + "\nresult: " + result;
						Log.d("MyApp", text);
						Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		// 未登録インスタンスのメソッドを実行する異常系確認用のボタンを準備
		v = findViewById(R.id.send_request_error1);
		assert v != null;
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				JSONArray args = new JSONArray().put("One").put(2).put("3");
				Log.d("MyApp", "invoking: MyClassJSX.foo()");
				functionChannel.invoke("MyClassJSX", "foo", args, new FunctionChannelCallback() {
					@Override
					public void onResult(boolean isError, Object result) {
						String text = "isError: " + isError + "\nresult: " + result;
						Log.d("MyApp", text);
						Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		// 存在しないメソッドを実行する異常系確認用のボタンを準備
		v = findViewById(R.id.send_request_error2);
		assert v != null;
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				JSONArray args = new JSONArray().put("One").put(2).put("3");
				Log.d("MyApp", "invoking: MyClassJS.fooX()");
				functionChannel.invoke("MyClassJS", "fooX", args, new FunctionChannelCallback() {
					@Override
					public void onResult(boolean isError, Object result) {
						String text = "isError: " + isError + "\nresult: " + result;
						Log.d("MyApp", text);
						Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		// 破棄
		v = findViewById(R.id.destroy);
		assert v != null;
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				functionChannel.destroy();
				dataChannel.destroy();
				dataBus.destroy();
			}
		});

		// WebView を 準備
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
				Log.d("CrossBorderBridge-js", consoleMessage.message() + " (src=" + consoleMessage.sourceId() + ", line=" + consoleMessage.lineNumber() + ")");
				return super.onConsoleMessage(consoleMessage);
			}
		});

		// WebView へコンテンツをロード
		webView.loadDataWithBaseURL("", loadTextFromAssert("html/index.html").replace("$(WEB-VIEW-DATA-BUS)", dataBus.getInjectJavaScript()), "text/html", "UTF-8", null);
	}

	private String loadTextFromAssert(String path) {
		InputStream is = null;
		BufferedReader br = null;
		StringBuilder result = new StringBuilder(16384);
		try {
			is = getAssets().open(path);
			br = new BufferedReader(new InputStreamReader(is));
			String str;
			while ((str = br.readLine()) != null) {
				result.append(str).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != is) try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (null != br) try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}
}
