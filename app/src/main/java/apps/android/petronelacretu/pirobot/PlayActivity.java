package apps.android.petronelacretu.irobot;

        import android.content.ClipData;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.drawable.Drawable;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.AsyncTask;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.view.DragEvent;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;

        import com.koushikdutta.async.ByteBufferList;
        import com.koushikdutta.async.callback.DataCallback;
        import com.koushikdutta.async.http.AsyncHttpClient;
        import com.koushikdutta.async.http.WebSocket;

        import java.io.IOException;
        import java.io.InputStream;
        import java.net.HttpURLConnection;
        import java.net.URL;


public class PlayActivity extends ActionBarActivity {

    private WebSocket mWebSocket;
    private ImageView mMainImage;
    private TextView mGreet;

    private ImageView moveWheels;
    private ImageView moveCam;
    private LinearLayout areaCam;
    private LinearLayout areaWheels;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);


        mMainImage = (ImageView) findViewById(R.id.mainImageView);
        mGreet = (TextView) findViewById(R.id.greet);

        moveWheels = (ImageView) findViewById(R.id.car);
        moveCam = (ImageView) findViewById(R.id.cam);

        areaCam = (LinearLayout) findViewById(R.id.areaCam);
        areaWheels = (LinearLayout) findViewById(R.id.areaWheels);

        // set default image in the ImageView when no connection established
        mMainImage.setImageResource(R.mipmap.ic_launcher);



        if (mWebSocket == null)
            SetWebSocket();


        moveWheels.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDrag(data, shadowBuilder, v, 0);
                    v.setVisibility(View.INVISIBLE);
                    return true;
                } else {
                    return false;
                }
            }
        });

        areaWheels.setOnDragListener(new View.OnDragListener() {
        
            Drawable enterShape = getResources().getDrawable(R.drawable.shape_drop);
            Drawable normalShape = getResources().getDrawable(R.drawable.shape);

            @Override
            public boolean onDrag(View v, DragEvent event) {
                int action = event.getAction();
                View dragView = (View) event.getLocalState();

                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // do nothing
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setBackgroundDrawable(enterShape);
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        String command = SetDirection(event.getX(), event.getY());
                        if (mWebSocket != null)
                            mWebSocket.send(command);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setBackgroundDrawable(normalShape);
                        break;
                    case DragEvent.ACTION_DROP:
                        // Dropped, reassign View to ViewGroup
                        View view = (View) event.getLocalState();
                        ViewGroup owner = (ViewGroup) view.getParent();
                        owner.removeView(view);
                        LinearLayout container = (LinearLayout) v;
                        container.addView(view);
                        view.setVisibility(View.VISIBLE);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        view = (View) event.getLocalState();
                        view.setVisibility(View.VISIBLE);
                        v.setBackgroundDrawable(normalShape);
                    default:
                        break;
                }
                return true;
            }
        });



        moveCam.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDrag(data, shadowBuilder, v, 0);
                    v.setVisibility(View.INVISIBLE);
                    return true;
                } else {
                    return false;
                }
            }
        });

        areaCam.setOnDragListener(new View.OnDragListener() {
            Drawable enterShape = getResources().getDrawable(R.drawable.shape_drop);
            Drawable normalShape = getResources().getDrawable(R.drawable.shape);

            @Override
            public boolean onDrag(View v, DragEvent event) {
                int action = event.getAction();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // do nothing
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setBackgroundDrawable(enterShape);
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        String command = SetCam(event.getX(), event.getY());
                        if (mWebSocket != null)
                            mWebSocket.send(command);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setBackgroundDrawable(normalShape);
                        break;
                    case DragEvent.ACTION_DROP:
                        // Dropped, reassign View to ViewGroup
                        View view = (View) event.getLocalState();
                        ViewGroup owner = (ViewGroup) view.getParent();
                        owner.removeView(view);
                        LinearLayout container = (LinearLayout) v;
                        container.addView(view);
                        view.setVisibility(View.VISIBLE);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        view = (View) event.getLocalState();
                        view.setVisibility(View.VISIBLE);
                        v.setBackgroundDrawable(normalShape);
                    default:
                        break;
                }
                return true;
            }
        });



        mMainImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mWebSocket != null)
                    mWebSocket.send("StartStreaming");
                try {

                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SetConnection();
                return true;
            }
        });

    }

    public String SetDirection(float x, float y){
        x = x - areaWheels.getWidth()/2;
        y = y - areaWheels.getHeight()/2;

        float sine = (float) (y / Math.sqrt(Math.pow(x,2) + Math.pow(y,2)));

        // mid way - 45 degrees angle - between 2 of the 4 directions taken as border for direction change,
        // i.e. sine is sqrt(2)/2

        if ( ((sine <  Math.sqrt(2)/2) && (sine > - Math.sqrt(2)/2 )) && (x > 0) )
        {
            return "SetMotorSpeeds 40 0";// turn right
        }

        if ( ((sine <  Math.sqrt(2)/2) && (sine > - Math.sqrt(2)/2 )) && (x < 0) )
        {
            return "SetMotorSpeeds 0 40";// turn left
        }

        if ( (sine >  Math.sqrt(2)/2) && (y > 0) )
        {
            return "SetMotorSpeeds -40 -40";// go backwards
        }

        if ( (sine < - Math.sqrt(2)/2) && (y < 0) )
        {
            return "SetMotorSpeeds 40 40";// go ahead
        }

        return "SetMotorSpeeds 0 0";
    }


    public String SetCam(float x, float y){
        x = x - areaCam.getWidth()/2;
        y = y - areaCam.getHeight()/2;

        float sine = (float) (y / Math.sqrt(Math.pow(x,2) + Math.pow(y,2)));

        // mid way - 45 degrees angle - between 2 of the 4 directions taken as border for direction change,
        // i.e. sine is sqrt(2)/2

        if ( ((sine <  Math.sqrt(2)/2) && (sine > - Math.sqrt(2)/2 )) && (x > 0) )
        {
            return "PanTilt 0.4 0.0";// turn right
        }

        if ( ((sine <  Math.sqrt(2)/2) && (sine > - Math.sqrt(2)/2 )) && (x < 0) )
        {
            return "PanTilt 0.0 0.4";// turn left
        }

        if ( (sine >  Math.sqrt(2)/2) && (y > 0) )
        {
            return "PanTilt -0.4 -0.4";// go downwards
        }

        if ( (sine < - Math.sqrt(2)/2) && (y < 0) )
        {
            return "PanTilt 0.4 0.4";// go upwards
        }

        return "PanTilt 0 0";
    }


    public double SetSpeed(float sine){
        //sine has values from -1 to 1
        // normalise it for values from 0 to 1
        double normalised = (double) (sine + 1)/2;

        return normalised;
    }

    private void SetWebSocket() {
        AsyncHttpClient.getDefaultInstance().websocket("ws://your_ip/robot_control/websocket",
                "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override
                    public void onCompleted(Exception ex, WebSocket webSocket) {
                        if (ex != null) {
                            ex.printStackTrace();
                            return;
                        }
                        mWebSocket = webSocket;
                    }
                });
    }


    private void SetConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute("http://your_ip:8080?action=snapshot");
        } else {
            mGreet.setText("No network connection available.");
        }

    }



    private class DownloadWebpageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Bitmap result) {
            mMainImage.setImageBitmap(result);
        }
    }

    private Bitmap downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            //Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(is);


            return bitmap;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
