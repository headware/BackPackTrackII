package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

public class LocationAdapter extends CursorAdapter {
    private static final String TAG = "BPT2.LocationAdapter";

    private Context mContext;
    private Location lastLocation;

    public LocationAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
        init();
    }

    public void init() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        lastLocation = LocationService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.location, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        final long time = cursor.getLong(cursor.getColumnIndex("time"));
        final String provider = cursor.getString(cursor.getColumnIndex("provider"));
        final double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
        final double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
        final boolean hasAltitude = !cursor.isNull(cursor.getColumnIndex("altitude"));
        boolean hasBearing = !cursor.isNull(cursor.getColumnIndex("bearing"));
        boolean hasAccuracy = !cursor.isNull(cursor.getColumnIndex("accuracy"));
        double altitude = cursor.getDouble(cursor.getColumnIndex("altitude"));
        double bearing = cursor.getDouble(cursor.getColumnIndex("bearing"));
        double accuracy = cursor.getDouble(cursor.getColumnIndex("accuracy"));
        final String name = cursor.getString(cursor.getColumnIndex("name"));

        // Calculate distance to last location
        Location dest = new Location("");
        dest.setLatitude(latitude);
        dest.setLongitude(longitude);
        double distance = (lastLocation == null ? 0 : lastLocation.distanceTo(dest));

        // Get views
        TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
        TextView tvProvider = (TextView) view.findViewById(R.id.tvProvider);
        TextView tvAltitude = (TextView) view.findViewById(R.id.tvAltitude);
        TextView tvBearing = (TextView) view.findViewById(R.id.tvBearing);
        TextView tvAccuracy = (TextView) view.findViewById(R.id.tvAccuracy);
        TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);

        // Set values
        view.setBackgroundColor(context.getResources().getColor(name == null ? android.R.color.transparent : android.R.color.darker_gray));
        tvTime.setText(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.MEDIUM).format(time));
        int resId = context.getResources().getIdentifier("provider_" + provider, "string", context.getPackageName());
        tvProvider.setText(resId == 0 ? "-" : context.getString(resId).substring(0, 1));
        tvAltitude.setText(hasAltitude ? Long.toString(Math.round(altitude)) : "?");
        tvBearing.setText(hasBearing ? Long.toString(Math.round(bearing)) : "?");
        tvAccuracy.setText(hasAccuracy ? Long.toString(Math.round(accuracy)) : "?");
        if (lastLocation != null && distance >= 1e7)
            tvDistance.setText(Long.toString(Math.round(distance / 1e6)) + "M");
        else if (lastLocation != null && distance >= 1e4)
            tvDistance.setText(Long.toString(Math.round(distance / 1e3)) + "k");
        else
            tvDistance.setText(lastLocation == null ? "?" : Long.toString(Math.round(distance)));
    }
}
