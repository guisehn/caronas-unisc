package br.unisc.caronasuniscegm.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import br.unisc.caronasuniscegm.R;

/**
 * Created by MateusFelipe on 17/10/2015.
 */
public class TokenUtils {

    public static String getToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        return sharedPref.getString(context.getString(R.string.preference_session_token), "");
    }
}
