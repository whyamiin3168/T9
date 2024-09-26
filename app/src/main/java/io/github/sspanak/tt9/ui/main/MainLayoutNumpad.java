package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

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
				key.setVisibility(View.INVISIBLE);
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

	public ArrayList<View> getKeypad_pos() {
		ArrayList<View> keypad_pos = new ArrayList<View>(20);

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

	public void setkeypadpos(View keypad){
		keypadView = keypad;
	}

	public View getkeypadView(){
		return keypadView;
	}

	public int getRowMultiplier(int keypadIndex){
		if (keypadIndex >= 0 && keypadIndex <= 4){
			return 1;
		} else if (keypadIndex >= 5 && keypadIndex <= 9){
			return 2;
		} else if (keypadIndex >= 10 && keypadIndex <= 14){
			return 3;
		} else if (keypadIndex >= 15 && keypadIndex <= 19){
			return 4;
		}
		return 0;
	}

	@Override
	void render() {
		getView();
		alignView();
		setKeyHeight(getKeyHeightCompat());
		enableClickHandlers();
		for (SoftKey key : getKeys()) {
			key.render();
		}

		int step = 25;
		isMoving = false;

		//View button = getView().findViewById(R.id.soft_key_1);
		//button.setBackgroundColor(Color.parseColor("#cac6ba"));

//		View view = findViewById(R.id.your_view);
//		float x = view.getX(); // X relative to parent
//		float y = view.getY(); // Y relative to parent
//
//		getView().findViewById(R.id.soft_key_1).getX();



// Runnable to update pointer position continuously
//		Runnable moveUpRunnable = new Runnable() {
//			@Override
//			public void run() {
//				if (isMoving) {
//					movePointer(0, -step);
//					handler.postDelayed(this, 100); // Delay of 100ms for continuous movement
//				}
//			}
//		};
//
//		Runnable moveDownRunnable = new Runnable() {
//			@Override
//			public void run() {
//				if (isMoving) {
//					movePointer(0, step);
//					handler.postDelayed(this, 100);
//				}
//			}
//		};
//
//		Runnable moveLeftRunnable = new Runnable() {
//			@Override
//			public void run() {
//				if (isMoving) {
//					movePointer(-step, 0);
//					handler.postDelayed(this, 100);
//				}
//			}
//		};
//
//		Runnable moveRightRunnable = new Runnable() {
//			@Override
//			public void run() {
//				if (isMoving) {
//					movePointer(step, 0);
//					handler.postDelayed(this, 100);
//				}
//			}
//		};
//
// Setup touch listeners for each button
//		ImageButton upButton = getView().findViewById(R.id.button1);
//		upButton.setOnTouchListener((v, event) -> {
//			if (event.getAction() == MotionEvent.ACTION_DOWN) {
//				isMoving = true;
//				handler.post(moveUpRunnable);
//			} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//				isMoving = false;
//			}
//			return true;
//		});
//
//		ImageButton downButton = getView().findViewById(R.id.button2);
//		downButton.setOnTouchListener((v, event) -> {
//			if (event.getAction() == MotionEvent.ACTION_DOWN) {
//				isMoving = true;
//				handler.post(moveDownRunnable);
//			} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//				isMoving = false;
//			}
//			return true;
//		});
//
//		ImageButton leftButton = getView().findViewById(R.id.button3);
//		leftButton.setOnTouchListener((v, event) -> {
//			if (event.getAction() == MotionEvent.ACTION_DOWN) {
//				isMoving = true;
//				handler.post(moveLeftRunnable);
//			} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//				isMoving = false;
//			}
//			return true;
//		});
//
//		ImageButton rightButton = getView().findViewById(R.id.button4);
//		rightButton.setOnTouchListener((v, event) -> {
//			if (event.getAction() == MotionEvent.ACTION_DOWN) {
//				isMoving = true;
//				handler.post(moveRightRunnable);
//			} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//				isMoving = false;
//			}
//			return true;
//		});

		// add keypads fixed position to ArrayList to extract exact
		// coordinates when movePointer is called

		int index = 1;
		// no need anymore

		if (!inStartingPos) {
			ImageView pointer = getView().findViewById(R.id.pointer);
			View startingkeypad = getView().findViewById(R.id.soft_key_5);
			View numpad_layout = getView().findViewById(R.id.mainnumpadconstraintLayout);
			View statusbar = getView().findViewById(R.id.status_bar_container);
			float startnumlayoutX = numpad_layout.getX();
			int startrowHeight = startingkeypad.getHeight();
			float centerX = startingkeypad.getWidth()/2;
			float centerY = startingkeypad.getHeight()/2;

			//pointer.setX(startingkeypad.getX() + startnumlayoutX + centerX - 4); // Update X position
			pointer.setY((2 * startrowHeight) + startrowHeight + startingkeypad.getY() + statusbar.getHeight() + centerY -10); // Update Y position

			inStartingPos = true;
		}


		ImageButton up_button = getView().findViewById(R.id.button2);
		up_button.setOnClickListener(v -> movePointer(0, -index));

		ImageButton down_button = getView().findViewById(R.id.button3);
		down_button.setOnClickListener(v -> movePointer(0, index));

		ImageButton left_button = getView().findViewById(R.id.button1);
		left_button.setOnClickListener(v -> movePointer(-index, 0));

		ImageButton right_button = getView().findViewById(R.id.button4);
		right_button.setOnClickListener(v -> movePointer(index, 0));

		// simulate the clicking action
		Button clickButton = getView().findViewById(R.id.leftbuttonT9);
		View root_view = getView(); //findViewById(android.R.id.content).getRootView();
		clickButton.setOnClickListener(v -> simulateTouchAtPointerPosition(root_view, 100));

		// right button
		Button holdButton = getView().findViewById(R.id.rightbuttonT9);
		//View root_view = getView(); //findViewById(android.R.id.content).getRootView();
		holdButton.setOnClickListener(v -> simulateTouchAtPointerPositionHold(root_view));


	}

	// no need anymore
	public void movePointer(int deltaX, int deltaY) {

		// pointer view
		ImageView pointer = getView().findViewById(R.id.pointer);

		// all possible keypad views
		ArrayList<View> the_keypads = getKeypad_pos();

		// set default keypad position which is at keypad 5
		if (keypadView == null){
			setkeypadpos(the_keypads.get(7));
		}

		View statusbar = getView().findViewById(R.id.status_bar_container);

		View numpad_layout = getView().findViewById(R.id.mainnumpadconstraintLayout);
		float numlayoutX = numpad_layout.getX();
		float numlayoutY = numpad_layout.getY();

		View settings = getView().findViewById(R.id.soft_key_settings);
		float settings_centerX = settings.getWidth()/2;
		float settings_centerY = settings.getHeight()/2;
		View currentView;

		// move up
		if (deltaX == 0 && deltaY == -1){
			currentView = getkeypadView();
			int keypadIndex = the_keypads.indexOf(currentView);
			//Log.d("jk", String.valueOf(keypadIndex));
			if (keypadIndex >= 5){
				Log.d("beforeup", String.valueOf(keypadIndex));
				// allow to move up
				int newkeypadIndex = keypadIndex - 5;
				View newkeypadView = the_keypads.get(newkeypadIndex);

				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;

				int newkeypadViewHeight = newkeypadView.getHeight();

				int rowMultiplier = getRowMultiplier(newkeypadIndex) - 2;
				int rowHeight = newkeypadView.getHeight();

				pointer.setX(newkeypadView.getX() + numlayoutX + newkeypadView_centerX - 45); // Update X position
				pointer.setY((rowMultiplier * rowHeight) + newkeypadViewHeight + newkeypadView.getY() + statusbar.getHeight() + newkeypadView_centerY - 20); // Update Y position

				// set new keypadView
				setkeypadpos(newkeypadView);

				Log.d("afterup", String.valueOf(the_keypads.indexOf(getkeypadView())));

			}

		// move down
		} else if (deltaX == 0 && deltaY == 1) {
			currentView = getkeypadView();
			int keypadIndex = the_keypads.indexOf(currentView);

			if (keypadIndex < 15){
				Log.d("beforedown", String.valueOf(keypadIndex));
				Log.d("prevkeycoord",currentView.getX() + "," + currentView.getY());
				// allow to move down
				int newkeypadIndex = keypadIndex + 5;
				View newkeypadView = the_keypads.get(newkeypadIndex);

				Log.d("currkeycoord",newkeypadView.getX() + "," + newkeypadView.getY());

				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;

				int rowMultiplier = getRowMultiplier(newkeypadIndex) - 1;
				int rowHeight = newkeypadView.getHeight();

				pointer.setX(newkeypadView.getX() + numlayoutX + newkeypadView_centerX - 45); // Update X position
				pointer.setY((rowMultiplier * rowHeight) + newkeypadView.getY() + statusbar.getHeight() + newkeypadView_centerY - 20); // Update Y position

				// set new keypadView
				setkeypadpos(newkeypadView);

				Log.d("afterdown", String.valueOf(the_keypads.indexOf(getkeypadView())));
				//Log.d("currkeycoord",newkeypadView.getX() + "," + newkeypadView.getY());

			}
		// move left
		} else if (deltaX == -1 && deltaY == 0) {
			currentView = getkeypadView();
			int keypadIndex = the_keypads.indexOf(currentView);

			if ((keypadIndex % 5) != 0){
				// allow to move left
				int newkeypadIndex = keypadIndex - 1;
				View newkeypadView = the_keypads.get(newkeypadIndex);

				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;

				int rowMultiplier = getRowMultiplier(newkeypadIndex) - 1;
				int rowHeight = newkeypadView.getHeight();

				pointer.setX(newkeypadView.getX() + numlayoutX + newkeypadView_centerX - 45); // Update X position
				pointer.setY((rowMultiplier * rowHeight) + newkeypadView.getY() + statusbar.getHeight() + newkeypadView_centerY - 20); // Update Y position

				// set new keypadView
				setkeypadpos(newkeypadView);

			}

		} else if (deltaX == 1 && deltaY == 0) {
			currentView = getkeypadView();
			int keypadIndex = the_keypads.indexOf(currentView);

			if (((keypadIndex-4) % 5) != 0){
				// allow to move left
				int newkeypadIndex = keypadIndex + 1;
				View newkeypadView = the_keypads.get(newkeypadIndex);

				float newkeypadView_centerX = newkeypadView.getWidth()/2;
				float newkeypadView_centerY = newkeypadView.getHeight()/2;

				int rowMultiplier = getRowMultiplier(newkeypadIndex) - 1;
				int rowHeight = newkeypadView.getHeight();

				pointer.setX(newkeypadView.getX() + numlayoutX + newkeypadView_centerX - 45); // Update X position
				pointer.setY((rowMultiplier * rowHeight) + newkeypadView.getY() + statusbar.getHeight() + newkeypadView_centerY - 20); // Update Y position

				// set new keypadView
				setkeypadpos(newkeypadView);

			}

		}

	}

//	public void simulateTouchAtPointerPosition1(View rootView) {
//		// Get the current position of the pointer
//		ImageView pointer = getView().findViewById(R.id.pointer);
//
//		float x = pointer.getX();
//		float y = pointer.getY();
//
//		// Get the current time in milliseconds
//		long downTime = SystemClock.uptimeMillis();
//		long eventTime = SystemClock.uptimeMillis();
//
//		// Create the MotionEvent for ACTION_DOWN (finger press)
//		// Note: because we have another constraint on the left, it takes into
//		// account of 250 dp spacing
//		MotionEvent motionEventDown = MotionEvent.obtain(
//			downTime, eventTime, MotionEvent.ACTION_DOWN, (x), y, 0
//		);
//
//		Log.d("fingerdown","" + downTime);
//
//		// Dispatch the touch event (finger press)
//		rootView.dispatchTouchEvent(motionEventDown);
//
//
//
//		long uptime = SystemClock.uptimeMillis();
//
//		// Create the MotionEvent for ACTION_UP (finger lift)
//		MotionEvent motionEventUp = MotionEvent.obtain(
//			downTime, eventTime, MotionEvent.ACTION_UP, (x), y, 0
//		);
//
//		Log.d("fingerup","" + uptime);
//
//
//		// Dispatch the touch event (finger lift)
//		rootView.dispatchTouchEvent(motionEventUp);
//
//		// Log the coordinates for debugging purposes
//		Log.d("SimulatedTouch", "Touch event simulated at X: " + x + ", Y: " + y);
//
//		// Recycle the MotionEvent objects to avoid memory leaks
//		motionEventDown.recycle();
//		motionEventUp.recycle();
//	}


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


	//
	// using SoftKey method but idk
	//
//	public void onPointerMove(float x, float y) {
//
//		// Loop through the keypad keys and check if the pointer is over any key
//		for (View key : getKeys()) {
//			if (isPointerOverKey(key, x, y)) {
//				simulateClick(key);
//				break;
//			}
//		}
//	}
//
//	public boolean isPointerOverKey(View key, float pointerX, float pointerY) {
//		int[] keyPosition = new int[2];
//
//		key.getLocationOnScreen(keyPosition);
//
//		float keyLeft = keyPosition[0];
//		float keyRight = keyLeft + key.getWidth();
//		float keyTop = keyPosition[1];
//		float keyBottom = keyTop + key.getHeight();
//
//		return (pointerX >= keyLeft && pointerX <= keyRight && pointerY >= keyTop && pointerY <= keyBottom);
//	}
//
//	public void simulateClick(View key) {
//		// Create a MotionEvent to simulate the touch
//		long downTime = SystemClock.uptimeMillis();
//		long eventTime = SystemClock.uptimeMillis();
//
//		MotionEvent motionEvent = MotionEvent.obtain(
//			downTime, eventTime, MotionEvent.ACTION_DOWN,
//			key.getX(), key.getY(), 0
//		);
//
//		// Dispatch the touch event to the key
//		key.dispatchTouchEvent(motionEvent);
//
//		// Log the key interaction
//		Log.d("KeyPress", "Pointer clicked on key: " + key.getId());
//
//		// Create and dispatch the ACTION_UP event to simulate lifting the finger
//		motionEvent = MotionEvent.obtain(
//			downTime, eventTime, MotionEvent.ACTION_UP,
//			key.getX(), key.getY(), 0
//		);
//		key.dispatchTouchEvent(motionEvent);
//	}

	//
	//
	//

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
