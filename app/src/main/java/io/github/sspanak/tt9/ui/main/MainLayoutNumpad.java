package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftCommandKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyArrow;
import io.github.sspanak.tt9.ui.main.keys.SoftKeySettings;
import io.github.sspanak.tt9.ui.main.keys.SoftNumberKey;
import io.github.sspanak.tt9.ui.main.keys.SoftPunctuationKey;

class MainLayoutNumpad extends BaseMainLayout {
	private boolean isTextEditingShown = false;
	private int height;
	private final Handler handler = new Handler();
	private boolean isMoving = false;

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

// Create a handler to continuously update the pointer position
		//Handler handler = new Handler();

// Runnable to update pointer position continuously
		Runnable moveUpRunnable = new Runnable() {
			@Override
			public void run() {
				if (isMoving) {
					movePointer(0, -step);
					handler.postDelayed(this, 100); // Delay of 100ms for continuous movement
				}
			}
		};

		Runnable moveDownRunnable = new Runnable() {
			@Override
			public void run() {
				if (isMoving) {
					movePointer(0, step);
					handler.postDelayed(this, 100);
				}
			}
		};

		Runnable moveLeftRunnable = new Runnable() {
			@Override
			public void run() {
				if (isMoving) {
					movePointer(-step, 0);
					handler.postDelayed(this, 100);
				}
			}
		};

		Runnable moveRightRunnable = new Runnable() {
			@Override
			public void run() {
				if (isMoving) {
					movePointer(step, 0);
					handler.postDelayed(this, 100);
				}
			}
		};

// Setup touch listeners for each button
		ImageButton upButton = getView().findViewById(R.id.button1);
		upButton.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				isMoving = true;
				handler.post(moveUpRunnable);
			} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				isMoving = false;
			}
			return true;
		});

		ImageButton downButton = getView().findViewById(R.id.button2);
		downButton.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				isMoving = true;
				handler.post(moveDownRunnable);
			} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				isMoving = false;
			}
			return true;
		});

		ImageButton leftButton = getView().findViewById(R.id.button3);
		leftButton.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				isMoving = true;
				handler.post(moveLeftRunnable);
			} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				isMoving = false;
			}
			return true;
		});

		ImageButton rightButton = getView().findViewById(R.id.button4);
		rightButton.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				isMoving = true;
				handler.post(moveRightRunnable);
			} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				isMoving = false;
			}
			return true;
		});

		// no need anymore
//		ImageButton upButton = getView().findViewById(R.id.button1);
//		upButton.setOnClickListener(v -> movePointer(0, -step)); // Move up
//
//		ImageButton downButton = getView().findViewById(R.id.button2);
//		downButton.setOnClickListener(v -> movePointer(0, step)); // Move down
//
//		ImageButton leftButton = getView().findViewById(R.id.button3);
//		leftButton.setOnClickListener(v -> movePointer(-step, 0)); // Move left
//
//		ImageButton rightButton = getView().findViewById(R.id.button4);
//		rightButton.setOnClickListener(v -> movePointer(step, 0)); // Move right

		// simulate the clicking action
		Button clickButton = getView().findViewById(R.id.leftbuttonT9);
		View root_view = getView(); //findViewById(android.R.id.content).getRootView();
		clickButton.setOnClickListener(v -> simulateTouchAtPointerPosition(root_view));

		// right button
		Button clickrightButton = getView().findViewById(R.id.rightbuttonT9);
		//View root_view = getView(); //findViewById(android.R.id.content).getRootView();
		clickrightButton.setOnClickListener(v -> simulateTouchAtPointerPosition(root_view));


	}

	// no need anymore
	public void movePointer(int deltaX, int deltaY) {
		// view.findViewById(R.id.pointer) is fine as well??
		ImageView pointer = getView().findViewById(R.id.pointer);

		pointer.setX(pointer.getX() + deltaX); // Update X position
		pointer.setY(pointer.getY() + deltaY); // Update Y position
	}

	public void simulateTouchAtPointerPosition(View rootView) {
		// Get the current position of the pointer
		ImageView pointer = getView().findViewById(R.id.pointer);

		//Softkey


		float x = pointer.getX();
		float y = pointer.getY();

		// Get the current time in milliseconds
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis();

		// Create the MotionEvent for ACTION_DOWN (finger press)
		// Note: because we have another constraint on the left, it takes into
		// account of 250 dp spacing
		MotionEvent motionEventDown = MotionEvent.obtain(
			downTime, eventTime, MotionEvent.ACTION_DOWN, (x), y, 0
		);

		// Dispatch the touch event (finger press)
		rootView.dispatchTouchEvent(motionEventDown);

		// Create the MotionEvent for ACTION_UP (finger lift)
		MotionEvent motionEventUp = MotionEvent.obtain(
			downTime, eventTime, MotionEvent.ACTION_UP, (x), y, 0
		);

		// Dispatch the touch event (finger lift)
		rootView.dispatchTouchEvent(motionEventUp);

		// Log the coordinates for debugging purposes
		Log.d("SimulatedTouch", "Touch event simulated at X: " + x + ", Y: " + y);

		// Recycle the MotionEvent objects to avoid memory leaks
		motionEventDown.recycle();
		motionEventUp.recycle();
	}

	//
	// using SoftKey method but idk
	//
	public void onPointerMove(float x, float y) {

		// Loop through the keypad keys and check if the pointer is over any key
		for (View key : getKeys()) {
			if (isPointerOverKey(key, x, y)) {
				simulateClick(key);
				break;
			}
		}
	}

	public boolean isPointerOverKey(View key, float pointerX, float pointerY) {
		int[] keyPosition = new int[2];

		key.getLocationOnScreen(keyPosition);

		float keyLeft = keyPosition[0];
		float keyRight = keyLeft + key.getWidth();
		float keyTop = keyPosition[1];
		float keyBottom = keyTop + key.getHeight();

		return (pointerX >= keyLeft && pointerX <= keyRight && pointerY >= keyTop && pointerY <= keyBottom);
	}

	public void simulateClick(View key) {
		// Create a MotionEvent to simulate the touch
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis();

		MotionEvent motionEvent = MotionEvent.obtain(
			downTime, eventTime, MotionEvent.ACTION_DOWN,
			key.getX(), key.getY(), 0
		);

		// Dispatch the touch event to the key
		key.dispatchTouchEvent(motionEvent);

		// Log the key interaction
		Log.d("KeyPress", "Pointer clicked on key: " + key.getId());

		// Create and dispatch the ACTION_UP event to simulate lifting the finger
		motionEvent = MotionEvent.obtain(
			downTime, eventTime, MotionEvent.ACTION_UP,
			key.getX(), key.getY(), 0
		);
		key.dispatchTouchEvent(motionEvent);
	}

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
