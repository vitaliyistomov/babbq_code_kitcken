package babbq.com.searchplace;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import babbq.com.searchplace.adapter.TestAdapter;

/**
 * Created by alex on 11/14/15.
 */
public class TestActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new TestFragment())
                .commit();
    }
}
