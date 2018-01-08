package com.hy.junl.hybridtest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Date:2018/1/2
 * @Desc:
 * @Foreword:但行好事，莫问前程，只需努力每一天。
 * @author:junl_yuan
 *
 *
 * 注意事项：如何避免WebView内存泄露？
 *      1、 不在xml中定义 Webview ，而是在需要的时候在Activity中创建，并且Context使用 getApplicationgContext()
 *      2、在 Activity 销毁（ WebView ）的时候，先让 WebView 加载null内容，然后移除 WebView，再销毁 WebView，最后置空。
 */

public class WebViewActivity extends AppCompatActivity{

    private WebView webView;
    private WebSettings webSettings;
    Handler handler = new Handler();
    private ProgressDialog progressBar;
    private TextView beginLoading,endLoading,loading,mtitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        webView = findViewById(R.id.webview);
        beginLoading = (TextView) findViewById(R.id.text_beginLoading);
        endLoading = (TextView) findViewById(R.id.text_endLoading);
        loading = (TextView) findViewById(R.id.text_Loading);
        mtitle = (TextView) findViewById(R.id.title);


        webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);//支持与JS交互

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

        //方式1. 加载一个网页：
        webView.loadUrl("http://www.baidu.com/");
        //方式2：加载apk包中的html页面
//        webView.loadUrl("file:///android_asset/WebViewActivity.html");
        progressBar = ProgressDialog.show(WebViewActivity.this, "提示", "正在进入网页，请稍后…", false, true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChormeClient());

    }


    @Override
    protected void onResume() {
        super.onResume();
        //激活WebView为活跃状态，能正常执行网页的响应
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //当页面被失去焦点被切换到后台不可见状态，需要执行onPause  通过onPause动作通知内核暂停所有的动作，比如DOM的解析、plugin的执行、JavaScript执行。
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        // 在 Activity 销毁（ WebView ）的时候，先让 WebView 加载null内容，然后移除 WebView，再销毁 WebView，最后置空。
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();

            ((ViewGroup)webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
    // $ git remote add origin git@github.com:junlandroid/HybridTest.git
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /**
         问题：在不做任何处理前提下 ，浏览网页时点击系统的“Back”键,整个 Browser 会调用 finish()而结束自身
         目标：点击返回后，是网页回退而不是退出浏览器
         解决方案：在当前Activity中处理并消费掉该 Back 事件
         */
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 该类作用：处理各种通知 & 请求事件
     *
     */
    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //打开网页时不调用系统浏览器， 而是在本WebView中显示；在网页上的所有加载都经过这个方法,这个函数我们可以做很多操作。
            if (url == null) return false;
            //调用拨号程序
            if (url.startsWith("mailto:") || url.startsWith("geo:") || url.startsWith("tel:") || url.startsWith("smsto:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                WebViewActivity.this.startActivity(intent);
                return true;
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //开始载入页面调用的，我们可以设定一个loading的页面，告诉用户程序在等待网络响应
//            Toast.makeText(WebViewActivity.this,"网页开始加载...",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //在页面加载结束时调用。我们可以关闭loading 条，切换程序动作。
            if (progressBar.isShowing()) {
                progressBar.dismiss();
            }
            Toast.makeText(WebViewActivity.this,"网页加载完毕...",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            //在加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次。
            //设定加载资源的操作

        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);

        }
    }

    /**
     * 该类的作用：辅助 WebView 处理 Javascript 的对话框,网站图标,网站标题等等。
     *
     */
    class MyWebChormeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            //作用：获得网页的加载进度并显示
            if (newProgress < 100) {
                String progress = newProgress + "%";
                loading.setText(progress);
            } else if (newProgress == 100) {
                String progress = newProgress + "%";
                loading.setText(progress);
            }

        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            //获取Web页中的标题
            // 每个网页的页面都有一个标题，比如www.baidu.com这个页面的标题即“百度一下，你就知道”，那么如何知道当前webview正在加载的页面的title并进行设置呢？
            mtitle.setText(title);
        }
    }


}
