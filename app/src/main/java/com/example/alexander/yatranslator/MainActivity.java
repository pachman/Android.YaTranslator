package com.example.alexander.yatranslator;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.example.alexander.yatranslator.db.TranslationParameters;
import com.example.alexander.yatranslator.db.tables.TranslationType;
import com.example.alexander.yatranslator.dependency.*;
import com.example.alexander.yatranslator.fragment.FavoriteFragment;
import com.example.alexander.yatranslator.fragment.HistoryFragment;
import com.example.alexander.yatranslator.fragment.TranslationFragment;
import com.example.alexander.yatranslator.fragment.TranslationListFragment;
import com.example.alexander.yatranslator.service.TranslationStorage;
import com.example.alexander.yatranslator.ui.SectionsPagerAdapter;
import com.example.alexander.yatranslator.ui.adapter.TranslationAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    String uiLang = Locale.getDefault().getLanguage();
    private static TranslateComponent component;
    private static TranslationStorage translationService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        component = DaggerTranslateComponent.builder()
                .appModule(new AppModule(this))
                .translateModule(new TranslateModule())
                .utilsModule(new UtilsModule())
                .storIOModule(new StorIOModule())
                .build();

        //todo перенести в DI
        translationService = new TranslationStorage(component.provideStorIOSQLite());

        //ButterKnife.bind(this);

        Log.d("[Debug]", "Locale " + uiLang);

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.bottom_tabs);
        createViewPager(viewPager, tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        createTabIcons(tabLayout);
    }

    private void createTabIcons(TabLayout tabLayout) {
        int[] names = new int[]{
                R.string.translation,
                R.string.history,
                R.string.favorite};
        int[] icons = new int[]{
                R.drawable.ic_translate_black_24dp,
                R.drawable.ic_schedule_black_24dp,
                R.drawable.ic_book_black_24dp};

        for (int i = 0; i < names.length; i++) {
            TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            tabOne.setText(getString(names[i]));
            tabOne.setCompoundDrawablesWithIntrinsicBounds(0, icons[i], 0, 0);
            tabLayout.getTabAt(i).setCustomView(tabOne);
        }
    }

    private void createViewPager(ViewPager viewPager, TabLayout tabLayout) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        appendTranslationFragment(adapter);

        appendHistoryFragment(adapter);

        appendFavoriteFragment(adapter);

        TabLayout.TabLayoutOnPageChangeListener listener = new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageSelected(int position) {
                Log.d("[Debug]", "Selected " + position);
                Fragment item = adapter.getItem(position);
                if (item instanceof TranslationListFragment) {
                    ((TranslationListFragment) item).refresh();
                }
            }
        };
        viewPager.addOnPageChangeListener(listener);
        viewPager.setAdapter(adapter);
    }

    private void appendFavoriteFragment(SectionsPagerAdapter adapter) {
        FavoriteFragment favoriteFragment = new FavoriteFragment();
        adapter.addFrag(favoriteFragment, getString(R.string.favorite));

        initListFragment(favoriteFragment);
    }

    private void appendTranslationFragment(SectionsPagerAdapter adapter) {
        TranslationFragment translateFragment = new TranslationFragment();
        component.inject(translateFragment);
        adapter.addFrag(translateFragment, getString(R.string.translation));
    }

    private void appendHistoryFragment(SectionsPagerAdapter adapter) {
        HistoryFragment historyFragment = new HistoryFragment();
        adapter.addFrag(historyFragment, getString(R.string.history));

        initListFragment(historyFragment);
    }

    private void initListFragment(TranslationListFragment fragment) {
        fragment.setSelectedFragmentListener((f, type) -> {
            Log.d("[Debug]", "Fragment selected -> " + type);

            translationService.getTranslationItems(type)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(translations -> {
                        TranslationAdapter translationAdapter = new TranslationAdapter(f.getContext(), new ArrayList<>(translations));
                        translationAdapter.setOnChangeFavoriteListener((v, translationItem) -> {
                            if (translationItem.getIsFavorite()) {
                                TranslationParameters parameters = translationItem.getParameters();
                                translationService.insertOrUpdate(TranslationType.Favorite, parameters.getDirection(), parameters.getText(), translationItem.getValues())
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(o -> {
                                            Log.d("[Debug]", "insert favorite");
                                        });
                            } else {
                                if (type == TranslationType.Favorite) {
                                    translationService.delete(translationItem)
                                            .subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(isDeleted -> {
                                                if (isDeleted) {
                                                    Log.d("[Debug]", "refresh favorite");
                                                    fragment.refresh();
                                                }
                                            });
                                } else {
                                    translationService.deleteFavorite(translationItem)
                                            .subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe();
                                }
                            }
                        });

                        translationAdapter.setOnDeleteItemListener((v, translationItem) -> {
                            translationService.delete(translationItem)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(isDeleted -> {
                                        if (isDeleted) {
                                            Log.d("[Debug]", "refresh fragment");
                                            fragment.refresh();
                                        }
                                    });
                        });

                        fragment.updateList(translationAdapter);
                    });
        });
    }
}



