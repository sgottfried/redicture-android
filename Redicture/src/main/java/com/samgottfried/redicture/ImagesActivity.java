package com.samgottfried.redicture;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.samgottfried.redicture.models.Post;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ImagesActivity extends Activity {

    private Post[] posts = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        new GetPosts(this).execute();
    }

    class PostsArrayAdapter extends ArrayAdapter<Post> {

        private final Context context;
        private final Post[] posts;

        public PostsArrayAdapter(Context context, Post[] posts) {
            super(context, R.layout.post_list_item, posts);
            this.context = context;
            this.posts = posts;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View postView = inflater.inflate(R.layout.post_list_item, parent, false);

            TextView textView = (TextView) postView.findViewById(R.id.title);
            ImageView imageView = (ImageView) postView.findViewById(R.id.image);
            new DownloadImage(imageView).execute(posts[position]);
            textView.setText(posts[position].getTitle());

            return postView;
        }

        class DownloadImage extends AsyncTask<Post,Void,Void> {

            private ImageView imageView;
            private Bitmap image;

            public DownloadImage (ImageView imageView) {
                this.imageView = imageView;
            }

            @Override
            protected Void doInBackground(Post... posts) {
                try
                {
                   image = downloadBitmap(posts[0].getThumbnail());
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if(image!=null)
                {
                    imageView.setImageBitmap(image);
                }

            }
        }

            private Bitmap downloadBitmap(String url) {
                final DefaultHttpClient client = new DefaultHttpClient();
                final HttpGet getRequest = new HttpGet(url);

                Bitmap image = null;

                try {

                    HttpResponse response = client.execute(getRequest);
                    final int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode != HttpStatus.SC_OK) {
                        Log.w("Redicture", "Error " + statusCode +
                                " while retrieving bitmap from " + url);
                        return null;

                    }

                    final HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream inputStream = null;
                        try {
                            inputStream = entity.getContent();

                            image = BitmapFactory.decodeStream(inputStream);

                        } finally {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            entity.consumeContent();
                        }
                    }
                } catch (Exception e) {
                    getRequest.abort();
                    Log.e("Redicture", "Something went wrong while" +
                            " retrieving bitmap from " + url + e.toString());
                }

                return image;
        }
    }


    class GetPosts extends AsyncTask<Void, Void, JSONObject> {

        private Context context;

        public GetPosts(Context context) {
            this.context = context;
        }
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
            try {
                jArray = json.getJSONArray("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            posts = new Post[jArray.length()];

            for(int i=0; i<jArray.length(); i++) {
                try {
                    JSONObject post = jArray.getJSONObject(i);
                    posts[i] = new Post(post.getString("title"), post.getString("hash"), post.getString("ext"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            final ListView listView = (ListView) findViewById(R.id.postsView);

            final ArrayAdapter adapter = new PostsArrayAdapter(context, posts);

            listView.setAdapter(adapter);
        }
    }

}