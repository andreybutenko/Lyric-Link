package ninja.andrey.lyriclink;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ninja.andrey.lyriclink.model.Result;

public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Toolbar webToolbar = (Toolbar) findViewById(R.id.webToolbar);
        setSupportActionBar(webToolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.item_tab);

            RelativeLayout tabLayout = (RelativeLayout) findViewById(R.id.tabLayout);
            tabLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTabSelectDialog();
                }
            });
        }
    }

    private void showTabSelectDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_tab_select, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        List<Map<String, String>> results = new ArrayList<>();

        Map<String, String> resultA = new HashMap<>();
        resultA.put("title", "Wow");
        resultA.put("provider", "Sub");
        resultA.put("prefix", "Selected:");
        results.add(resultA);

        Map<String, String> resultB = new HashMap<>();
        resultB.put("title", "Dub");
        resultB.put("provider", "Mub");
        resultB.put("prefix", "Default:");
        results.add(resultB);

        SimpleAdapter tabAdapter = new SimpleAdapter(this, results,
                R.layout.list_tab_select,
                new String[] { "title", "provider", "prefix" },
                new int[] { R.id.tabTrack, R.id.tabProvider, R.id.tabPrefix });

        ListView tabSelectListView = (ListView) view.findViewById(R.id.tabSelectListView);
        tabSelectListView.setAdapter(tabAdapter);

        builder.setTitle("Select a provider")
                .setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        tabSelectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
            }
        });
    }
}