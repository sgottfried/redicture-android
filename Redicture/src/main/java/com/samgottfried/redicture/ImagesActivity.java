package com.samgottfried.redicture;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.samgottfried.redicture.models.Post;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class ImagesActivity extends Activity {

    private Post[] posts = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

       /* if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/

        new GetPosts().execute();
    }

    class GetPosts extends AsyncTask<Void, Void, JSONObject> {


        protected JSONObject doInBackground(Void... voids) {
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet httpGet = new HttpGet("http://imgur.com/r/all.json?perPage=10&page=1");
            InputStream inputStream = null;
            String result = null;
            try {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if(inputStream != null) {
                        inputStream.close();
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            JSONObject json = null;

            try {
                json = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json;
        }


        protected void onPostExecute(JSONObject json) {

            JSONArray jArray = null;
            TextView jsonView = (TextView) findViewById(R.id.json);
            jsonView.setText(json.toString());
            try {
                jArray = json.getJSONArray("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            posts = new Post[jArray.length()];

            for(int i=0; i<jArray.length(); i++) {
                try {
                    JSONObject post = jArray.getJSONObject(i);
                    posts[i] = new Post(post.getString("title"), post.getString("hash"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
