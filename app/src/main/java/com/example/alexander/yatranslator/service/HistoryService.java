package com.example.alexander.yatranslator.service;

import android.util.Log;

import com.example.alexander.yatranslator.db.TranslationParameters;
import com.example.alexander.yatranslator.db.entities.TranslationItem;
import com.example.alexander.yatranslator.db.tables.ParametersTable;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.Query;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Alexander on 09.04.2017.
 */

public class HistoryService {
    StorIOSQLite storIOSQLite;

    public HistoryService(StorIOSQLite storIOSQLite) {
        this.storIOSQLite = storIOSQLite;
    }

    public Observable<Long> ExistItem(String text) {

        return Observable.defer(() -> Observable.just(
                storIOSQLite.get()
                        .object(TranslationParameters.class)
                        .withQuery(
                                Query.builder()
                                        .table(ParametersTable.TABLE)
                                        .where(ParametersTable.COLUMN_TEXT + "='" + text + "'")
                                        //todo Добавить тип
                                        .build())
                        .prepare()
                        .executeAsBlocking().getId()));
    }

    public Observable UpdateOrder(Integer id) {
        //todo узнать как делать update storio

        return Observable.empty();
    }


    public Observable<List<TranslationItem>> getHistory(){
        Log.d("[Debug]", "getHistory");

        return Observable.defer(() -> Observable.just(
                    storIOSQLite.get()
                            .listOfObjects(TranslationItem.class)
                            .withQuery(ParametersTable.QUERY_ALL_HISTORY)
                            .prepare()
                            .executeAsBlocking()));

    }

    public Observable putTranslateItem(Integer type, String direction, String text, List<String> translations){

        TranslationParameters parameters = new TranslationParameters(null, type, direction, text);
        TranslationItem withParameters = new TranslationItem(parameters, translations);

        Log.d("inserted history", text);

        return Observable.defer(() -> Observable.just(
                storIOSQLite.put()
                        .object(withParameters)
                        .prepare()
                        .executeAsBlocking()));
    }

    private boolean canInsert(String text, List<String> translations) {
        return text == null || text.isEmpty()
                || translations == null
                || translations.isEmpty()
                || translations.get(0) == text;
    }
}