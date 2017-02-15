package com.gometro.gometropro;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by wprenison on 2015/08/20.
 */
public class AnimExpandLayout extends Animation
{
    private int initialHeight;
    private int targetHeight;
    private View view;

    public AnimExpandLayout(View view, int initialHeight, int targetHeight)
    {
        this.view = view;
        this.targetHeight = targetHeight;
        this.initialHeight = initialHeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t)
    {
        view.getLayoutParams().height = (int) (initialHeight + ((targetHeight - initialHeight) * interpolatedTime));
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight)
    {
        super.initialize(width, height, parentWidth, height);
    }

    @Override
    public boolean willChangeBounds()
    {
        return true;
    }
}
