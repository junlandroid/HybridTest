package com.hy.junl.hybridtest;

import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * @Date:2018/1/3
 * @Desc:
 * @Foreword:但行好事，莫问前程，只需努力每一天。
 * @author:junl_yuan
 */

public class Android2JS extends Object {
    @JavascriptInterface
    public void hello(String messge) {
        Log.e("==", "JS调用Android代码~~~~");
    }
}
