package pl.tmsj.tetrismove;

import android.view.Menu;

public class GenericMenu {

	Menu mMenu;
	
	public GenericMenu(Menu menu) {
		mMenu = menu;
	}

	public void populate() {

		mMenu.add("Resume");
		mMenu.add("Restart");
		mMenu.add("Music");
		mMenu.add("Quit");
	}
}
