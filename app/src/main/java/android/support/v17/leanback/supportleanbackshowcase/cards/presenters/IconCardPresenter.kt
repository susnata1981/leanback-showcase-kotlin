package android.support.v17.leanback.supportleanbackshowcase.cards.presenters

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.widget.ImageCardView
import android.view.View

/**
 * Created by susnata on 3/17/18.
 */
class IconCardPresenter(context: Context) : ImageCardViewPresenter(context, R.style.IconCardTheme) {

    override protected fun onCreateView(): ImageCardView {
        val imageCardView = super.onCreateView()
        val image = imageCardView.getMainImageView()
        image.setBackgroundResource(R.drawable.icon_focused)
        image.getBackground().setAlpha(0)
        imageCardView.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus -> animateIconBackground(image.getBackground(), hasFocus) })
        return imageCardView
    }

    private fun animateIconBackground(drawable: Drawable, hasFocus: Boolean) {
        if (hasFocus) {
            ObjectAnimator.ofInt(drawable, "alpha", 0, 255).setDuration(ANIMATION_DURATION.toLong()).start()
        } else {
            ObjectAnimator.ofInt(drawable, "alpha", 255, 0).setDuration(ANIMATION_DURATION.toLong()).start()
        }
    }

    companion object {
        private val ANIMATION_DURATION = 200
    }
}
