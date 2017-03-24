// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.fc;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.dwango.cbb.dc.DataChannel;
import jp.co.dwango.cbb.dc.DataChannelCallback;
import jp.co.dwango.cbb.dc.DataChannelHandler;
import jp.co.dwango.cbb.dc.DataChannelResponseHandler;
import jp.co.dwango.cbb.dc.ErrorType;

public class FunctionChannel {
	private final Map<String, Object> instances = new HashMap<String, Object>();
	private final DataChannel channel;
	private final ExecutorService executor;

	/**
	 * function-channelを生成
	 *
	 * @param dataChannel data-functionChannel
	 */
	public FunctionChannel(DataChannel dataChannel) {
		this(dataChannel, 4);
	}

	/**
	 * function-channelを生成
	 *
	 * @param dataChannel        data-functionChannel
	 * @param numberOfThreadPool スレッドプールのスレッド数
	 */
	public FunctionChannel(DataChannel dataChannel, int numberOfThreadPool) {
		executor = Executors.newFixedThreadPool(numberOfThreadPool);
		channel = dataChannel;
		channel.addHandler(new DataChannelHandler() {
			@Override
			public void onPush(Object packet) {
				try {
					String format = ((JSONArray) packet).getString(0);
					JSONArray data = ((JSONArray) packet).getJSONArray(1);
					if (FunctionChannelProtocol.DC_OMI.equals(format)) {
						// 戻り値なしのプロシージャコールを実行
						Pair<Object, Pair<Method, Object[]>> p = getProcedure(null, data);
						if (null != p) {
							try {
								Logger.d("invoke-without-return: " + p.first);
								p.second.first.invoke(p.first, p.second.second);
							} catch (Exception e) {
								Logger.printStackTrace(e);
							}
						} else {
							Logger.e("method not found: " + data);
						}
					} else if (FunctionChannelProtocol.DC_EDO.equals(format)) {
						Logger.w("uncaught DC_EDO: " + packet);
					} else {
						Logger.w("Unknown format: format=" + format + ", data=" + data);
					}
				} catch (JSONException e) {
					Logger.printStackTrace(e);
				}
			}

			@Override
			public void onRequest(Object packet, DataChannelCallback callback) {
				try {
					String format = ((JSONArray) packet).getString(0);
					JSONArray data = ((JSONArray) packet).getJSONArray(1);
					if (FunctionChannelProtocol.DC_OMI.equals(format)) {
						// 戻り値ありのプロシージャコールを実行
						Object result = null;
						Pair<Object, Pair<Method, Object[]>> p = getProcedure(callback, data);
						if (null == p) {
							Logger.e("method not found: " + packet);
							return;
						}
						try {
							Logger.d("invoke-with-return: " + p.first);
							result = p.second.first.invoke(p.first, p.second.second);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
						if (result instanceof Object[]) {
							// 戻り値が配列型の場合はJSONArrayに変換して返す
							JSONArray resultJsonArray = new JSONArray();
							for (Object o : (Object[]) result) {
								resultJsonArray.put(o);
							}
							callback.send(new JSONArray().put(FunctionChannelProtocol.DC_EDO).put(resultJsonArray));
						} else if (result instanceof AsyncResult) {
							// Promiseの場合は非同期実行
							executor.execute(new AsyncTask(callback, (AsyncResult) result));
						} else {
							// その他の場合はそのまま返す
							callback.send(new JSONArray().put(FunctionChannelProtocol.DC_EDO).put(result));
						}
					} else if (FunctionChannelProtocol.DC_EDO.equals(format)) {
						Logger.w("uncaught DC_EDO: " + packet);
						callback.send(new JSONArray().put(FunctionChannelProtocol.DC_ERR).put(FunctionChannelError.InvalidRequest));
					} else {
						Logger.w("Unknown format: format=" + format + ", packet=" + packet);
						callback.send(new JSONArray().put(FunctionChannelProtocol.DC_ERR).put(FunctionChannelError.InvalidRequest));
					}
				} catch (JSONException e) {
					Logger.printStackTrace(e);
					callback.send(new JSONArray().put(FunctionChannelProtocol.DC_ERR).put(FunctionChannelError.InvalidRequest));
				}
			}
		});
	}

	/**
	 * ログ出力の有効化/無効化
	 *
	 * @param enabled true = 有効, false = 無効
	 */
	public static void logging(boolean enabled) {
		Logger.enabled = enabled;
	}

	// 実行要求に対応するプロシージャを取得
	private Pair<Object, Pair<Method, Object[]>> getProcedure(DataChannelCallback callback, JSONArray packet) {
		try {
			ObjectMethodInvocation omi = new ObjectMethodInvocation(packet);
			final Object instance;
			if (!instances.containsKey(omi.id)) {
				Logger.w("does not bind: " + omi.id);
				if (null != callback) {
					callback.send(new JSONArray().put(FunctionChannelProtocol.DC_ERR).put(FunctionChannelError.ObjectNotBound));
				}
				return null;
			}
			instance = instances.get(omi.id);
			Object[] args;
			if (null == omi.arguments || omi.arguments.length() < 1) {
				args = null;
			} else {
				args = new Object[omi.arguments.length()];
				for (int i = 0; i < omi.arguments.length(); i++) {
					args[i] = omi.arguments.get(i);
				}
			}
			Method[] methods = instance.getClass().getMethods();
			for (Method m : methods) {
				if (m.getName().equals(omi.methodName) && isPermitRemoteCall(m)) {
					Type[] a = m.getGenericParameterTypes();
					if (null == args && 0 == a.length || null != args && args.length == a.length) {
						Pair<Method, Object[]> method = new Pair<Method, Object[]>(m, args);
						return new Pair<Object, Pair<Method, Object[]>>(instance, method);
					}
				}
			}
			Logger.w("object-functionChannel-method was not implemented: " + omi.methodName + " (number of args: " + (null == args ? 0 : args.length) + ")");
			if (null != callback) {
				callback.send(new JSONArray().put(FunctionChannelProtocol.DC_ERR).put(FunctionChannelError.MethodNotExist));
			}
			return null;
		} catch (JSONException e) {
			Logger.printStackTrace(e);
		}
		return null;
	}

	// メソッドが cross-border-bridge しても良いかをチェック
	private boolean isPermitRemoteCall(Method m) {
		if (null == m.getAnnotation(CrossBorderMethod.class)) {
			// CrossBorderMethodAsync / CrossBorderMethod が付与されていないメソッドは呼び出し禁止
			Logger.w("Not permitted remote call: " + m.getName());
			return false;
		}
		return true;
	}

	/**
	 * コンテンツ→アプリ方向のRPCを登録
	 *
	 * @param id       インスタンス識別名
	 * @param instance インスタンス
	 */
	public void bind(String id, Object instance) {
		unbind(id);
		Logger.d("binding: " + id + " to " + instance.toString());
		instances.put(id, instance);
	}

	/**
	 * コンテンツ→アプリ方向のRPCを解除
	 *
	 * @param id インスタンス識別名
	 */
	public void unbind(String id) {
		if (instances.containsKey(id)) {
			Logger.d("unbinding: " + id);
			instances.remove(id);
		}
	}

	/**
	 * native → JS 方向のRPCを実行（引数+戻り値なし）
	 *
	 * @param id     インスタンス識別名
	 * @param method メソッド名
	 */
	public void invoke(String id, String method) {
		invoke(id, method, null, null);
	}

	/**
	 * native → JS 方向のRPCを実行（戻り値なし）
	 *
	 * @param id     インスタンス識別名
	 * @param method メソッド名
	 * @param args   引数
	 */
	public void invoke(String id, String method, @Nullable JSONArray args) {
		invoke(id, method, args, null);
	}

	/**
	 * native → JS 方向のRPCを実行（戻り値あり）
	 *
	 * @param id       インスタンス識別名
	 * @param method   メソッド名
	 * @param args     引数
	 * @param callback 応答を受け取るリスナ
	 */
	public void invoke(String id, String method, @Nullable JSONArray args, @Nullable final FunctionChannelCallback callback) {
		JSONArray packet = new JSONArray();
		packet.put(FunctionChannelProtocol.DC_OMI);
		JSONArray data = new JSONArray();
		data.put(id);
		data.put(method);
		data.put(null != args ? args : new JSONArray());
		packet.put(data);
		channel.sendRequest(packet, callback == null ? null : new DataChannelResponseHandler() {
			@Override
			public void onResponse(Object response) {
				try {
					String format = ((JSONArray) response).getString(0);
					Object data = ((JSONArray) response).get(1);
					if (FunctionChannelProtocol.DC_EDO.equals(format)) {
						callback.onResult(false, data);
					} else if (FunctionChannelProtocol.DC_ERR.equals(format)) {
						Logger.e("function-channel error: " + data);
						callback.onResult(true, data);
					} else {
						Logger.w("unknown result format: data=" + data.toString() + ", format=" + format);
					}
				} catch (JSONException e) {
					Logger.printStackTrace(e);
				}
			}

			@Override
			public void onError(ErrorType errorType) {
				Logger.e("data-channel error: " + errorType);
				callback.onResult(true, errorType);
			}
		});
	}

	/**
	 * function-functionChannel を破棄
	 */
	public void destroy() {
		executor.shutdown();
		channel.destroy();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			super.finalize();
		} finally {
			destroy();
		}
	}
}
