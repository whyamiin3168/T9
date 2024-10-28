package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftCommandKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKeySettings;
import io.github.sspanak.tt9.ui.main.keys.SoftNumberKey;
import io.github.sspanak.tt9.ui.main.keys.SoftPunctuationKey;

class MainLayoutNumpad extends BaseMainLayout {
	private boolean isTextEditingShown = false;
	private int height;
	private final Handler handler = new Handler();
	private boolean isMoving = false;
	private View keypadView = null;
	private boolean inStartingPos= false;
	private int rightside = 1;
	private View prevView = null;
	private View preeditView = null;

	MainLayoutNumpad(TraditionalT9 tt9) {
		super(tt9, R.layout.main_numpad);
	}

	private void alignView() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || view == null) {
			return;
		}

		LinearLayout container = view.findViewById(R.id.numpad_container);
		if (container != null) {
			container.setGravity(tt9.getSettings().getNumpadAlignment());
		}
	}

	private int getBackgroundColor(@NonNull View contextView, boolean dark) {
		return ContextCompat.getColor(
			contextView.getContext(),
			dark ? R.color.dark_numpad_background : R.color.numpad_background
		);
	}


	private int getSeparatorColor(@NonNull View contextView, boolean dark) {
		return ContextCompat.getColor(
			contextView.getContext(),
			dark ? R.color.dark_numpad_separator : R.color.numpad_separator
		);
	}


	@Override
	void setDarkTheme(boolean dark) {
		if (view == null) {
			return;
		}

		// background
		view.setBackgroundColor(getBackgroundColor(view, dark));

		// text
		for (SoftKey key : getKeys()) {
			key.setDarkTheme(dark);
		}

		// separators
		int separatorColor = getSeparatorColor(view, dark);
		for (View separator : getSeparators()) {
			if (separator != null) {
				separator.setBackgroundColor(separatorColor);
			}
		}
	}


	@Override void showCommandPalette() {}
	@Override void hideCommandPalette() {}
	@Override boolean isCommandPaletteShown() { return false; }


	@Override
	void showTextEditingPalette() {
		isTextEditingShown = true;

		for (SoftKey key : getKeys()) {
			int keyId = key.getId();

			if (keyId == R.id.soft_key_0) {
				key.setEnabled(tt9 != null && !tt9.isInputModeNumeric());
			} else if (key.getClass().equals(SoftNumberKey.class)) {
				key.setVisibility(View.GONE);
			}

			if (key.getClass().equals(SoftPunctuationKey.class)) {
				key.setVisibility(View.VISIBLE);
			}

			if (key.getClass().equals(SoftCommandKey.class)) {
				key.setVisibility(View.VISIBLE);
			}

			if (keyId == R.id.soft_key_rf3) {
				key.render();
			}

			if (
				keyId == R.id.soft_key_add_word
				|| keyId == R.id.soft_key_input_mode
				|| keyId == R.id.soft_key_language
				|| keyId == R.id.soft_key_filter_suggestions
			) {
				key.setEnabled(false);
			}
		}
	}

	@Override
	void hideTextEditingPalette() {
		isTextEditingShown = false;

		for (SoftKey key : getKeys()) {
			if (key.getClass().equals(SoftNumberKey.class) || key.getClass().equals(SoftPunctuationKey.class)) {
				key.setVisibility(View.VISIBLE);
				key.setEnabled(true);
			}

			if (key.getClass().equals(SoftCommandKey.class)) {
				key.setVisibility(View.GONE);
			}


			int keyId = key.getId();

			if (keyId == R.id.soft_key_rf3) {
				key.render();
			}

			if (
				keyId == R.id.soft_key_add_word
				|| keyId == R.id.soft_key_input_mode
				|| keyId == R.id.soft_key_language
				|| keyId == R.id.soft_key_filter_suggestions
			) {
				key.setEnabled(true);
			}
		}
	}

	@Override
	boolean isTextEditingPaletteShown() {
		return isTextEditingShown;
	}


	/**
	 * Uses the key height from the settings, but if it takes up too much of the screen, it will
	 * be adjusted so that the entire Main View would take up around 50%  of the screen in landscape mode
	 * and 75% in portrait mode. Returns the adjusted height of a single key.
	 */
	private int getKeyHeightCompat() {
		int keyHeight = tt9.getSettings().getNumpadKeyHeight();
		int screenHeight = DeviceInfo.getScreenHeight(tt9.getApplicationContext());

		boolean isLandscape = DeviceInfo.isLandscapeOrientation(tt9.getApplicationContext());
		double maxScreenHeight = isLandscape ? screenHeight * 0.75 : screenHeight * 0.8;
		double maxKeyHeight = isLandscape ? screenHeight * 0.115 : screenHeight * 0.125;

		// it's all very approximate but when it comes to screen dimensions,
		// accuracy is not that important
		return keyHeight * 5 > maxScreenHeight ? (int) Math.round(maxKeyHeight) : keyHeight;
	}


	void setKeyHeight(int height) {
		if (view == null || height <= 0) {
			return;
		}

		ViewGroup table = view.findViewById(R.id.main_soft_keys);
		int tableRowsCount = table.getChildCount();

		for (int rowId = 0; rowId < tableRowsCount; rowId++) {
			View row = table.getChildAt(rowId);
			ViewGroup.LayoutParams layout = row.getLayoutParams();
			if (layout != null) {
				layout.height = height;
				row.setLayoutParams(layout);
			}
		}
	}


	int getHeight(boolean forceRecalculate) {
		if (height <= 0 || forceRecalculate) {
			Resources resources = tt9.getResources();
			height = getKeyHeightCompat() * 4
				+ resources.getDimensionPixelSize(R.dimen.numpad_candidate_height)
				+ resources.getDimensionPixelSize(R.dimen.numpad_padding_bottom) * 4;
		}

		return height;
	}

	// Returns a list of keypad positions within the main numpad layout
	public ArrayList<View> getKeypad_pos() {
		ArrayList<View> keypad_pos = new ArrayList<View>(22);

		// status bar row
		keypad_pos.add(getView().findViewById(R.id.soft_key_left_arrow));
		keypad_pos.add(getView().findViewById(R.id.soft_key_right_arrow));

		// first row
		keypad_pos.add(getView().findViewById(R.id.soft_key_settings));
		keypad_pos.add(getView().findViewById(R.id.soft_key_1));
		keypad_pos.add(getView().findViewById(R.id.soft_key_2));
		keypad_pos.add(getView().findViewById(R.id.soft_key_3));
		keypad_pos.add(getView().findViewById(R.id.soft_key_backspace));

		// second row
		keypad_pos.add(getView().findViewById(R.id.soft_key_add_word));
		keypad_pos.add(getView().findViewById(R.id.soft_key_4));
		keypad_pos.add(getView().findViewById(R.id.soft_key_5));
		keypad_pos.add(getView().findViewById(R.id.soft_key_6));
		keypad_pos.add(getView().findViewById(R.id.soft_key_filter_suggestions));

		// third row
		keypad_pos.add(getView().findViewById(R.id.soft_key_input_mode));
		keypad_pos.add(getView().findViewById(R.id.soft_key_7));
		keypad_pos.add(getView().findViewById(R.id.soft_key_8));
		keypad_pos.add(getView().findViewById(R.id.soft_key_9));
		keypad_pos.add(getView().findViewById(R.id.soft_key_rf3));

		// fourth row
		keypad_pos.add(getView().findViewById(R.id.soft_key_language));
		keypad_pos.add(getView().findViewById(R.id.soft_key_punctuation_1));
		keypad_pos.add(getView().findViewById(R.id.soft_key_0));
		keypad_pos.add(getView().findViewById(R.id.soft_key_punctuation_2));
		keypad_pos.add(getView().findViewById(R.id.soft_key_ok));

		return keypad_pos;
	}

	// Returns a list of keypad positions within the text editting layout
	public ArrayList<View> getEdittingKeypad_pos() {
		ArrayList<View> keypad_pos = new ArrayList<View>(22);

		// status bar row
		keypad_pos.add(getView().findViewById(R.id.soft_key_left_arrow));
		keypad_pos.add(getView().findViewById(R.id.soft_key_right_arrow));

		// first row
		keypad_pos.add(getView().findViewById(R.id.soft_key_settings));
		keypad_pos.add(getView().findViewById(R.id.soft_key_101));
		keypad_pos.add(getView().findViewById(R.id.soft_key_102));
		keypad_pos.add(getView().findViewById(R.id.soft_key_103));
		keypad_pos.add(getView().findViewById(R.id.soft_key_backspace));

		// second row
		keypad_pos.add(getView().findViewById(R.id.soft_key_add_word));
		keypad_pos.add(getView().findViewById(R.id.soft_key_104));
		keypad_pos.add(getView().findViewById(R.id.soft_key_105));
		keypad_pos.add(getView().findViewById(R.id.soft_key_106));
		keypad_pos.add(getView().findViewById(R.id.soft_key_filter_suggestions));

		// third row
		keypad_pos.add(getView().findViewById(R.id.soft_key_input_mode));
		keypad_pos.add(getView().findViewById(R.id.soft_key_107));
		keypad_pos.add(getView().findViewById(R.id.soft_key_108));
		keypad_pos.add(getView().findViewById(R.id.soft_key_109));
		keypad_pos.add(getView().findViewById(R.id.soft_key_rf3));

		// fourth row
		keypad_pos.add(getView().findViewById(R.id.soft_key_language));
		keypad_pos.add(getView().findViewById(R.id.soft_key_punctuation_1));
		keypad_pos.add(getView().findViewById(R.id.soft_key_0));
		keypad_pos.add(getView().findViewById(R.id.soft_key_punctuation_2));
		keypad_pos.add(getView().findViewById(R.id.soft_key_ok));

		return keypad_pos;
	}

	// Assign chosen keypad as current keypad View
	public void setkeypadpos(View keypad){
		keypadView = keypad;
	}

	// Gets the stored keypad View
	public View getkeypadView(){
		return keypadView;
	}

	// Returns row number for calculating pointer position at chosen row
	public int getRowMultiplier(int keypadIndex){
		if (keypadIndex >= 2 && keypadIndex <= 6){
			return 1;
		} else if (keypadIndex >= 7 && keypadIndex <= 11){
			return 2;
		} else if (keypadIndex >= 12 && keypadIndex <= 16){
			return 3;
		} else if (keypadIndex >= 17 && keypadIndex <= 21){
			return 4;
		}
		return 0;
	}

	// The render function has been modified to facilitate key mapping system
	// This approach is not recommended as touch function on a key should be
	// done within typing handler class if possible.
	// This approach is chosen due to low complexity and higher flexibility in
	// changing the UI of the system
	@Override
	void render() {
		getView();
		alignView();
		setKeyHeight(getKeyHeightCompat());
		enableClickHandlers();
		for (SoftKey key : getKeys()) {
			key.render();
		}

		isMoving = false;
		int index = 1;

		// Set the pointer position when T9 has its first launch
		if (!inStartingPos) {
			// Extract the necessary Views
			ImageView pointer = getView().findViewById(R.id.pointer);
			View startingkeypad = getView().findViewById(R.id.soft_key_5); // The pointer initial position
			View startingedittingkeypad = getView().findViewById(R.id.soft_key_105); // The edit keypad version
			View numpad_layout = getView().findViewById(R.id.mainnumpadconstraintLayout);
			View statusbar = getView().findViewById(R.id.status_bar_container);

			// Get the Y coordinate for initial position
			int startrowHeight = startingkeypad.getHeight();
			float centerY = startingkeypad.getHeight()/2;

			// Set initial background colour
			int color = ContextCompat.getColor(getView().getContext(), R.color.pointer_colour);
			startingkeypad.setBackgroundColor(color);
			startingedittingkeypad.setBackgroundColor(color);

			pointer.setX(startingkeypad.getX()); // Update X position
			pointer.setY((2 * startrowHeight) + startrowHeight + startingkeypad.getY() + statusbar.getHeight() + centerY -10); // Update Y position

			// Track the previously assigned keypad to reconfigure background colour
			prevView = startingkeypad;
			preeditView = startingedittingkeypad;
			inStartingPos = true; // signal initial launch has occured
		}

		// View for all other views within the main numpad layout
		View root_view = getView();

		// left view
		ImageButton left_up_button = getView().findViewById(R.id.leftbutton2);
		left_up_button.setOnClickListener(v -> movePointer(0, -index));

		ImageButton left_down_button = getView().findViewById(R.id.leftbutton3);
		left_down_button.setOnClickListener(v -> movePointer(0, index));

		ImageButton left_left_button = getView().findViewById(R.id.leftbutton1);
		left_left_button.setOnClickListener(v -> movePointer(-index, 0));

		ImageButton left_right_button = getView().findViewById(R.id.leftbutton4);
		left_right_button.setOnClickListener(v -> movePointer(index, 0));

		// simulate the clicking action
		ImageButton leftclickButton = getView().findViewById(R.id.leftselectbuttonT9);
		leftclickButton.setOnClickListener(v -> simulateTouchAtPointerPosition(root_view, 100));

		// simulate hold action
		ImageButton leftholdButton = getView().findViewById(R.id.leftholdbuttonT9);
		leftholdButton.setOnClickListener(v -> simulateTouchAtPointerPositionHold(root_view));

		//
		// right view
		ImageButton right_up_button = getView().findViewById(R.id.rightbutton2);
		right_up_button.setOnClickListener(v -> movePointer(0, -index));

		ImageButton right_down_button = getView().findViewById(R.id.rightbutton3);
		right_down_button.setOnClickListener(v -> movePointer(0, index));

		ImageButton right_left_button = getView().findViewById(R.id.rightbutton1);
		right_left_button.setOnClickListener(v -> movePointer(-index, 0));

		ImageButton right_right_button = getView().findViewById(R.id.rightbutton4);
		right_right_button.setOnClickListener(v -> movePointer(index, 0));

		// simulate the clicking action
		ImageButton rightclickButton = getView().findViewById(R.id.rightselectbuttonT9);
		rightclickButton.setOnClickListener(v -> simulateTouchAtPointerPosition(root_view, 100));

		// hold action button
		ImageButton holdButton = getView().findViewById(R.id.rightholdbuttonT9);
		holdButton.setOnClickListener(v -> simulateTouchAtPointerPositionHold(root_view));

		// swap side of key amapping layout
		ViewSwitcher leftviewSwitcher = getView().findViewById(R.id.leftviewswitcher);
		ViewSwitcher rightviewSwitcher = getView().findViewById(R.id.rightviewswitcher);
		Button leftswitchButton = getView().findViewById(R.id.leftswitchbutton);
		Button rightswitchButton = getView().findViewById(R.id.rightswitchbutton);

		// Set the necessary layout (direction or press action) on each side
		// Currently, the swap button resides visibly on right side but can be made visible
		// once button size in main_numpad.xml layout is set large enough
		leftswitchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				leftviewSwitcher.setDisplayedChild(1);
				rightviewSwitcher.setDisplayedChild(1);
			}
		});

		// Set the necessary layout (direction or press action) on each side
		// Note: rightside variable performs check whether to "ignore" swap when already on right
		rightswitchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (rightside == 1){
					leftviewSwitcher.setDisplayedChild(1);
					rightviewSwitcher.setDisplayedChild(1);
					rightside = 0;
				} else if (rightside == 0){
					leftviewSwitcher.setDisplayedChild(0);
					rightviewSwitcher.setDisplayedChild(0);
					rightside = 1;
				}

			}
		});


	}

	// Navigate pointer based on the direction button that has been pressed on
	public void movePointer(int deltaX, int deltaY) {

		// pointer view
		ImageView pointer = getView().findViewById(R.id.pointer);

		// all possible keypad views
		ArrayList<View> the_keypads = getKeypad_pos();
		ArrayList<View> editting_keypads = getEdittingKeypad_pos();

		// set default keypad position which is at keypad 5
		if (keypadView == null){
			setkeypadpos(the_keypads.get(9));
		}

		// Get the necessary Views and dimensions of main numpad layout
		View statusbar = getView().findViewById(R.id.status_bar_container);
		View numpad_layout = getView().findViewById(R.id.mainnumpadconstraintLayout);
		float numlayoutX = numpad_layout.getX();
		float numlayoutY = numpad_layout.getY();
		View currentView;

		// move up
		if (deltaX == 0 && deltaY == -1){
			currentView = getkeypadView();
			int keypadIndex = the_keypads.indexOf(currentView);

			// This moves up to the arrow keys navigating the suggestion bar
			if (keypadIndex == 2 || keypadIndex == 6){
				int newkeypadIndex;

				// Retrieve the keypad index that has been chosen
				if (keypadIndex == 2) {
					newkeypadIndex = keypadIndex - 2;
				} else {
					newkeypadIndex = keypadIndex - 5;
				}

				Log.d("beforeup", String.valueOf(keypadIndex)); // for debugging purpose
				// allow to move up to the soft key arrows
				View newkeypadView = the_keypads.get(newkeypadIndex);

				//
				//
				// change pointer size
				// Set new width and height
				int newWidth = 10; // in pixels
				int newHeight = 10; // in pixels

				// Get the current layout parameters of the ImageView
				ViewGroup.LayoutParams layoutParams = pointer.getLayoutParams();

				// Update the width and height
				layoutParams.width = newWidth;
				layoutParams.height = newHeight;

				// Apply the new layout parameters and coordinates to the pointer
				pointer.setLayoutParams(layoutParams);
				pointer.setX(newkeypadView.getX() + numlayoutX - 10);
				pointer.setY( newkeypadView.getY() );

				// Modify the background colour for the chosen keypad and previous keypad
				int prevcolor = newkeypadView.getSolidColor();
				prevView.setBackgroundColor(prevcolor);
				int color = ContextCompat.getColor(getView().getContext(), R.color.pointer_colour);
				newkeypadView.setBackgroundColor(color);
				prevView = newkeypadView;

				// set new keypadView
				setkeypadpos(newkeypadView);

				Log.d("afterup", String.valueOf(the_keypads.indexOf(getkeypadView()))); // for debugging purposes

			} // top row to get to softkeyarrows in the suggestions list

			// Navigate below arrow keys and within numpad
			else if (keypadIndex >= 7){
				Log.d("beforeup", String.valueOf(keypadIndex)); // for debugging purpose

				// Retrieve the keypad index that has been chosen
				int newkeypadIndex = keypadIndex - 5;
				View newkeypadView = the_keypads.get(newkeypadIndex);
				View newedittingkeypadView = editting_keypads.get(newkeypadIndex);

				// Get coordinates to position the pointer at the correct row
				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;
				int newkeypadViewHeight = newkeypadView.getHeight();
				int rowMultiplier = getRowMultiplier(newkeypadIndex) - 2;
				int rowHeight = newkeypadView.getHeight();

				//
				//
				// change back pointer size
				// Set new width and height
				int newWidth = 70; // in pixels
				int newHeight = 50; // in pixels
				float density = getView().getResources().getDisplayMetrics().density;
				int newWidthPx = (int) (newWidth * density);
				int newHeightPx = (int) (newHeight * density);

				// Get the current layout parameters of the ImageView
				ViewGroup.LayoutParams layoutParams = pointer.getLayoutParams();

				// Update the width and height
				layoutParams.width = newWidthPx;
				layoutParams.height = newHeightPx;

				// Apply the new layout parameters to the ImageView
				pointer.setLayoutParams(layoutParams);

				// Action keypads on the side have smaller width (28)
				List<Integer> excludedIndices = Arrays.asList(2,7,12,17, 6,11, 16, 21);
				if (!excludedIndices.contains(newkeypadIndex)) {
					pointer.setX(newkeypadView.getX() + numlayoutX + 38);
				} else {
					pointer.setX(newkeypadView.getX() + numlayoutX + 28);
				}

				// Only update Y position is required
				pointer.setY((rowMultiplier * rowHeight) + newkeypadViewHeight + newkeypadView.getY() + statusbar.getHeight() + newkeypadView_centerY - 20);

				// Modify the background colour for the chosen keypad and previous keypad
				int prevcolor = newkeypadView.getSolidColor();
				prevView.setBackgroundColor(prevcolor);
				preeditView.setBackgroundColor(prevcolor); // for edit keypad
				int color = ContextCompat.getColor(getView().getContext(), R.color.pointer_colour);
				newkeypadView.setBackgroundColor(color);
				newedittingkeypadView.setBackgroundColor(color);
				prevView = newkeypadView;
				preeditView = newedittingkeypadView;


				// set new keypadView
				setkeypadpos(newkeypadView);

				Log.d("afterup", String.valueOf(the_keypads.indexOf(getkeypadView()))); // for debugging purpose

			} // below suggestion list
		// move down
		} else if (deltaX == 0 && deltaY == 1) {
			currentView = getkeypadView();
			int keypadIndex = the_keypads.indexOf(currentView);

			// This moves down from the arrow keys to main numpad
			if (keypadIndex == 0 || keypadIndex == 1){
				int newkeypadIndex;

				// Retrieve the keypad index that has been chosen
				if (keypadIndex == 0) {
					newkeypadIndex = keypadIndex + 2;
				} else {
					newkeypadIndex = keypadIndex + 5;
				}

				Log.d("beforeup", String.valueOf(keypadIndex)); // for debugging purpose
				// allow to move up to the soft key arrows
				View newkeypadView = the_keypads.get(newkeypadIndex);
				View newedittingkeypadView = editting_keypads.get(newkeypadIndex);

				// soft arrow keys < and > x & y coordinates
				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;
				int rowHeight = newkeypadView.getHeight();

				//
				//
				// change pointer size
				// Set new width and height
				int newWidth = 70; // in pixels
				int newHeight = 50; // in pixels
				float density = getView().getResources().getDisplayMetrics().density;
				int newWidthPx = (int) (newWidth * density);
				int newHeightPx = (int) (newHeight * density);

				// Get the current layout parameters of the ImageView
				ViewGroup.LayoutParams layoutParams = pointer.getLayoutParams();

				// Update the width and height
				layoutParams.width = newWidthPx;
				layoutParams.height = newHeightPx;

				// Apply the new layout parameters to the ImageView
				pointer.setLayoutParams(layoutParams);

				pointer.setX(newkeypadView.getX() + numlayoutX + 55); // Update X position
				pointer.setY( newkeypadView.getY() + statusbar.getHeight() + newkeypadView_centerY); // Update Y position

				// Modify the background colour for the chosen keypad and previous keypad
				int prevcolor = newkeypadView.getSolidColor();
				prevView.setBackgroundColor(prevcolor);
				preeditView.setBackgroundColor(prevcolor);
				int color = ContextCompat.getColor(getView().getContext(), R.color.pointer_colour);
				newkeypadView.setBackgroundColor(color);
				newedittingkeypadView.setBackgroundColor(color);
				prevView = newkeypadView;
				preeditView = newedittingkeypadView;

				// set new keypadView
				setkeypadpos(newkeypadView);

				Log.d("afterup", String.valueOf(the_keypads.indexOf(getkeypadView()))); // for debugging purpose

			} // from softkeyarrows to numpad


			else if (keypadIndex < 17 && (keypadIndex != 0 || keypadIndex != 1)){
				Log.d("beforedown", String.valueOf(keypadIndex)); // for debugging purpose

				// Retrieve the keypad index that has been chosen
				int newkeypadIndex = keypadIndex + 5;
				View newkeypadView = the_keypads.get(newkeypadIndex);
				View newedittingkeypadView = editting_keypads.get(newkeypadIndex);

				// Get coordinates to position the pointer at the correct row
				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;
				int rowMultiplier = getRowMultiplier(newkeypadIndex) - 1;
				int rowHeight = newkeypadView.getHeight();

				//
				//
				// change back pointer size
				// Set new width and height
				int newWidth = 70; // in pixels
				int newHeight = 50; // in pixels
				float density = getView().getResources().getDisplayMetrics().density;
				int newWidthPx = (int) (newWidth * density);
				int newHeightPx = (int) (newHeight * density);

				// Get the current layout parameters of the ImageView
				ViewGroup.LayoutParams layoutParams = pointer.getLayoutParams();

				// Update the width and height
				layoutParams.width = newWidthPx;
				layoutParams.height = newHeightPx;

				// Apply the new layout parameters to the ImageView
				pointer.setLayoutParams(layoutParams);

				// Action keypads on the side have smaller width (28)
				List<Integer> excludedIndices = Arrays.asList(2,7,12,17, 6, 11, 16,21);
				if (!excludedIndices.contains(newkeypadIndex)) {
					pointer.setX(newkeypadView.getX() + numlayoutX + 38);
				} else {
					pointer.setX(newkeypadView.getX() + numlayoutX + 28);
				}

				// Only update Y position is required
				pointer.setY((rowMultiplier * rowHeight) + newkeypadView.getY() + statusbar.getHeight() + newkeypadView_centerY - 20);

				// Modify the background colour for the chosen keypad and previous keypad
				int prevcolor = newkeypadView.getSolidColor();
				prevView.setBackgroundColor(prevcolor);
				preeditView.setBackgroundColor(prevcolor);
				int color = ContextCompat.getColor(getView().getContext(), R.color.pointer_colour);
				newkeypadView.setBackgroundColor(color);
				newedittingkeypadView.setBackgroundColor(color);
				prevView = newkeypadView;
				preeditView = newedittingkeypadView;

				// set new keypadView
				setkeypadpos(newkeypadView);

				Log.d("afterdown", String.valueOf(the_keypads.indexOf(getkeypadView()))); // fpr debugging purpose

			} // not at bottom of numpad
		// move left
		} else if (deltaX == -1 && deltaY == 0) {
			currentView = getkeypadView();
			int keypadIndex = the_keypads.indexOf(currentView);

			// For right arrow key
			if (keypadIndex != 0 && keypadIndex == 1){

				// Retrieve the keypad index that has been chosen
				int newkeypadIndex = 0;

				Log.d("beforeup", String.valueOf(keypadIndex)); // for debugging purpose
				// allow to move up to the soft key arrows
				View newkeypadView = the_keypads.get(newkeypadIndex);
				View newedittingkeypadView = editting_keypads.get(newkeypadIndex);

				// soft arrow keys < and > x & y coordinates
				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;
				int rowHeight = newkeypadView.getHeight();

				//
				//
				// change pointer size
				// Set new width and height
				int newWidth = 10; // in pixels
				int newHeight = 10; // in pixels

				// Get the current layout parameters of the ImageView
				ViewGroup.LayoutParams layoutParams = pointer.getLayoutParams();

				// Update the width and height
				layoutParams.width = newWidth;
				layoutParams.height = newHeight;

				// Apply the new layout parameters to the ImageView
				pointer.setLayoutParams(layoutParams);

				pointer.setX(newkeypadView.getX() + numlayoutX ); // Update X position
				pointer.setY( newkeypadView.getY() + ((newkeypadView_centerY)/2) + 2); // Update Y position

				// Modify the background colour for the chosen keypad and previous keypad
				int prevcolor = newkeypadView.getSolidColor();
				prevView.setBackgroundColor(prevcolor);
				preeditView.setBackgroundColor(prevcolor);
				int color = ContextCompat.getColor(getView().getContext(), R.color.pointer_colour);
				newkeypadView.setBackgroundColor(color);
				newedittingkeypadView.setBackgroundColor(color);
				prevView = newkeypadView;
				preeditView = newedittingkeypadView;

				// set new keypadView
				setkeypadpos(newkeypadView);

				Log.d("afterup", String.valueOf(the_keypads.indexOf(getkeypadView()))); // for debugging purpose

			} // within row of softkeyarrows

			// Below suggestion bar but not leftmost column of action keypads
			else if ( ((keypadIndex-2) % 5)  != 0 && (keypadIndex != 0)){
				// allow to move left
				int newkeypadIndex = keypadIndex - 1;
				View newkeypadView = the_keypads.get(newkeypadIndex);
				View newedittingkeypadView = editting_keypads.get(newkeypadIndex);

				// Retrieve the keypad index that has been chosen
				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;
				int rowMultiplier = getRowMultiplier(newkeypadIndex) - 1;
				int rowHeight = newkeypadView.getHeight();

				//
				//
				// change back pointer size
				// Set new width and height
				int newWidth = 70; // in pixels
				int newHeight = 50; // in pixels
				float density = getView().getResources().getDisplayMetrics().density;
				int newWidthPx = (int) (newWidth * density);
				int newHeightPx = (int) (newHeight * density);

				// Get the current layout parameters of the ImageView
				ViewGroup.LayoutParams layoutParams = pointer.getLayoutParams();

				// Update the width and height
				layoutParams.width = newWidthPx;
				layoutParams.height = newHeightPx;

				// Apply the new layout parameters to the ImageView
				pointer.setLayoutParams(layoutParams);

				// Action keypads on the side have smaller width (28)
				List<Integer> excludedIndices = Arrays.asList(2, 7, 12, 17);
				if (!excludedIndices.contains(newkeypadIndex)) {
					pointer.setX(newkeypadView.getX() + numlayoutX + 38);
				} else {
					pointer.setX(newkeypadView.getX() + numlayoutX + 28);
				}

				// Only update Y position
				pointer.setY((rowMultiplier * rowHeight) + newkeypadView.getY() + statusbar.getHeight() + newkeypadView_centerY - 20);

				// Modify the background colour for the chosen keypad and previous keypad
				int prevcolor = newkeypadView.getSolidColor();
				prevView.setBackgroundColor(prevcolor);
				preeditView.setBackgroundColor(prevcolor);
				int color = ContextCompat.getColor(getView().getContext(), R.color.pointer_colour);
				newkeypadView.setBackgroundColor(color);
				newedittingkeypadView.setBackgroundColor(color);
				prevView = newkeypadView;
				preeditView = newedittingkeypadView;

				// set new keypadView
				setkeypadpos(newkeypadView);

			} // within rows of numpad and boundary of row
		// move right
		} else if (deltaX == 1 && deltaY == 0) {
			currentView = getkeypadView();
			int keypadIndex = the_keypads.indexOf(currentView);

			// For left arrow key
			if (keypadIndex == 0 && keypadIndex != 1){

				// Retrieve the keypad index that has been chosen
				int newkeypadIndex = 1;

				Log.d("beforeup", String.valueOf(keypadIndex)); // for debugging purpose
				// allow to move up to the soft key arrows
				View newkeypadView = the_keypads.get(newkeypadIndex);

				// soft arrow keys < and > x & y coordinates
				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;
				int rowHeight = newkeypadView.getHeight();

				//
				//
				// change pointer size
				// Set new width and height
				int newWidth = 10; // in pixels
				int newHeight = 10; // in pixels

				// Get the current layout parameters of the ImageView
				ViewGroup.LayoutParams layoutParams = pointer.getLayoutParams();

				// Update the width and height
				layoutParams.width = newWidth;
				layoutParams.height = newHeight;

				// Apply the new layout parameters to the ImageView
				pointer.setLayoutParams(layoutParams);

				pointer.setX(newkeypadView.getX() + numlayoutX); // Update X position
				pointer.setY( newkeypadView.getY() + ((newkeypadView_centerY)/2) + 2); // Update Y position

				// Modify the background colour for the chosen keypad and previous keypad
				int prevcolor = newkeypadView.getSolidColor();
				prevView.setBackgroundColor(prevcolor);
				int color = ContextCompat.getColor(getView().getContext(), R.color.pointer_colour);
				newkeypadView.setBackgroundColor(color);
				prevView = newkeypadView;

				// set new keypadView
				setkeypadpos(newkeypadView);

				Log.d("afterup", String.valueOf(the_keypads.indexOf(getkeypadView()))); // for debugging purpose

			} // within row of softkeyarrows

			// Below suggestion bar but not rightmost column of action keypads
			else if (((keypadIndex-1) % 5) != 0){
				// allow to move right
				int newkeypadIndex = keypadIndex + 1;
				View newkeypadView = the_keypads.get(newkeypadIndex);
				View newedittingkeypadView = editting_keypads.get(newkeypadIndex);

				// Retrieve the keypad index that has been chosen
				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;
				int rowMultiplier = getRowMultiplier(newkeypadIndex) - 1;
				int rowHeight = newkeypadView.getHeight();

				//
				//
				// change back pointer size
				// Set new width and height
				int newWidth = 70; // in pixels
				int newHeight = 50; // in pixels
				float density = getView().getResources().getDisplayMetrics().density;
				int newWidthPx = (int) (newWidth * density);
				int newHeightPx = (int) (newHeight * density);

				// Get the current layout parameters of the ImageView
				ViewGroup.LayoutParams layoutParams = pointer.getLayoutParams();

				// Update the width and height
				layoutParams.width = newWidthPx;
				layoutParams.height = newHeightPx;

				// Apply the new layout parameters to the ImageView
				pointer.setLayoutParams(layoutParams);

				// Action keypads on the side have smaller width (28)
				List<Integer> excludedIndices = Arrays.asList(6, 11, 16, 21);
				if (!excludedIndices.contains(newkeypadIndex)) {
					pointer.setX(newkeypadView.getX() + numlayoutX + 38);
				} else {
					pointer.setX(newkeypadView.getX() + numlayoutX + 28);
				}

				// Modify the background colour for the chosen keypad and previous keypad
				int prevcolor = newkeypadView.getSolidColor();
				prevView.setBackgroundColor(prevcolor);
				preeditView.setBackgroundColor(prevcolor);
				int color = ContextCompat.getColor(getView().getContext(), R.color.pointer_colour);
				newkeypadView.setBackgroundColor(color);
				newedittingkeypadView.setBackgroundColor(color);
				prevView = newkeypadView;
				preeditView = newedittingkeypadView;

				// Only update Y position is required
				pointer.setY((rowMultiplier * rowHeight) + newkeypadView.getY() + statusbar.getHeight() + newkeypadView_centerY - 20);

				// set new keypadView
				setkeypadpos(newkeypadView);

			} // within rows of numpad and boundary of row
		}

	}

	// Mimic a single press action on the keypads
	public void simulateTouchAtPointerPosition(View rootView, int delaytime) {
		// Get the current position of the pointer (T9 keypad button)
		ImageView pointer = rootView.findViewById(R.id.pointer);

		// Get the pointer's X and Y coordinates
		float x = pointer.getX();
		float y = pointer.getY();

		// Get the current time in milliseconds for the start of the press
		long downTime = SystemClock.uptimeMillis();
		long eventTime = downTime;

		// Create the MotionEvent for ACTION_DOWN (finger press)
		MotionEvent motionEventDown = MotionEvent.obtain(
			downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0
		);

		// Dispatch the ACTION_DOWN event (finger press)
		rootView.dispatchTouchEvent(motionEventDown);

		// Log the start of the touch event for debugging purposes
		Log.d("SimulatedTouch", "Touch event started at X: " + x + ", Y: " + y);

		// Simulate a quick tap by immediately dispatching ACTION_UP if the user releases before 1000ms
		rootView.postDelayed(() -> {
			long upTime = SystemClock.uptimeMillis();
			if (upTime - downTime < 1000){
				Log.d("SimulatedTouch", "Quick tap detected at X: " + x + ", Y: " + y);

				// Dispatch the ACTION_UP event for a quick tap (for letters)
				MotionEvent motionEventUp = MotionEvent.obtain(
					downTime, upTime, MotionEvent.ACTION_UP, x, y, 0
				);
				rootView.dispatchTouchEvent(motionEventUp);

				// Log the quick tap release
				Log.d("SimulatedTouch", "Quick tap event released at X: " + x + ", Y: " + y + " (Tap for letters)");

				// Recycle the MotionEvent to avoid memory leaks
				motionEventUp.recycle();

			}
		}, delaytime); // Adjust the delay as needed for quick tap detection

		// Recycle the ACTION_DOWN MotionEvent to avoid memory leaks
		motionEventDown.recycle();
	}

	// Mimic press and hold action on the keypads
	public void simulateTouchAtPointerPositionHold(View rootView) {
		// Get the current position of the pointer (T9 keypad button)
		ImageView pointer = getView().findViewById(R.id.pointer);

		// Get the pointer's X and Y coordinates
		float x = pointer.getX();
		float y = pointer.getY();

		// Get the current time in milliseconds for the start of the press
		long downTime = SystemClock.uptimeMillis();
		long[] eventTime = {SystemClock.uptimeMillis()}; // Use array to allow modification inside inner class

		// Create the MotionEvent for ACTION_DOWN (finger press)
		MotionEvent motionEventDown = MotionEvent.obtain(
			downTime, eventTime[0], MotionEvent.ACTION_DOWN, x, y, 0
		);

		// Dispatch the ACTION_DOWN event (finger press)
		rootView.dispatchTouchEvent(motionEventDown);

		// Log the start of the touch event for debugging purposes
		Log.d("SimulatedTouch", "Touch event started at X: " + x + ", Y: " + y);

		// Create a handler to simulate a hold if press lasts longer than 1000 milliseconds
		Handler handler = new Handler();
		handler.postDelayed(() -> {
			// If still holding after 1000 milliseconds, it’s a long press
			eventTime[0] = SystemClock.uptimeMillis(); // Update event time
			Log.d("SimulatedTouch", "Hold event detected at X: " + x + ", Y: " + y);

			// Dispatch ACTION_UP after holding (for numbers)
			MotionEvent motionEventUp = MotionEvent.obtain(
				downTime, eventTime[0], MotionEvent.ACTION_UP, x, y, 0
			);
			rootView.dispatchTouchEvent(motionEventUp);

			// Log the long press release
			Log.d("SimulatedTouch", "Hold event released at X: " + x + ", Y: " + y + " (Hold for numbers)");

			// Recycle the MotionEvent to avoid memory leaks
			motionEventUp.recycle();
		}, 1000); // 1000 milliseconds = 1 second for a long press

		// Detect a quick tap by immediately dispatching ACTION_UP if the user releases before 1000ms

		// Recycle the ACTION_DOWN MotionEvent to avoid memory leaks
		motionEventDown.recycle();
	}

	@Override
	protected void enableClickHandlers() {
		super.enableClickHandlers();

		for (SoftKey key : getKeys()) {
			if (key instanceof SoftKeySettings) {
				((SoftKeySettings) key).setMainView(tt9.getMainView());
			}
		}
	}


	@NonNull
	@Override
	protected ArrayList<SoftKey> getKeys() {
		if (!keys.isEmpty()) {
			return keys;
		}

		ViewGroup table = view.findViewById(R.id.main_soft_keys);
		int tableRowsCount = table.getChildCount();

		for (int rowId = 0; rowId < tableRowsCount; rowId++) {
			View row = table.getChildAt(rowId);
			if (row instanceof ViewGroup) {
				keys.addAll(getKeysFromContainer((ViewGroup) row));
			}
		}

		keys.addAll(getKeysFromContainer(view.findViewById(R.id.status_bar_container)));

		return keys;
	}


	protected ArrayList<View> getSeparators() {
		// it's fine... it's shorter, faster and easier to read than searching with 3 nested loops
		return new ArrayList<>(Arrays.asList(
			view.findViewById(R.id.separator_top),
			view.findViewById(R.id.separator_candidates_1),
			view.findViewById(R.id.separator_candidates_2),
			view.findViewById(R.id.separator_candidates_bottom),
			view.findViewById(R.id.separator_1_1),
			view.findViewById(R.id.separator_1_2),
			view.findViewById(R.id.separator_2_1),
			view.findViewById(R.id.separator_2_2),
			view.findViewById(R.id.separator_3_1),
			view.findViewById(R.id.separator_3_2),
			view.findViewById(R.id.separator_4_1),
			view.findViewById(R.id.separator_4_2)
		));
	}
}
