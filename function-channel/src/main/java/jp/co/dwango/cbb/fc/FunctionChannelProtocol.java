// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.fc;

final class FunctionChannelProtocol {
	static final String DC_OMI = "omi"; // RPCの実行要求
	static final String DC_EDO = "edo"; // RPCの実行が完了した時の応答（その他の用途でも使う可能性がある）
	static final String DC_ERR = "err"; // RPCの実行が失敗した時の応答
}
