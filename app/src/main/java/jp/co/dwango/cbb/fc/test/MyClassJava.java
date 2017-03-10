// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.fc.test;

import jp.co.dwango.cbb.fc.AsyncCallback;
import jp.co.dwango.cbb.fc.AsyncResult;
import jp.co.dwango.cbb.fc.CrossBorderMethod;

class MyClassJava {
	// 引数に指定されたテキストを結合して返すメソッド（同期）
	@CrossBorderMethod
	public String foo(String a1, String a2, String a3) {
		return a1 + a2 + a3;
	}

	// 引数に指定されたテキストを結合して返すメソッド（非同期）
	// ※非同期であることを分かり易くするため, 3秒後に応答を返す
	@CrossBorderMethod
	public AsyncResult<String> fooA(final String a1, final String a2, final String a3) {
		// AsyncResult<Type> を new して return
		return new AsyncResult<String>() {
			@Override
			public void run(AsyncCallback<String> callback) {
				// 3秒間sleep
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// callback#onResult で 戻り値を返す
				callback.onResult(a1 + a2 + a3);
			}
		};
	}
}
