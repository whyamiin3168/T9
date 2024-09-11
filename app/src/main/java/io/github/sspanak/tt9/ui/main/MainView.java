package io.github.sspanak.tt9.ui.main;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.util.Logger;

public class MainView {
	protected final TraditionalT9 tt9;
	protected BaseMainLayout main;


	protected MainView(TraditionalT9 tt9) {
		this.tt9 = tt9;

		forceCreateInputView();
	}
	// here is default
	int default_softkey = R.id.soft_key_5;

	public boolean createInputView() {
		SettingsStore settings = tt9.getSettings();

		if (settings.isMainLayoutNumpad() && !(main instanceof MainLayoutNumpad)) {
			main = new MainLayoutNumpad(tt9);
		} else if (settings.isMainLayoutSmall() && (main == null || !main.getClass().equals(MainLayoutSmall.class))) {
			main = new MainLayoutSmall(tt9);
		} else if (settings.isMainLayoutTray() && (main == null || !main.getClass().equals(MainLayoutTray.class))) {
			main = new MainLayoutTray(tt9);
		} else if (settings.isMainLayoutStealth() && !(main instanceof MainLayoutStealth)) {
			main = new MainLayoutStealth(tt9);
		} else {
			return false;
		}

		main.render();

		// main chunk
//		ArrayList<Integer> numpadButtons = new ArrayList<>();
//		numpadButtons.add(R.id.soft_key_1);  // Button ID for "1"
//		numpadButtons.add(R.id.soft_key_2);  // Button ID for "2"
//		numpadButtons.add(R.id.soft_key_3);  // Button ID for "3"
//		numpadButtons.add(R.id.soft_key_4);  // Button ID for "4"
//		numpadButtons.add(R.id.soft_key_5);  // Button ID for "5"
//		numpadButtons.add(R.id.soft_key_6);  // Button ID for "6"
//		numpadButtons.add(R.id.soft_key_7);  // Button ID for "7"
//		numpadButtons.add(R.id.soft_key_8);  // Button ID for "8"
//		numpadButtons.add(R.id.soft_key_9);  // Button ID for "9"

//		int[] navPos = {0, 0};
//		ImageButton up_button = main.getView().findViewById(R.id.button1);
//		ImageButton down_button = main.getView().findViewById(R.id.button2);
//		ImageButton left_button = main.getView().findViewById(R.id.button3);
//		ImageButton right_button = main.getView().findViewById(R.id.button4);
//
//		Button the_click = main.getView().findViewById(R.id.leftbuttonT9);


		//int default_softkey = R.id.soft_key_5;
		//int prev_softkey = default_softkey;
//		up_button.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (navPos[1] < 1) {
//					navPos[1]++;
//
//					int softkey_num = getNumber(navPos);
//					View chosen_softkey = main.getView().findViewById(softkey_num);
//					View prev_softkey = main.getView().findViewById(default_softkey);
//
//					// set new colour
//					chosen_softkey.setBackgroundColor(Color.parseColor("#cac6ba"));
//					// change prev/default softkey colour
//					prev_softkey.setBackgroundColor(Color.parseColor("#E7E7E7"));
//
//					// update the new prev/default softkey
//					changePrevSoftKey(softkey_num);
//
//				}
//			}
//		});
//		down_button.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (navPos[1] > -1) {
//					navPos[1]--;
//					int softkey_num = getNumber(navPos);
//					View chosen_softkey = main.getView().findViewById(softkey_num);
//					View prev_softkey = main.getView().findViewById(default_softkey);
//
//					// set new colour
//					chosen_softkey.setBackgroundColor(Color.parseColor("#cac6ba"));
//					// change prev/default softkey colour
//					prev_softkey.setBackgroundColor(Color.parseColor("#E7E7E7"));
//
//					// update the new prev/default softkey
//					changePrevSoftKey(softkey_num);
//				}
////				int numberToDisplay = 42;
////				numberDisplay.setText(String.valueOf(numberToDisplay)); // Update the TextView with the number
//			}
//		});
//
//		left_button.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (navPos[0] > -1) {
//					navPos[0]--;
//					int softkey_num = getNumber(navPos);
//					View chosen_softkey = main.getView().findViewById(softkey_num);
//					View prev_softkey = main.getView().findViewById(default_softkey);
//
//					// set new colour
//					chosen_softkey.setBackgroundColor(Color.parseColor("#cac6ba"));
//					// change prev/default softkey colour
//					prev_softkey.setBackgroundColor(Color.parseColor("#E7E7E7"));
//
//					// update the new prev/default softkey
//					changePrevSoftKey(softkey_num);
//				}
////				int numberToDisplay = 42;
////				numberDisplay.setText(String.valueOf(numberToDisplay)); // Update the TextView with the number
//			}
//		});
//
//		right_button.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (navPos[0] < 1) {
//					navPos[0]++;
//					int softkey_num = getNumber(navPos);
//					View chosen_softkey = main.getView().findViewById(softkey_num);
//					View prev_softkey = main.getView().findViewById(default_softkey);
//
//					// set new colour
//					chosen_softkey.setBackgroundColor(Color.parseColor("#cac6ba"));
//					// change prev/default softkey colour
//					prev_softkey.setBackgroundColor(Color.parseColor("#E7E7E7"));
//
//					// update the new prev/default softkey
//					changePrevSoftKey(softkey_num);
//				}
////				int numberToDisplay = 42;
////				numberDisplay.setText(String.valueOf(numberToDisplay)); // Update the TextView with the number
//			}
//		});

//		the_click.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
////				int numberToDisplay = 42;
////				numberDisplay.setText(String.valueOf(navPos)); // Update the TextView with the number
//				int softkey_num = getNumber(navPos);
//
//				//main.tt9.getApplication();
////				View chosen_softkey = main.getView().findViewById(softkey_num);
////				chosen_softkey.setBackgroundColor(Color.RED);
//				//String displayText = "{" + navPos[0] + ", " + navPos[1] + "}";
////				String displayText = String.valueOf(softkey_num);
////				numberDisplay.setText(displayText);
//			}
//		});

		// get soft key id is below the createInputView

		return true;
	}

	public int getNumber(int[] nav_pos) {
		// Check the array contents using Arrays.equals
		//int softKeyId = -1;

		if (nav_pos[0] == -1 && nav_pos[1] == 1) return R.id.soft_key_1;
		if (nav_pos[0] == 0 && nav_pos[1] == 1) return R.id.soft_key_2;
		if (nav_pos[0] == 1 && nav_pos[1] == 1) return R.id.soft_key_3; // Update this with actual ID if needed
		if (nav_pos[0] == -1 && nav_pos[1] == 0) return R.id.soft_key_4; // Update this with actual ID if needed

		// Additional cases for other positions
		if (nav_pos[0] == 0 && nav_pos[1] == 0) return R.id.soft_key_5; // Update this with actual ID if needed
		if (nav_pos[0] == 1 && nav_pos[1] == 0) return R.id.soft_key_6; // Update this with actual ID if needed
		if (nav_pos[0] == -1 && nav_pos[1] == -1) return R.id.soft_key_7; // Update this with actual ID if needed
		if (nav_pos[0] == 0 && nav_pos[1] == -1) return R.id.soft_key_8; // Update this with actual ID if needed
		if (nav_pos[0] == 1 && nav_pos[1] == -1) return R.id.soft_key_9; // Update this with actual ID if needed

		return -1; // Return -1 if no matching position found
	}

	public void changePrevSoftKey(int softkeyid){
		default_softkey = softkeyid;
	}

	public void forceCreateInputView() {
		main = null;
		if (!createInputView()) {
			Logger.w(getClass().getSimpleName(), "Invalid MainView setting. Creating default.");
			main = new MainLayoutSmall(tt9);
		}
	}

	public View getView() {
		return main.getView();
	}

	public void render() {
		main.hideCommandPalette();
		main.hideTextEditingPalette();
		main.render();
	}

	public void setDarkTheme(boolean darkEnabled) {
		main.setDarkTheme(darkEnabled);
	}

	public void showCommandPalette() {
		if (main != null) {
			main.showCommandPalette();
		}
	}

	public void hideCommandPalette() {
		if (main != null) {
			main.hideCommandPalette();
		}
	}

	public boolean isCommandPaletteShown() {

		return main != null && main.isCommandPaletteShown();
	}

	public void showTextEditingPalette() {
		if (main != null) {
			main.showTextEditingPalette();
		}
	}

	public void hideTextEditingPalette() {
		if (main != null) {
			main.hideTextEditingPalette();
		}
	}

	public boolean isTextEditingPaletteShown() {
		return main != null && main.isTextEditingPaletteShown();
	}
}
