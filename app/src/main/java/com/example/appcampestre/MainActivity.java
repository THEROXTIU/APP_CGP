package com.example.appcampestre;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.app.DownloadManager;
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
import android.os.Environment;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    static WebView webView;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private WebSettings myWebSettings;
    SwipeRefreshLayout mySwipeRefreshLayout;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Executor executor;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //Executor executor = ContextCompat.getMainExecutor(this);
        /*
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Inicio de Sesión Biométrico App Campestre")
                .setSubtitle("Desbloquea la aplicación con tu huella dactilar")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();

         */
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
            Toast.makeText(getApplicationContext(), "Error al subir la imagen, intentalo de nuevo!", Toast.LENGTH_LONG).show();
    }

    // Método onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        executor = ContextCompat.getMainExecutor(this);


        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Error de Autenticación")
                        .setMessage("Se ha producido un error en la autenticación: " + errString)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Cierra la aplicación
                                finish();
                            }
                        })
                        .setCancelable(false) // Evita que el usuario cierre el diálogo sin aceptar
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        "Autentificación correcta!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "La autentificación falló",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Inicio de Sesión Biométrico App Campestre")
                .setSubtitle("Desbloquea la aplicación con tu huella o rostro")
                .setDeviceCredentialAllowed(true)


                .build();

        if (isInternetConnected()) {


            // Prompt appears when user clicks "Log in".
            // Consider integrating with the keystore to unlock cryptographic operations,
            // if needed by your app.

            webView = findViewById(R.id.webView);
            biometricPrompt.authenticate(promptInfo);
            //configuración de las opciones del WebView
            myWebSettings = webView.getSettings();
            myWebSettings.setJavaScriptEnabled(true);
            myWebSettings.setAllowFileAccess(true);
            myWebSettings.setAllowContentAccess(true);
            webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.setWebViewClient(new WebViewClient());
            myWebSettings.setAllowFileAccess(true);
            myWebSettings.setAllowContentAccess(true);
            mySwipeRefreshLayout = findViewById(R.id.swipeContainer);


            webView.setDownloadListener((new DownloadListener() {
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimeType = "image/png");
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading file....");
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_SHORT).show();


                }
            }));

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

        }else{
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Error de Conexión")
                    .setMessage("Asegúrate de tener conexión a Internet")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Cierra la aplicación
                            finish();
                        }
                    })
                    .setCancelable(false) // Evita que el usuario cierre el diálogo sin aceptar
                    .show();
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
