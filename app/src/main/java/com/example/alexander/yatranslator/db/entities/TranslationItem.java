package com.example.alexander.yatranslator.db.entities;

import com.example.alexander.yatranslator.db.TranslationParameters;

import java.util.List;

/**
 * Created by Alexander on 09.04.2017.
 */
public class TranslationItem {
    private final List<String> values;
    private final TranslationParameters parameters;
    private Boolean isFavorite;

    public TranslationItem(TranslationParameters parameters, List<String> translations) {
        this.values = translations;
        this.parameters = parameters;
        this.isFavorite = false;
    }



    public List<String> getValues() {
        return values;
    }

    public TranslationParameters getParameters() {
        return parameters;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean favorite) {
        isFavorite = favorite;
    }
}