package com.hy.junl.hybridtest;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.util.Set;

/**
 * @Date:2018/1/3
 * @Desc:
 * @link:http://blog.csdn.net/carson_ho/article/details/64904691
 * @Foreword:但行好事，莫问前程，只需努力每一天。
 * @author:junl_yuan
 */

public class JavaCallJS extends AppCompatActivity{
    private static final String TAG = JavaCallJS.class.getSimpleName();
    private Button button;
    private WebView webview;
    private WebSettings webSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_javacalljs);

        button = (Button)findViewById( R.id.button );
        webview = (WebView)findViewById( R.id.webview );

        webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);// 设置允许JS弹窗

        webview.addJavascriptInterface(new Android2JS(), "test");//AndroidtoJS类对象映射到js的test对象

        webview.loadUrl("file:///android_asset/JavaCallJS.html");

        setChormeClient();
        setWebViewClient();
    }

    private void setWebViewClient() {
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 步骤2：根据协议的参数，判断是否是所需要的url
                // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                //假定传入进来的 url = "js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的)
                Uri uri = Uri.parse(url);
                if (uri.getScheme().equals("js")) {
                    // 如果 authority  = 预先约定协议里的 webview，即代表都符合约定的协议
                    // 所以拦截url,下面JS开始调用Android需要的方法
                    if (uri.getAuthority().equals("webview")) {
                        Log.e("==", "JS开始调用Android方法~");
                        Set<String> names = uri.getQueryParameterNames();
                        for (String name : names) {
                            Log.e("参数名", name.toString());//拿到协议中携带的某个参数名key
                            String parameter = uri.getQueryParameter(name);//拿到协议中携带的某个参数名对应的value
                            Log.e("参数值", parameter);
                            Toast.makeText(JavaCallJS.this, "参数名:" + name.toString() + "  参数值:" + parameter, Toast.LENGTH_SHORT).show();
                        }
                        // android 给js传值  待完善
                        String result = "来自Android的测试数据";
                        webview.loadUrl("javascript:Android2JS("+result+")");
                    }
                }
                return true;
            }
        });
    }

    private void setChormeClient() {
        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(JavaCallJS.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, result.toString());
                        Toast.makeText(JavaCallJS.this, "点击了JS确定按钮，当JS确定按钮被点击时回调", Toast.LENGTH_SHORT).show();
                        result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会一片空白显示不了内容，别的Button也无法点击
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        result.cancel();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(JavaCallJS.this);
                b.setTitle("Confirm");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {

                Uri uri = Uri.parse(message);
                if (uri.getScheme().equals("js")) {
                    if (uri.getAuthority().equals("demo")) {
                        Set<String> names = uri.getQueryParameterNames();
                        for (String name : names) {
                            Log.e("参数名", name.toString());//拿到协议中携带的某个参数名key
                            String parameter = uri.getQueryParameter(name);//拿到协议中携带的某个参数名对应的value
                            Log.e("参数值", parameter);
                            Toast.makeText(JavaCallJS.this, "参数名:" + name.toString() + "  参数值:" + parameter, Toast.LENGTH_SHORT).show();
                        }
                        result.confirm("js调用了Android的方法成功啦");//通过该方法传值给JS
                    }
                }
               return true;
            }
        });
    }

    public void JavaCallJS(View view) {
        // 必须另开线程进行JS方法调用(否则无法调用)
        webview.post(new Runnable() {
            @Override
            public void run() {
                // 注意调用的JS方法名要对应上
                // 调用javascript的callJS()方法
                int version = Build.VERSION.SDK_INT;
                if (version < Build.VERSION_CODES.KITKAT) {
                    //因为该方法在 Android 4.4 版本才可使用，所以使用时需进行版本判断
                    webview.loadUrl("javascript:javaCallJS()");
                    webview.addJavascriptInterface(new Android2JS(), "test");//AndroidtoJS类对象映射到js的test对象
                } else {
                    webview.evaluateJavascript("javascript:javaCallJS()", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            //此处为 js 返回的结果
                        }
                    });
                }
            }
        });

    }

    public void Skip2Another(View view) {
        startActivity(new Intent(JavaCallJS.this, TestAlertActivity.class));
    }
}
