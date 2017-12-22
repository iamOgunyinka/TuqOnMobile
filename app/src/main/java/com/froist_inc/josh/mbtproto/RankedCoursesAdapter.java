package com.froist_inc.josh.mbtproto;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class RankedCoursesAdapter extends ArrayAdapter<Utilities.RankedCoursesData>
{
    private Context context;
    private int layout_resource_id;
    private float image_width;

    public RankedCoursesAdapter( Context context, int layout_resource_id, ArrayList<Utilities.RankedCoursesData> items )
    {
        super( context, layout_resource_id, items );
        this.context = context;
        this.layout_resource_id = layout_resource_id;

        @SuppressWarnings("deprecation")
        float width = ((Activity)context).getWindowManager().getDefaultDisplay().getWidth();

        float margin = (int) ConvertDpToPixel( 10f, context );
        // two images, three margins of 10dips
        image_width = ((width - (3 * margin)) / 2);
    }

    @Override
    public View getView( final int position, View convert_view, ViewGroup parent ) {
        LinearLayout row = ( LinearLayout ) convert_view;
        ItemHolder holder;
        Utilities.RankedCoursesData item = getItem( position );

        if ( row == null ) {
            holder = new ItemHolder();
            LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            row = ( LinearLayout ) inflater.inflate(layout_resource_id, parent, false );
            holder.item_image = ( ImageView )row.findViewById( R.id.ranking_item_image );
            holder.item_text = ( TextView ) row.findViewById( R.id.ranking_item_text );
            holder.item_text.setText( item.GetSubjectName() );
        } else {
            holder = ( ItemHolder ) row.getTag();
        }

        row.setTag( holder );
        Bitmap bitmap;
        if( item.icon_data == null ) {
            bitmap = BitmapFactory.decodeResource( getContext().getResources(), R.drawable.course );
        } else {
            bitmap = BitmapFactory.decodeByteArray( item.icon_data, 0, item.icon_data.length );
        }
        SetImageBitmap( bitmap, holder.item_image);
        return row;
    }

    public static class ItemHolder
    {
        ImageView item_image;
        TextView  item_text;
    }

    // resize the image proportionately so it fits the entire space
    private void SetImageBitmap( Bitmap bitmap, ImageView image_view )
    {
        float i = image_width / ((float) bitmap.getWidth());
        float imageHeight = i * (bitmap.getHeight());
        LinearLayout.LayoutParams params = ( LinearLayout.LayoutParams ) image_view.getLayoutParams();
        params.height = (int) imageHeight;
        params.width = (int) image_width;
        image_view.setLayoutParams( params );
        image_view.setImageBitmap( bitmap );
    }

    public static float ConvertDpToPixel( final float dp, final Context context )
    {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi/160f );
    }
}
