// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.fc;

import org.json.JSONArray;

import jp.co.dwango.cbb.dc.DataChannelCallback;

class AsyncTask implements Runnable {
	private final DataChannelCallback callback;
	private final AsyncResult asyncResult;

	AsyncTask(DataChannelCallback callback, AsyncResult asyncResult) {
		this.callback = callback;
		this.asyncResult = asyncResult;
	}

	@Override
	public void run() {
		asyncResult.execute(new AsyncCallback() {
			@Override
			public void onResult(Object value) {
				callback.send(new JSONArray().put(FunctionChannelProtocol.DC_EDO).put(value));
			}
		});
	}
}
