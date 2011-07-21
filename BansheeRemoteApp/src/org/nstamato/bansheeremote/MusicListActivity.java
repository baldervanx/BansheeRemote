package org.nstamato.bansheeremote;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public abstract class MusicListActivity<T extends MusicItem> extends ListActivity {

    private MusicListAdapter<T> musicListAdapter; 
    protected BansheeDB bansheeDB = new BansheeDB();
    private ImageView icon;
    private TextView title;
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		setResult(resultCode,data);
		if(resultCode==RESULT_OK)
			finish();
	}
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse);
        Bundle extras = getIntent().getExtras();
        this.icon = (ImageView)this.findViewById(R.id.icon);
        this.title = (TextView)this.findViewById(R.id.title);
        this.title.setText(getTitleText(extras));
        this.icon.setImageResource(getIconResource());
		try{
			List<T> items = getMusicItems(extras);
			this.musicListAdapter = new MusicListAdapter<T>(this, R.layout.list_item, items);
			setListAdapter(musicListAdapter);
		} catch(Exception e){
			ErrorHandler.handleError(this, e, R.string.error_db);
		}
		ListView l = getListView();
		l.setTextFilterEnabled(true);
		l.setFastScrollEnabled(true);
    }
	
	public T getItem(int position) {
		return this.musicListAdapter.getItem(position);
	}
	
	protected abstract int getIconResource();
	
	protected abstract String getTitleText(Bundle extras);
	
	protected abstract List<T> getMusicItems(Bundle extras);
	
}
