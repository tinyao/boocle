/*
 * Copyright 2012 - 2013 Benjamin Weiss
 * Copyright 2012 Neofonie Mobile GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.keyboardsurfer.android.widget.crouton;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/** Builds the default animations for showing and hiding a {@link Crouton}. */
final class DefaultAnimationsBuilder {
  private static final long DURATION = 400;
  private static Animation slideInDownAnimation, slideOutUpAnimation;
  private static Animation slideInDownOverlayAnimation, slideOutUpOverlayAnimation;
  private static int lastInAnimationHeight, lastOutAnimationHeight;

  private DefaultAnimationsBuilder() {
    /* no-op */
  }

  /**
   * @param croutonView
   *   The croutonView which gets animated.
   * @param isOverlay 
   *
   * @return The default Animation for a showing {@link Crouton}.
   */
  static Animation buildDefaultSlideInDownAnimation(View croutonView) {
	  
    if (!areLastMeasuredInAnimationHeightAndCurrentEqual(croutonView) || (null == slideInDownOverlayAnimation)) {
    	slideInDownOverlayAnimation = new TranslateAnimation(
        0, 0,                               // X: from, to
        -croutonView.getMeasuredHeight() , 0 // Y: from, to
      );
      
    	slideInDownOverlayAnimation.setDuration(DURATION);
    	setLastInAnimationHeight(croutonView.getMeasuredHeight());
    }
    return slideInDownOverlayAnimation;
  }
  
  /**
   * @param croutonView
   *   The croutonView which gets animated.
   * @param isOverlay 
   *
   * @return The default Animation for a showing {@link Crouton}.
   */
  static Animation buildDefaultSlideInDownOverlayAnimation(View croutonView) {
	  
	DisplayMetrics displayMetrics = 
			croutonView.getContext().getResources().getDisplayMetrics();
	  
//	int fromY = (int)((-croutonView.getMeasuredHeight() * displayMetrics.density) + 0.5);
	  
    if (!areLastMeasuredInAnimationHeightAndCurrentEqual(croutonView) || (null == slideInDownOverlayAnimation)) {
    	slideInDownOverlayAnimation = new TranslateAnimation(
        0, 0,                               // X: from, to
        -croutonView.getMeasuredHeight() , displayMetrics.density * 48 // Y: from, to
      );
      
      // displayMetrics = context.getResources().getDisplayMetrics();
      // (int)((dp * displayMetrics.density) + 0.5);
      
    	slideInDownOverlayAnimation.setDuration(DURATION);
      setLastInAnimationHeight(croutonView.getMeasuredHeight());
    }
    return slideInDownOverlayAnimation;
  }

  /**
   * @param croutonView
   *   The croutonView which gets animated.
   * @param isOverlay 
   *
   * @return The default Animation for a hiding {@link Crouton}.
   */
  static Animation buildDefaultSlideOutUpAnimation(View croutonView) {
	  
    if (!areLastMeasuredOutAnimationHeightAndCurrentEqual(croutonView) || (null == slideOutUpOverlayAnimation)) {
    	slideOutUpOverlayAnimation = new TranslateAnimation(
        0, 0,                               // X: from, to
        0, -croutonView.getMeasuredHeight() // Y: from, to
      );
      
    	slideOutUpOverlayAnimation.setDuration(DURATION);
      setLastOutAnimationHeight(croutonView.getMeasuredHeight());
    }
    return slideOutUpOverlayAnimation;
  }
  
  /**
   * @param croutonView
   *   The croutonView which gets animated.
   * @param isOverlay 
   *
   * @return The default Animation for a hiding {@link Crouton}.
   */
  static Animation buildDefaultSlideOutUpOverlayAnimation(View croutonView) {
	  
	  DisplayMetrics displayMetrics = 
				croutonView.getContext().getResources().getDisplayMetrics();
	  
    if (!areLastMeasuredOutAnimationHeightAndCurrentEqual(croutonView) || (null == slideOutUpOverlayAnimation)) {
    	slideOutUpOverlayAnimation = new TranslateAnimation(
        0, 0,                               // X: from, to
        displayMetrics.density * 48, -croutonView.getMeasuredHeight() // Y: from, to
      );
      
    	slideOutUpOverlayAnimation.setDuration(DURATION);
      setLastOutAnimationHeight(croutonView.getMeasuredHeight());
    }
    return slideOutUpOverlayAnimation;
  }

  private static boolean areLastMeasuredInAnimationHeightAndCurrentEqual(View croutonView) {
    return areLastMeasuredAnimationHeightAndCurrentEqual(lastInAnimationHeight, croutonView);
  }

  private static boolean areLastMeasuredOutAnimationHeightAndCurrentEqual(View croutonView) {
    return areLastMeasuredAnimationHeightAndCurrentEqual(lastOutAnimationHeight, croutonView);
  }

  private static boolean areLastMeasuredAnimationHeightAndCurrentEqual(int lastHeight, View croutonView) {
    return lastHeight == croutonView.getMeasuredHeight();
  }

  private static void setLastInAnimationHeight(int lastInAnimationHeight) {
    DefaultAnimationsBuilder.lastInAnimationHeight = lastInAnimationHeight;
  }

  private static void setLastOutAnimationHeight(int lastOutAnimationHeight) {
    DefaultAnimationsBuilder.lastOutAnimationHeight = lastOutAnimationHeight;
  }
}
