package com.czzz.base;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.czzz.demo.R;

public abstract class DrawerBaseActivity extends SherlockFragmentActivity{

    protected MenuDrawer mMenuDrawer;
    private View drawerView;

    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);

        mMenuDrawer = MenuDrawer.attach(this, 
        		MenuDrawer.Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
        
        drawerView = initDrawerView();
        
        mMenuDrawer.setMenuView(drawerView);
        mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
        mMenuDrawer.setSlideDrawable(R.drawable.ic_drawer);
        mMenuDrawer.setDrawerIndicatorEnabled(true);
    }
    
    protected abstract void onMenuItemClicked(int resId);
    
    protected abstract View initDrawerView();

    protected View.OnClickListener menuClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
            onMenuItemClicked(v.getId());
		}
	};
    
    @Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub
    	switch (item.getItemId()) {
    	case android.R.id.home:
    		mMenuDrawer.toggleMenu();
    		return true;
    	}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void startActivity(Intent intent) {
		// TODO Auto-generated method stub
		super.startActivity(intent);
		this.overridePendingTransition(R.anim.activity_scroll_from_right, R.anim.fade_out_exit);
	}
	
	@Override
    public void onBackPressed() {
        final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }
        super.onBackPressed();
    }

}
