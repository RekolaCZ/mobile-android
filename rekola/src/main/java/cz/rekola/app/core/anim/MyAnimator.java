package cz.rekola.app.core.anim;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import cz.rekola.app.R;

public class MyAnimator {

	public static void showSlideUp(View view) {
		if (view.getVisibility() != View.VISIBLE) {
			Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_bottom_up);
			view.startAnimation(animation);
		}
		view.setVisibility(View.VISIBLE);
	}

	public static void hideSlideDown(View view) {
		if (view.getVisibility() == View.VISIBLE) {
			Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_bottom_down);
			view.startAnimation(animation);
		}
		view.setVisibility(view.GONE);
	}

	public static void showSlideDown(View view) {
		if (view.getVisibility() != View.VISIBLE) {
			Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_top_down);
			view.startAnimation(animation);
		}
		view.setVisibility(View.VISIBLE);
	}

	public static void hideSlideUp(View view) {
		if (view.getVisibility() == View.VISIBLE) {
			Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_top_up);
			view.startAnimation(animation);
		}
		view.setVisibility(view.GONE);
	}
}
