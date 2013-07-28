package com.czzz.demo;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.czzz.base.BaseActivity;
import com.czzz.bookcircle.BookCollection;
import com.czzz.demo.listadapter.OwnerAdapter;
import com.czzz.view.CollectionDropDialog;

public class BookOwnersActivity extends BaseActivity{

	private GridView ownerGrid;
	
	private ArrayList<BookCollection> ownerInfos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.book_owners_activity);
		
		Intent ii = getIntent();
		ownerInfos = ii.getParcelableArrayListExtra("owners");
		
		Log.d("DEBUG", "info: " + ownerInfos);
		
		ownerGrid = (GridView) findViewById(R.id.owners_grid_);
		
		fillDataGrid();
		
	}

	private void fillDataGrid(){
		OwnerAdapter ownerAdapter = new OwnerAdapter(this, ownerInfos, false);
		ownerGrid.setAdapter(ownerAdapter);
		ownerGrid.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				// TODO Auto-generated method stub
				CollectionDropDialog detailDialog = new CollectionDropDialog(
						BookOwnersActivity.this, ownerInfos.get(position));
				detailDialog.show();
			}
			
		});
	}
	
}
