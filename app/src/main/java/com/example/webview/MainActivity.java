package com.example.webview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import static android.net.ConnectivityManager.*;

public class MainActivity extends AppCompatActivity {
    WebView webView;
    ProgressBar progressBarWeb;
    Button retry;
    RelativeLayout conncetionProblem;
    //ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Used to open web in full screen
        //Window window=getWindow();
        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        webView=(WebView)findViewById(R.id.webView);
        progressBarWeb=(ProgressBar)findViewById(R.id.progressBar);
        retry=(Button)findViewById(R.id.retry);
        conncetionProblem=(RelativeLayout)findViewById(R.id.internetConncetion);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);


            WebSettings webSettings=webView.getSettings();
            webSettings.setJavaScriptEnabled(true);









        //Check network is available or not
        isNetworkAvailable();






        //swipeRefreshLayout.setColorSchemeColors(#000);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });






        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNetworkAvailable();
            }
        });

      /*
      progress dialog is used to show on mid screen loading
        progressDialog=new ProgressDialog(getApplicationContext());
        progressDialog.setMessage("Loading..");

       */




        webView.setWebViewClient(new WebViewClient());
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

               view.loadUrl(url);
               return true;
            }
        });
        //For downloading content of web in device ,use Dexter library

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, long contentLength) {
                Dexter.withContext(getApplicationContext())
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url));
                                request.setMimeType(contentDisposition);
                                String cookies= CookieManager.getInstance().getCookie(url);
                                request.addRequestHeader("cookies",cookies);
                                request.addRequestHeader("User-agent",userAgent);
                                request.setDescription("Downloading File..........");
                                request.setTitle(URLUtil.guessFileName(url,contentDisposition,mimetype));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS,URLUtil.guessFileName(url,contentDisposition,mimetype));

                                DownloadManager downloadManager=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);

                                Toast.makeText(MainActivity.this, "Downloagin File.....", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                  permissionToken.continuePermissionRequest();
                            }
                        }).check();


            }
        });





        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBarWeb.setVisibility(View.VISIBLE);
                progressBarWeb.setProgress(newProgress);
                setTitle("Loading....");
                //progressDialog.show();
                if(newProgress==100)
                {
                    setTitle(view.getTitle());
                    //progressDialog.dismiss();
                    progressBarWeb.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });



    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack())
        {
            webView.goBack();
        }
        else
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Exit or not")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Did you want to exit")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                        }
                    })
                    .setNegativeButton("No",null)
                    .show();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.forword:
                if(webView.canGoForward())
                {
                    webView.goForward();
                }
                break;
            case R.id.refresh:
                webView.reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    //Checking network is available or not
    public void isNetworkAvailable() {
        // Get Connectivity Manager class object from Systems Service
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get Network Info from connectivity Manager
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = cm.getNetworkInfo(TYPE_MOBILE);

        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if(wifi!=null && wifi.isConnected())
        {
            webView.loadUrl("https://www.google.com");
          webView.setVisibility(View.VISIBLE);
          conncetionProblem.setVisibility(View.GONE);
        }
        else if(mobileNetwork!=null && mobileNetwork.isConnected())
        {
            webView.loadUrl("https://www.google.com");
            webView.setVisibility(View.VISIBLE);
            conncetionProblem.setVisibility(View.GONE);
        }
        else
        {
            webView.setVisibility(View.GONE);
            conncetionProblem.setVisibility(View.VISIBLE);
        }

    }


    //For saving instance which loss after rotation of mobile


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }
}