/*
 * Copyright 2020 Johan Walles <johan.walles@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.walles.johan.exactype.activities.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.gmail.walles.johan.exactype.R;

import java.text.NumberFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import timber.log.Timber;

public class StatsFragment extends Fragment {

    private StatsViewModel viewModel;

    public static StatsFragment newInstance() {
        return new StatsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.stats_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            viewModel.refresh(getContext());
            repopulateTable();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.stats, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);
        viewModel.populate(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        repopulateTable();
    }

    private void repopulateTable() {
        Context context = getContext();
        if (context == null) {
            Timber.w("Context was null when trying to populate the stats table");
            return;
        }

        View view = getView();
        if (view == null) {
            Timber.w("View was null when trying to populate stats table");
            return;
        }

        TableLayout table = view.findViewById(R.id.statsTable);

        // Remove everything except for the table heading
        table.removeViewsInLayout(1, table.getChildCount() - 1);

        Resources r = context.getResources();
        int fiveDpInPixels = (int)TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5,
            r.getDisplayMetrics()
        );
        TableRow.LayoutParams fiveDpOnEachSide = new TableRow.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        fiveDpOnEachSide.setMargins(fiveDpInPixels, 0, fiveDpInPixels, 0);

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        for (StatsViewModel.Entry entry: viewModel.entries) {
            TextView charColumn = new TextView(context);
            charColumn.setText(entry.name);
            charColumn.setGravity(Gravity.END);
            charColumn.setLayoutParams(fiveDpOnEachSide);

            TextView countColumn = new TextView(context);
            countColumn.setText(numberFormat.format(entry.count));
            countColumn.setGravity(Gravity.END);
            countColumn.setLayoutParams(fiveDpOnEachSide);

            TextView rankColumn = new TextView(context);
            rankColumn.setText(numberFormat.format(entry.rank));
            rankColumn.setGravity(Gravity.END);
            rankColumn.setLayoutParams(fiveDpOnEachSide);

            TextView percentileColumn = new TextView(context);
            String percentileText =
                context.getResources().getString(R.string.percentile_fmt, entry.percentile);
            percentileColumn.setText(percentileText);
            percentileColumn.setGravity(Gravity.END);
            percentileColumn.setLayoutParams(fiveDpOnEachSide);

            TableRow row = new TableRow(context);
            row.addView(charColumn);
            row.addView(countColumn);
            row.addView(rankColumn);
            row.addView(percentileColumn);

            table.addView(row);
        }
    }
}
