// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.fc;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import jp.co.dwango.cbb.db.DataBus;
import jp.co.dwango.cbb.db.MemoryQueue;
import jp.co.dwango.cbb.db.MemoryQueueDataBus;
import jp.co.dwango.cbb.dc.DataChannel;
import jp.co.dwango.cbb.dc.DataChannelResponseHandler;
import jp.co.dwango.cbb.dc.ErrorType;

public class FunctionChannelTest {
	private DataBus dataBus1;
	private DataBus dataBus2;
	private DataChannel dataChannel1;
	private DataChannel dataChannel2;
	private FunctionChannel functionChannel1;
	private FunctionChannel functionChannel2;
	private MyClassJava myClassJava;

	@Before
	public void setUp() {
		FunctionChannel.logging(false);
	}

	private void before() {
		before(false);
	}

	private void before(boolean closeDataChannel2) {
		MemoryQueue memoryQueue1 = new MemoryQueue();
		MemoryQueue memoryQueue2 = new MemoryQueue();
		dataBus1 = new MemoryQueueDataBus(memoryQueue1, memoryQueue2);
		dataBus2 = new MemoryQueueDataBus(memoryQueue2, memoryQueue1);
		dataChannel1 = new DataChannel(dataBus1);
		dataChannel2 = new DataChannel(dataBus2);
		functionChannel1 = new FunctionChannel(dataChannel1);
		functionChannel2 = new FunctionChannel(dataChannel2);
		myClassJava = new MyClassJava();
		functionChannel2.bind("MyClassJava", myClassJava);
		if (closeDataChannel2) {
			dataChannel2.destroy();
		}
	}

	private void after() {
		functionChannel2.unbind("MyClassJava");
		// 念のため unbind が機能しているかチェックしておく
		functionChannel1.invoke("MyClassJava", "countUp", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertTrue(isError);
			}
		});
		functionChannel1.destroy();
		functionChannel2.destroy();
		dataChannel1.destroy();
		dataChannel2.destroy();
		dataBus1.destroy();
		dataBus2.destroy();
	}

	@Test
	public void test_正常ケース_引数と戻り値が無いRPC() {
		before();
		functionChannel1.invoke("MyClassJava", "countUp", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertFalse(isError);
			}
		});
		Assert.assertEquals(1, myClassJava.getCount());
		after();
	}

	@Test
	public void test_正常ケース_戻り値のみ有るRPC() {
		before();
		myClassJava.countUp();
		functionChannel1.invoke("MyClassJava", "getCount", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertFalse(isError);
				Assert.assertEquals(1, result);
			}
		});
		myClassJava.countDown();
		Assert.assertEquals(0, myClassJava.getCount());
		after();
	}

	@Test
	public void test_正常ケース_引数と戻り値があるRPC() {
		before();
		functionChannel1.invoke("MyClassJava", "add", new JSONArray().put(2525).put(4649), new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertFalse(isError);
				Assert.assertEquals(2525 + 4649, result);
				myClassJava.countUp();
			}
		});
		Assert.assertEquals(1, myClassJava.getCount());
		after();
	}

	@Test
	public void test_異常ケース_bindされていないオブジェクト() {
		before();
		functionChannel1.invoke("MyClassJavaX", "add", new JSONArray().put(2525).put(4649), new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertTrue(isError);
				myClassJava.countUp();
			}
		});
		Assert.assertEquals(1, myClassJava.getCount());
		after();
	}

	@Test
	public void test_異常ケース_アクセス権の無いメソッド() {
		before();
		functionChannel1.invoke("MyClassJava", "reset", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertTrue(isError);
				myClassJava.countUp();
			}
		});
		Assert.assertEquals(1, myClassJava.getCount());
		after();
	}

	@Test
	public void test_異常ケース_存在しない無いメソッド() {
		before();
		functionChannel1.invoke("MyClassJava", "not-exist-method", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertTrue(isError);
				myClassJava.countUp();
			}
		});
		Assert.assertEquals(1, myClassJava.getCount());
		after();
	}

	@Test
	public void test_異常ケース_アノテーションが付与されていないメソッド() {
		before();
		functionChannel1.invoke("MyClassJava", "notPermittedMethod", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertTrue(isError);
				Assert.assertEquals(FunctionChannelError.MethodNotExist.toString(), result);
				myClassJava.countUp();
			}
		});
		Assert.assertEquals(1, myClassJava.getCount());
		after();
	}

	@Test
	public void test_異常ケース_引数の数が異なるメソッド() {
		before();
		functionChannel1.invoke("MyClassJava", "add", new JSONArray().put(2525).put(4649).put(2828), new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertTrue(isError);
				myClassJava.countUp();
			}
		});
		Assert.assertEquals(1, myClassJava.getCount());
		after();
	}

	@Test
	public void test_正常ケース_非同期() {
		before();
		FunctionChannelCallback callback = Mockito.mock(FunctionChannelCallback.class);
		functionChannel1.invoke("MyClassJava", "countUpAsync", null, callback);
		// 実行直後は完了していない
		Assert.assertEquals(0, myClassJava.getCount());
		// 300msほどで完了するので最大1秒待機
		Mockito.verify(callback, Mockito.timeout(1000)).onResult(false, 1);
		Assert.assertEquals(1, myClassJava.getCount());
		after();
	}

	@Test
	public void test_正常ケース_同期だが戻り値を拾わない() {
		before();
		functionChannel1.invoke("MyClassJava", "countUp");
		functionChannel1.invoke("MyClassJava", "add", new JSONArray().put(1).put(2));
		Assert.assertEquals(1, myClassJava.getCount());
		after();
	}

	@Test
	public void test_正常ケース_unbindせずに再bind() {
		before();
		MyClassJava myClassJava2 = new MyClassJava();
		functionChannel2.bind("MyClassJava", myClassJava2);
		functionChannel1.invoke("MyClassJava", "countUp", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertFalse(isError);
			}
		});
		Assert.assertEquals(0, myClassJava.getCount());
		Assert.assertEquals(1, myClassJava2.getCount());
		after();
	}

	@Test
	public void test_異常ケース_相手方のDataChannelをわざと切断() {
		before(true);
		functionChannel1.invoke("MyClassJava", "countUp", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				Assert.assertTrue(isError);
			}
		});
		after();
	}

	@Test
	public void test_正常ケース_戻り値がString配列() {
		before();
		functionChannel1.invoke("MyClassJava", "getStringArray", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				JSONArray resultArray = (JSONArray) result;
				Assert.assertEquals(3, resultArray.length());
				try {
					Assert.assertEquals("one", resultArray.getString(0));
					Assert.assertEquals("two", resultArray.getString(1));
					Assert.assertEquals("three", resultArray.getString(2));
				} catch (JSONException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		});
		after();
	}

	@Test
	public void test_正常ケース_戻り値がint配列() {
		before();
		functionChannel1.invoke("MyClassJava", "getIntArray", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				JSONArray resultArray = (JSONArray) result;
				Assert.assertEquals(3, resultArray.length());
				try {
					Assert.assertEquals(1001, resultArray.getInt(0));
					Assert.assertEquals(2020, resultArray.getInt(1));
					Assert.assertEquals(3333, resultArray.getInt(2));
				} catch (JSONException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		});
		after();
	}

	@Test
	public void test_正常ケース_戻り値がany配列() {
		before();
		functionChannel1.invoke("MyClassJava", "getAnyArray", null, new FunctionChannelCallback() {
			@Override
			public void onResult(boolean isError, Object result) {
				JSONArray resultArray = (JSONArray) result;
				Assert.assertEquals(5, resultArray.length());
				try {
					Assert.assertEquals(1, resultArray.getInt(0));
					Assert.assertEquals("two", resultArray.getString(1));
					Assert.assertEquals(true, resultArray.getBoolean(2));
					JSONArray arg4 = resultArray.getJSONArray(3);
					Assert.assertEquals(3, arg4.length());
					Assert.assertEquals("one", arg4.getString(0));
					Assert.assertEquals("two", arg4.getString(1));
					Assert.assertEquals("three", arg4.getString(2));
					JSONArray arg5 = resultArray.getJSONArray(4);
					Assert.assertEquals(3, arg5.length());
					Assert.assertEquals(1001, arg5.getInt(0));
					Assert.assertEquals(2020, arg5.getInt(1));
					Assert.assertEquals(3333, arg5.getInt(2));
				} catch (JSONException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		});
		after();
	}

	@Test
	public void test_異常ケース_不正リクエスト1_存在しないリクエスト() {
		before();
		functionChannel2.bind("MyClassJava", new MyClassJava());
		dataChannel1.sendRequest(new JSONArray().put("foo").put("foo"), new DataChannelResponseHandler() {
			@Override
			public void onResponse(Object o) {
				try {
					Assert.assertEquals(FunctionChannelProtocol.DC_ERR, ((JSONArray) o).get(0));
					Assert.assertEquals(FunctionChannelError.InvalidRequest.toString(), ((JSONArray) o).get(1));
				} catch (JSONException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}

			@Override
			public void onError(ErrorType s) {
				Assert.fail("ErrorType: " + s);
			}
		});
		after();
	}

	@Test
	public void test_異常ケース_不正リクエスト2_EDOをリクエストで投げる() {
		before();
		functionChannel2.bind("MyClassJava", new MyClassJava());
		dataChannel1.sendRequest(new JSONArray().put(FunctionChannelProtocol.DC_EDO).put("result"), new DataChannelResponseHandler() {
			@Override
			public void onResponse(Object o) {
				try {
					Assert.assertEquals(FunctionChannelProtocol.DC_ERR, ((JSONArray) o).get(0));
					Assert.assertEquals(FunctionChannelError.InvalidRequest.toString(), ((JSONArray) o).get(1));
				} catch (JSONException e) {
					Assert.fail();
					e.printStackTrace();
				}
			}

			@Override
			public void onError(ErrorType s) {
				Assert.fail("ErrorType:" + s);
			}
		});
		// カバレッジを上げるため一応PUSHも投げておく
		dataChannel1.sendPush(new JSONArray().put(FunctionChannelProtocol.DC_EDO).put("result"));
		after();
	}

	@Test
	public void test_正常ケース_マルチスレッド() throws InterruptedException {
		before();
		final int tryCount = 1000;
		final int threadCount = 10;
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < tryCount; i++) {
						functionChannel1.invoke("MyClassJava", "countUp", null, new FunctionChannelCallback() {
							@Override
							public void onResult(boolean isError, Object result) {
								Assert.assertFalse(isError);
							}
						});
					}
				}
			});
			threads[i].start();
		}
		for (Thread t : threads) {
			t.join();
		}
		Assert.assertEquals(tryCount * threadCount, myClassJava.getCount());
		after();
	}
}