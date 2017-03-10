// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.fc;

import org.junit.Assert;

public class MyClassJava {
	private int count = 0;

	void reset() {
		count = 0;
	}

	@CrossBorderMethod
	public void countUp() {
		count++;
	}

	@CrossBorderMethod
	public void countDown() {
		count--;
	}

	@CrossBorderMethod
	public int getCount() {
		return count;
	}

	@CrossBorderMethod
	public int add(int a, int b) {
		return a + b;
	}

	@CrossBorderMethod
	public AsyncResult<Integer> countUpAsync() {
		return new AsyncResult<Integer>() {
			@Override
			public void run(AsyncCallback<Integer> callback) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				count++;
				callback.onResult(count);
			}
		};
	}

	@CrossBorderMethod
	public String[] getStringArray() {
		return new String[]{
				"one",
				"two",
				"three"
		};
	}

	@CrossBorderMethod
	public int[] getIntArray() {
		return new int[]{
				1001,
				2020,
				3333
		};
	}

	@CrossBorderMethod
	public Object[] getAnyArray() {
		return new Object[]{
				1,
				"two",
				true,
				getStringArray(),
				getIntArray()
		};
	}

	public void notPermittedMethod() {
		Assert.fail();
	}
}
