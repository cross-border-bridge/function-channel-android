// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.fc;

abstract public class AsyncResult<T> {
	void execute(AsyncCallback<T> callback) {
		run(callback);
	}

	public abstract void run(final AsyncCallback<T> callback);
}
