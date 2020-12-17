package com.deekshant.browser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity implements TabListSheet.OnTabSheetButtonClick{

    //Declaration
    EditText search;
    ImageButton clear ,menu,forward,copyURL,editURL,shareURL;
    Button tabButton;
    TextView currentURL;
    ImageView currentIcon;
    LinearLayout btnContainer,searchContainer,editTool;
    ProgressBar progressBar;

    WebContainer pager;
    WebContainerAdapter pagerAdapter;
    LayoutInflater inflater;

    TabListSheet tabList;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialization
        search = findViewById(R.id.searchField);
        clear = findViewById(R.id.clear);
        menu = findViewById(R.id.more);
        tabButton = findViewById(R.id.showTabs);
        forward = findViewById(R.id.forward);
        btnContainer = findViewById(R.id.buttonsContainer);
        searchContainer = findViewById(R.id.searchContainer);
        editTool = findViewById(R.id.editToolContainer);
        progressBar = findViewById(R.id.progressBar);
        copyURL = findViewById(R.id.copyURL);
        shareURL = findViewById(R.id.shareURL);
        editURL = findViewById(R.id.editURL);
        currentURL = findViewById(R.id.currentURL);
        currentIcon = findViewById(R.id.currentIcon);
        inflater = getLayoutInflater();
        pagerAdapter = new WebContainerAdapter();
        pager = findViewById (R.id.webContainer);
        pager.setPagingEnabled(false);
        pager.setAdapter (pagerAdapter);

        //Load Home page from assets
        addNewTab("file:///android_asset/homepage.html");

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (search.hasFocus()){if (!search.getText().toString().equals("")){search.setText("");}else{closeKeyboard();search.clearFocus(); }}
            }
        });

        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    clear.setVisibility(View.VISIBLE);
                    btnContainer.setVisibility(View.GONE);
                    if (!search.getText().toString().equals("") && !search.getText().toString().equals("app://flappyBird")) {
                        editTool.setVisibility(View.VISIBLE);
                    }
                    search.setText("");
                }else {
                    clear.setVisibility(View.GONE);
                    btnContainer.setVisibility(View.VISIBLE);
                    editTool.setVisibility(View.GONE);
                }
            }
        });

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH)
                {
                    searchFor(search.getText().toString());
                    closeKeyboard();
                    search.clearFocus();
                    clear.setVisibility(View.GONE);
                    btnContainer.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateUi();
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(runnable, 100);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Get the web view hit test result
        WebView webView = (WebView) pagerAdapter.getView(pager.getCurrentItem());
        final WebView.HitTestResult result = webView.getHitTestResult();

        // If user long press on url
        if (result.getType() == WebView.HitTestResult.ANCHOR_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {

            final String url = result.getExtra();
            menu.setHeaderTitle(url);

            // Add items to the menu
            menu.add(0, 1, 0, "Open in new tab")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            addNewTab(url);
                            return false;
                        }
                    });
            menu.add(0,2,1,"Copy Url").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    CopyURL(url);
                    return false;
                }
            });
        }
        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            menu.setHeaderTitle(result.getExtra());

            menu.add(0, 1, 0, "Download Image")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            String DownloadImageURL = result.getExtra();

                            if(URLUtil.isValidUrl(DownloadImageURL)){

                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                                request.allowScanningByMediaScanner();


                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);

                                Toast.makeText(MainActivity.this,"Downloading Started.",Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(MainActivity.this,"Sorry.. Something Went Wrong.",Toast.LENGTH_LONG).show();
                            }
                            return false;
                        }
                    });
            menu.add(0,2,1,"Copy Url").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    CopyURL(result.getExtra());
                    return false;
                }
            });
        }
    }

    private void searchFor(String input) {
        WebView webView = (WebView) pagerAdapter.getView(pager.getCurrentItem());
        if (input.contains(".")){
            if (input.contains("http://") || input.contains("https://")){
                webView.loadUrl(input);
            }else{
                webView.loadUrl("https://"+input);
            }
        }else{
            webView.loadUrl(getSearchUrl(input.trim().replace(" ","%20"),"Google"));
        }
    }

    private void updateUi() {
        tabButton.setText(""+pagerAdapter.getCount());
        WebView webView = (WebView) pagerAdapter.getView(pager.getCurrentItem());
        if (webView.getProgress()>0 && webView.getProgress()<100)
        {
            progressBar.setProgress(webView.getProgress());
        }else {
            progressBar.setProgress(0);
        }

        if (!search.hasFocus()){
            //Text for specific url
            switch (webView.getUrl())
            {
                case "file:///android_asset/homepage.html":
                    search.setText("");
                    break;
                default:
                    search.setText(webView.getUrl());
            }
        }
        currentURL.setText(webView.getUrl());
        if (webView.getFavicon()!=null) { currentIcon.setImageBitmap(webView.getFavicon()); }
        tabButton.setText(""+pagerAdapter.getCount());
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.refresh:
                        reloadPage();
                        return true;
                    case R.id.newTab:
                        addNewTab("file:///android_asset/homepage.html");
                        return true;
                    case R.id.openHome:
                        WebView webView = (WebView) pagerAdapter.getView(pager.getCurrentItem());
                        webView.loadUrl("file:///android_asset/homepage.html");
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    //


    public void showTabs(View view)
    {
        tabList = new TabListSheet(pagerAdapter.getTabsList(),MainActivity.this);
        tabList.show(getSupportFragmentManager(),"tabs");
    }

    void OpenTab(int position)
    {
        pager.setCurrentItem(position,false);
    }

    public void addNewTab(String url) {

        WebView tab = (WebView) inflater.inflate (R.layout.web_view, null);
        WebSettings settings = tab.getSettings();
        tab.getSettings().setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        tab.setDownloadListener(new DownloadListener()
        {
            @Override
            public void onDownloadStart(final String url, final String userAgent,
                                        final String contentDisposition, final String mimeType,
                                        long contentLength) {
                Dexter.withContext(MainActivity.this)
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                DownloadManager.Request request = new DownloadManager.Request(
                                        Uri.parse(url));
                                request.setMimeType(mimeType);
                                String cookies = CookieManager.getInstance().getCookie(url);
                                request.addRequestHeader("cookie", cookies);
                                request.addRequestHeader("User-Agent", userAgent);
                                request.setDescription("Downloading File...");
                                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                                url, contentDisposition, mimeType));
                                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                dm.enqueue(request);
                                Toast.makeText(getApplicationContext(), "Downloading Started", Toast.LENGTH_LONG).show();
                            }
                            @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                                Toast.makeText(MainActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                        }).check();
            }});

        tab.setWebViewClient(new WebViewClient());
        tab.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }

            public void onPageFinished(WebView view, String url) {

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        tab.loadUrl(url);
        registerForContextMenu(tab);

        pagerAdapter.addView (tab);pagerAdapter.getWebSettingsAt(0).setJavaScriptEnabled(false);
        pagerAdapter.notifyDataSetChanged();
        pager.setCurrentItem(pagerAdapter.getCount()-1,false);
    }

    public void CloseTab(int position)
    {
        pagerAdapter.removeView(pager,position);
    }

    private void closeKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }

    public void CopyURL(String url)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("url", url);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Url Copied", Toast.LENGTH_SHORT).show();
    }

    public void ShareURL(String url)
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(intent, "Share URL"));
    }

    public void shareCurrent(View view)
    {
        WebView webView = (WebView) pagerAdapter.getView(pager.getCurrentItem());
        ShareURL(webView.getUrl());
    }

    public void copyCurrent(View view)
    {
        WebView webView = (WebView) pagerAdapter.getView(pager.getCurrentItem());
        CopyURL(webView.getUrl());
    }

    public void EditUrl(View view)
    {
        WebView webView = (WebView) pagerAdapter.getView(pager.getCurrentItem());
        search.setText(webView.getUrl());
    }

    public void openDownloads()
    {
        Intent downloads = new Intent(MainActivity.this,DownloadsActivity.class);
        startActivityForResult(downloads,0);
    }

    public void openHistoryBookmarks()
    {
        Intent downloads = new Intent(MainActivity.this,HistoryAndBookmarks.class);
        startActivityForResult(downloads,1);
    }

    public void goForward(View view)
    {
        WebView currentTab = (WebView) pagerAdapter.getView(pager.getCurrentItem());
        if (currentTab.canGoForward())
            currentTab.goForward();
    }

    public void reloadPage()
    {
        WebView currentTab =(WebView) pagerAdapter.getView(pager.getCurrentItem());
        currentTab.reload();
    }

    public String getSearchUrl(String text, String engine)
    {
        switch (engine)
        {
            case "Google":
                return "https://google.com/search?q="+text.trim().replace(" ","+");
            case "Bing":
                return "https://bing.com/search?q="+text.trim().replace(" ","+");
            default:
                return "https://duckduckgo.com/?q="+text.trim().replace(" ","%20");
        }
    }

    @Override
    public void onBackPressed() {
        WebView currentTab = (WebView) pagerAdapter.getView(pager.getCurrentItem());
        if (currentTab.canGoBack()) {
            currentTab.goBack();
        }else {super.onBackPressed();}
    }

    @Override
    public void onAddTabClicked() {
        addNewTab("file:///android_asset/homepage.html");
        tabList.dismiss();
    }

    @Override
    public void onTabClicked(int position) {
        OpenTab(position);
        tabList.dismiss();
    }

    @Override
    public void onCloseTabClicked(int position) {
        CloseTab(position);
    }

    public int getCurrentTab(){
        return pager.getCurrentItem();
    }
}