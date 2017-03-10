// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.fc;

import org.json.JSONArray;
import org.json.JSONException;

class ObjectMethodInvocation {
	final String id;
	final String methodName;
	final JSONArray arguments;

	ObjectMethodInvocation(JSONArray json) throws JSONException {
		id = json.getString(0);
		methodName = json.getString(1);
		arguments = json.isNull(2) ? new JSONArray() : json.getJSONArray(2);
	}
}
