package com.example.alexander.yatranslater.gateway.dto;

import java.util.List;
import java.util.Map;

/**
 * Created by Alexander on 01.04.2017.
 */
public class LanguagesResponse {
    private List<String> dirs;
    private Map<String, String> langs;

    public List<String> getDirs() {
        return dirs;
    }

    public void setDirs(List<String> dirs) {
        this.dirs = dirs;
    }

    public Map<String, String> getLangs() {
        return langs;
    }

    public void setLangs(Map<String, String> langs) {
        this.langs = langs;
    }
}
