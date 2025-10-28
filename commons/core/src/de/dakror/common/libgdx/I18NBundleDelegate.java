package de.dakror.common.libgdx;

import java.util.Locale;
import java.util.MissingResourceException;

import com.badlogic.gdx.utils.I18NBundle;

public class I18NBundleDelegate {
    private I18NBundle bundle;

    public I18NBundleDelegate(I18NBundle bundle) {
        I18NBundle.setExceptionOnMissingKey(true);
        this.bundle = bundle;
    }

    public I18NBundle getBundle() {
        return bundle;
    }

    public Locale getLocale() {
        return bundle.getLocale();
    }

    public String get(String key) {
        try {
            return bundle.get(key).trim();
        } catch (MissingResourceException e) {
            System.err.println("[LANG] '" + key + "'");
            return "???" + key + "???";
        }
    }

    public String format(String key, Object... args) {
        try {
            return bundle.format(key, args).trim();
        } catch (MissingResourceException e) {
            System.err.println("Missing Translation \"" + key + "\"");
            return "???" + key + "???";
        }
    }
}
