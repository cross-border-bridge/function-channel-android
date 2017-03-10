require=(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
// Copyright © 2017 DWANGO Co., Ltd.
"use strict";
/**
 * data-channel format
 */
var FMT_OMI = 'omi'; // Object Method Invocation
var FMT_EDO = 'edo'; // EncoDed Object
var FMT_ERR = 'err'; // ERRor
/**
 * error-type of function channel
 */
var ERROR_TYPE_OBJECT_NOT_BOUND = 'ObjectNotBound';
var ERROR_TYPE_METHOD_NOT_EXIST = 'MethodNotExist';
var FunctionChannel = (function () {
    /**
     * コンストラクタ (FunctionChannel を生成する)
     *
     * @param dataChannel DataChannel
     */
    function FunctionChannel(dataChannel) {
        this._bindingObjects = {};
        this._dataChannel = dataChannel;
        this._onReceivePacket = this.onReceivePacket.bind(this);
        this._dataChannel.addHandler(this._onReceivePacket);
    }
    /**
     * デストラクタ (FunctionChannel を破棄)
     */
    FunctionChannel.prototype.destroy = function () {
        if (!this._dataChannel)
            return;
        this._dataChannel.removeHandler(this._onReceivePacket);
        this._dataChannel = undefined;
        this._onReceivePacket = undefined;
        this._bindingObjects = {};
    };
    /**
     * 破棄済みか確認する
     *
     * @return 結果（true: 破棄済み, false: 破棄されていない）
     */
    FunctionChannel.prototype.destroyed = function () {
        return !this._dataChannel;
    };
    /**
     * オブジェクト識別子 と オブジェクト を紐付ける
     *
     * @param id オブジェクト識別子
     * @param object オブジェクト
     */
    FunctionChannel.prototype.bind = function (id, object) {
        if (!this._dataChannel)
            return;
        this._bindingObjects[id] = object;
    };
    /**
     * オブジェクト識別子 の紐付けを解除する
     *
     * @param id オブジェクト識別子
     */
    FunctionChannel.prototype.unbind = function (id) {
        if (!this._dataChannel)
            return;
        delete this._bindingObjects[id];
    };
    /**
     * 端方(native側) で bind されているオブジェクトのメソッドを実行する
     *
     * @param id 端方で bind されているオブジェクト識別子
     * @param method 実行するメソッド名
     * @param args 実行するメソッドに指定する引数
     * @param [callback] 実行結果の戻り値を受け取るハンドラ（戻り値が不要な場合は指定してなくよい）
     * @param [timeout] 応答待ちのタイムアウト
     */
    FunctionChannel.prototype.invoke = function (id, method, args, callback, timeout) {
        if (!this._dataChannel)
            return;
        var dcc;
        if (callback) {
            dcc = function (error, packet) {
                if (error) {
                    callback.apply(this, [error]);
                }
                else if (FMT_ERR === packet[0]) {
                    callback.apply(this, [packet[1]]);
                }
                else {
                    callback.apply(this, [undefined, packet[1]]);
                }
            };
        }
        else {
            dcc = undefined;
        }
        this._dataChannel.send([FMT_OMI, [id, method, args]], dcc, timeout);
    };
    FunctionChannel.prototype.onReceivePacket = function (packet, callback) {
        if (!this._dataChannel)
            return;
        if (packet[0] === FMT_OMI) {
            this.dispatchMethodInvocation(packet[1][0], packet[1][1], packet[1][2], callback);
        }
        else {
            console.warn('unknown format', packet[0]);
        }
    };
    FunctionChannel.prototype.dispatchMethodInvocation = function (id, methodName, args, callback) {
        if (!this._bindingObjects[id]) {
            if (callback)
                callback([FMT_ERR, ERROR_TYPE_OBJECT_NOT_BOUND]);
            return;
        }
        if (!this._bindingObjects[id][methodName]) {
            if (callback)
                callback([FMT_ERR, ERROR_TYPE_METHOD_NOT_EXIST]);
            return;
        }
        var result = (_a = this._bindingObjects[id])[methodName].apply(_a, args);
        if (callback)
            callback([FMT_EDO, result]);
        var _a;
    };
    return FunctionChannel;
}());
exports.FunctionChannel = FunctionChannel;

},{}],"@cross-border-bridge/function-channel":[function(require,module,exports){
// Copyright © 2017 DWANGO Co., Ltd.
"use strict";
var FunctionChannel_1 = require('./FunctionChannel');
exports.FunctionChannel = FunctionChannel_1.FunctionChannel;

},{"./FunctionChannel":1}]},{},[])
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJsaWIvRnVuY3Rpb25DaGFubmVsLmpzIiwibGliL2luZGV4LmpzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0FDQUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzdIQTtBQUNBO0FBQ0E7QUFDQTtBQUNBIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsIi8vIENvcHlyaWdodCDCqSAyMDE3IERXQU5HTyBDby4sIEx0ZC5cblwidXNlIHN0cmljdFwiO1xuLyoqXG4gKiBkYXRhLWNoYW5uZWwgZm9ybWF0XG4gKi9cbnZhciBGTVRfT01JID0gJ29taSc7IC8vIE9iamVjdCBNZXRob2QgSW52b2NhdGlvblxudmFyIEZNVF9FRE8gPSAnZWRvJzsgLy8gRW5jb0RlZCBPYmplY3RcbnZhciBGTVRfRVJSID0gJ2Vycic7IC8vIEVSUm9yXG4vKipcbiAqIGVycm9yLXR5cGUgb2YgZnVuY3Rpb24gY2hhbm5lbFxuICovXG52YXIgRVJST1JfVFlQRV9PQkpFQ1RfTk9UX0JPVU5EID0gJ09iamVjdE5vdEJvdW5kJztcbnZhciBFUlJPUl9UWVBFX01FVEhPRF9OT1RfRVhJU1QgPSAnTWV0aG9kTm90RXhpc3QnO1xudmFyIEZ1bmN0aW9uQ2hhbm5lbCA9IChmdW5jdGlvbiAoKSB7XG4gICAgLyoqXG4gICAgICog44Kz44Oz44K544OI44Op44Kv44K/IChGdW5jdGlvbkNoYW5uZWwg44KS55Sf5oiQ44GZ44KLKVxuICAgICAqXG4gICAgICogQHBhcmFtIGRhdGFDaGFubmVsIERhdGFDaGFubmVsXG4gICAgICovXG4gICAgZnVuY3Rpb24gRnVuY3Rpb25DaGFubmVsKGRhdGFDaGFubmVsKSB7XG4gICAgICAgIHRoaXMuX2JpbmRpbmdPYmplY3RzID0ge307XG4gICAgICAgIHRoaXMuX2RhdGFDaGFubmVsID0gZGF0YUNoYW5uZWw7XG4gICAgICAgIHRoaXMuX29uUmVjZWl2ZVBhY2tldCA9IHRoaXMub25SZWNlaXZlUGFja2V0LmJpbmQodGhpcyk7XG4gICAgICAgIHRoaXMuX2RhdGFDaGFubmVsLmFkZEhhbmRsZXIodGhpcy5fb25SZWNlaXZlUGFja2V0KTtcbiAgICB9XG4gICAgLyoqXG4gICAgICog44OH44K544OI44Op44Kv44K/IChGdW5jdGlvbkNoYW5uZWwg44KS56C05qOEKVxuICAgICAqL1xuICAgIEZ1bmN0aW9uQ2hhbm5lbC5wcm90b3R5cGUuZGVzdHJveSA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgaWYgKCF0aGlzLl9kYXRhQ2hhbm5lbClcbiAgICAgICAgICAgIHJldHVybjtcbiAgICAgICAgdGhpcy5fZGF0YUNoYW5uZWwucmVtb3ZlSGFuZGxlcih0aGlzLl9vblJlY2VpdmVQYWNrZXQpO1xuICAgICAgICB0aGlzLl9kYXRhQ2hhbm5lbCA9IHVuZGVmaW5lZDtcbiAgICAgICAgdGhpcy5fb25SZWNlaXZlUGFja2V0ID0gdW5kZWZpbmVkO1xuICAgICAgICB0aGlzLl9iaW5kaW5nT2JqZWN0cyA9IHt9O1xuICAgIH07XG4gICAgLyoqXG4gICAgICog56C05qOE5riI44G/44GL56K66KqN44GZ44KLXG4gICAgICpcbiAgICAgKiBAcmV0dXJuIOe1kOaenO+8iHRydWU6IOegtOajhOa4iOOBvywgZmFsc2U6IOegtOajhOOBleOCjOOBpuOBhOOBquOBhO+8iVxuICAgICAqL1xuICAgIEZ1bmN0aW9uQ2hhbm5lbC5wcm90b3R5cGUuZGVzdHJveWVkID0gZnVuY3Rpb24gKCkge1xuICAgICAgICByZXR1cm4gIXRoaXMuX2RhdGFDaGFubmVsO1xuICAgIH07XG4gICAgLyoqXG4gICAgICog44Kq44OW44K444Kn44Kv44OI6K2Y5Yil5a2QIOOBqCDjgqrjg5bjgrjjgqfjgq/jg4gg44KS57SQ5LuY44GR44KLXG4gICAgICpcbiAgICAgKiBAcGFyYW0gaWQg44Kq44OW44K444Kn44Kv44OI6K2Y5Yil5a2QXG4gICAgICogQHBhcmFtIG9iamVjdCDjgqrjg5bjgrjjgqfjgq/jg4hcbiAgICAgKi9cbiAgICBGdW5jdGlvbkNoYW5uZWwucHJvdG90eXBlLmJpbmQgPSBmdW5jdGlvbiAoaWQsIG9iamVjdCkge1xuICAgICAgICBpZiAoIXRoaXMuX2RhdGFDaGFubmVsKVxuICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICB0aGlzLl9iaW5kaW5nT2JqZWN0c1tpZF0gPSBvYmplY3Q7XG4gICAgfTtcbiAgICAvKipcbiAgICAgKiDjgqrjg5bjgrjjgqfjgq/jg4jorZjliKXlrZAg44Gu57SQ5LuY44GR44KS6Kej6Zmk44GZ44KLXG4gICAgICpcbiAgICAgKiBAcGFyYW0gaWQg44Kq44OW44K444Kn44Kv44OI6K2Y5Yil5a2QXG4gICAgICovXG4gICAgRnVuY3Rpb25DaGFubmVsLnByb3RvdHlwZS51bmJpbmQgPSBmdW5jdGlvbiAoaWQpIHtcbiAgICAgICAgaWYgKCF0aGlzLl9kYXRhQ2hhbm5lbClcbiAgICAgICAgICAgIHJldHVybjtcbiAgICAgICAgZGVsZXRlIHRoaXMuX2JpbmRpbmdPYmplY3RzW2lkXTtcbiAgICB9O1xuICAgIC8qKlxuICAgICAqIOerr+aWuShuYXRpdmXlgbQpIOOBpyBiaW5kIOOBleOCjOOBpuOBhOOCi+OCquODluOCuOOCp+OCr+ODiOOBruODoeOCveODg+ODieOCkuWun+ihjOOBmeOCi1xuICAgICAqXG4gICAgICogQHBhcmFtIGlkIOerr+aWueOBpyBiaW5kIOOBleOCjOOBpuOBhOOCi+OCquODluOCuOOCp+OCr+ODiOitmOWIpeWtkFxuICAgICAqIEBwYXJhbSBtZXRob2Qg5a6f6KGM44GZ44KL44Oh44K944OD44OJ5ZCNXG4gICAgICogQHBhcmFtIGFyZ3Mg5a6f6KGM44GZ44KL44Oh44K944OD44OJ44Gr5oyH5a6a44GZ44KL5byV5pWwXG4gICAgICogQHBhcmFtIFtjYWxsYmFja10g5a6f6KGM57WQ5p6c44Gu5oi744KK5YCk44KS5Y+X44GR5Y+W44KL44OP44Oz44OJ44Op77yI5oi744KK5YCk44GM5LiN6KaB44Gq5aC05ZCI44Gv5oyH5a6a44GX44Gm44Gq44GP44KI44GE77yJXG4gICAgICogQHBhcmFtIFt0aW1lb3V0XSDlv5znrZTlvoXjgaHjga7jgr/jgqTjg6DjgqLjgqbjg4hcbiAgICAgKi9cbiAgICBGdW5jdGlvbkNoYW5uZWwucHJvdG90eXBlLmludm9rZSA9IGZ1bmN0aW9uIChpZCwgbWV0aG9kLCBhcmdzLCBjYWxsYmFjaywgdGltZW91dCkge1xuICAgICAgICBpZiAoIXRoaXMuX2RhdGFDaGFubmVsKVxuICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICB2YXIgZGNjO1xuICAgICAgICBpZiAoY2FsbGJhY2spIHtcbiAgICAgICAgICAgIGRjYyA9IGZ1bmN0aW9uIChlcnJvciwgcGFja2V0KSB7XG4gICAgICAgICAgICAgICAgaWYgKGVycm9yKSB7XG4gICAgICAgICAgICAgICAgICAgIGNhbGxiYWNrLmFwcGx5KHRoaXMsIFtlcnJvcl0pO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICBlbHNlIGlmIChGTVRfRVJSID09PSBwYWNrZXRbMF0pIHtcbiAgICAgICAgICAgICAgICAgICAgY2FsbGJhY2suYXBwbHkodGhpcywgW3BhY2tldFsxXV0pO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICBlbHNlIHtcbiAgICAgICAgICAgICAgICAgICAgY2FsbGJhY2suYXBwbHkodGhpcywgW3VuZGVmaW5lZCwgcGFja2V0WzFdXSk7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfTtcbiAgICAgICAgfVxuICAgICAgICBlbHNlIHtcbiAgICAgICAgICAgIGRjYyA9IHVuZGVmaW5lZDtcbiAgICAgICAgfVxuICAgICAgICB0aGlzLl9kYXRhQ2hhbm5lbC5zZW5kKFtGTVRfT01JLCBbaWQsIG1ldGhvZCwgYXJnc11dLCBkY2MsIHRpbWVvdXQpO1xuICAgIH07XG4gICAgRnVuY3Rpb25DaGFubmVsLnByb3RvdHlwZS5vblJlY2VpdmVQYWNrZXQgPSBmdW5jdGlvbiAocGFja2V0LCBjYWxsYmFjaykge1xuICAgICAgICBpZiAoIXRoaXMuX2RhdGFDaGFubmVsKVxuICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICBpZiAocGFja2V0WzBdID09PSBGTVRfT01JKSB7XG4gICAgICAgICAgICB0aGlzLmRpc3BhdGNoTWV0aG9kSW52b2NhdGlvbihwYWNrZXRbMV1bMF0sIHBhY2tldFsxXVsxXSwgcGFja2V0WzFdWzJdLCBjYWxsYmFjayk7XG4gICAgICAgIH1cbiAgICAgICAgZWxzZSB7XG4gICAgICAgICAgICBjb25zb2xlLndhcm4oJ3Vua25vd24gZm9ybWF0JywgcGFja2V0WzBdKTtcbiAgICAgICAgfVxuICAgIH07XG4gICAgRnVuY3Rpb25DaGFubmVsLnByb3RvdHlwZS5kaXNwYXRjaE1ldGhvZEludm9jYXRpb24gPSBmdW5jdGlvbiAoaWQsIG1ldGhvZE5hbWUsIGFyZ3MsIGNhbGxiYWNrKSB7XG4gICAgICAgIGlmICghdGhpcy5fYmluZGluZ09iamVjdHNbaWRdKSB7XG4gICAgICAgICAgICBpZiAoY2FsbGJhY2spXG4gICAgICAgICAgICAgICAgY2FsbGJhY2soW0ZNVF9FUlIsIEVSUk9SX1RZUEVfT0JKRUNUX05PVF9CT1VORF0pO1xuICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICB9XG4gICAgICAgIGlmICghdGhpcy5fYmluZGluZ09iamVjdHNbaWRdW21ldGhvZE5hbWVdKSB7XG4gICAgICAgICAgICBpZiAoY2FsbGJhY2spXG4gICAgICAgICAgICAgICAgY2FsbGJhY2soW0ZNVF9FUlIsIEVSUk9SX1RZUEVfTUVUSE9EX05PVF9FWElTVF0pO1xuICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICB9XG4gICAgICAgIHZhciByZXN1bHQgPSAoX2EgPSB0aGlzLl9iaW5kaW5nT2JqZWN0c1tpZF0pW21ldGhvZE5hbWVdLmFwcGx5KF9hLCBhcmdzKTtcbiAgICAgICAgaWYgKGNhbGxiYWNrKVxuICAgICAgICAgICAgY2FsbGJhY2soW0ZNVF9FRE8sIHJlc3VsdF0pO1xuICAgICAgICB2YXIgX2E7XG4gICAgfTtcbiAgICByZXR1cm4gRnVuY3Rpb25DaGFubmVsO1xufSgpKTtcbmV4cG9ydHMuRnVuY3Rpb25DaGFubmVsID0gRnVuY3Rpb25DaGFubmVsO1xuIiwiLy8gQ29weXJpZ2h0IMKpIDIwMTcgRFdBTkdPIENvLiwgTHRkLlxuXCJ1c2Ugc3RyaWN0XCI7XG52YXIgRnVuY3Rpb25DaGFubmVsXzEgPSByZXF1aXJlKCcuL0Z1bmN0aW9uQ2hhbm5lbCcpO1xuZXhwb3J0cy5GdW5jdGlvbkNoYW5uZWwgPSBGdW5jdGlvbkNoYW5uZWxfMS5GdW5jdGlvbkNoYW5uZWw7XG4iXX0=
