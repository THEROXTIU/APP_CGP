package com.example.appcampestre;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    static WebView webView;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private WebSettings myWebSettings;
    SwipeRefreshLayout mySwipeRefreshLayout;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //Swipe to refresh functionality
        mySwipeRefreshLayout = (SwipeRefreshLayout)this.findViewById(R.id.swipeContainer);

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        webView.reload();
                    }
                }
        );


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }
    //Método para poder envíar archivos
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isInternetConnected()) {

            webView = findViewById(R.id.webView);

            //configuración de las opciones del WebView
            myWebSettings = webView.getSettings();
            myWebSettings.setJavaScriptEnabled(true);
            myWebSettings.setAllowFileAccess(true);
            myWebSettings.setAllowContentAccess(true);
            webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.setWebViewClient(new WebViewClient());

            mySwipeRefreshLayout = findViewById(R.id.swipeContainer);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    // La página está comenzando a cargarse, así que indicamos que se está actualizando.
                    mySwipeRefreshLayout.setRefreshing(true);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    // La página se ha cargado completamente, así que detenemos la animación de actualización.
                    mySwipeRefreshLayout.setRefreshing(false);
                }
            });

            mySwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Cargar la página web cuando se inicie la actualización.
                    webView.loadUrl("http://192.168.10.96:8000/");
                    mySwipeRefreshLayout.setRefreshing(true);
                }
            });
            /* OCULTAR PROGRESSBAR CUANDO YA LA PÁGINA SE HA CARGADO POR COMPLETO
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    progressBar.setVisibility(View.GONE);
                    super.onPageFinished(view, url);
                }


            });
            */
            webView.setWebChromeClient(new WebChromeClient() {
                // For 3.0+ Devices (Start)
                // onActivityResult attached before constructor
                protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                    mUploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                    startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
                }


                // For Lollipop 5.0+ Devices
                public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                    if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }

                    uploadMessage = filePathCallback;

                    Intent intent = fileChooserParams.createIntent();
                    try {
                        startActivityForResult(intent, REQUEST_SELECT_FILE);
                    } catch (ActivityNotFoundException e) {
                        uploadMessage = null;
                        Toast.makeText(getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return true;
                }

                //For Android 4.1 only
                protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                    mUploadMessage = uploadMsg;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
                }

                protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
                    mUploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                    startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
                }
            });
            //FIN DEL MÉTODO PARA ENVIAR ARCHIVOS

            webView.getSettings().setDomStorageEnabled(true);

            myWebSettings.setAllowFileAccess(true);
            myWebSettings.setAllowContentAccess(true);

            // Habilitar la carga de archivos desde el WebView
            myWebSettings.setAllowFileAccess(true);
            myWebSettings.setAllowFileAccessFromFileURLs(true);
            myWebSettings.setAllowUniversalAccessFromFileURLs(true);


            //cargamos la url que deseamos acceder
            webView.loadUrl("http://192.168.10.96:8000/");

        } else {
            finishAffinity();
            // El dispositivo no tiene conexión a Internet, muestra un mensaje de error.
            Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_SHORT).show();

        }
    }



    //INICIO DEL MÉTODO PARA PODER UTILIZAR LA FUNCIÓN DE REGRESAR EN LA APP
    public void onBackPressed() { //if user presses the back button do this
        if (webView.isFocused() && webView.canGoBack()) { //check if in webview and the user can go back
            webView.goBack(); //go back in webview
        } else { //do this if the webview cannot go back any further

            new AlertDialog.Builder(this) //alert the person knowing they are about to close
                    .setTitle("SALIENDO DE LA APP CAMPESTRE")
                    .setMessage("¿Estás seguro que quieres cerrar la aplicación?")
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }//FIN DEL MÉTODO FUNCIÓN PARA REGRESAR EN LA APP
    //MÉTODO PARA VERIFICAR LA CONEXIÓN A INTERNET
    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        return false;
    }
}