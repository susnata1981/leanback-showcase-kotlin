package android.support.v17.leanback.supportleanbackshowcase.cards

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.widget.BaseCardView
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView

/**
 * Created by susnata on 3/17/18.
 */
class CharacterCardView(context: Context) : BaseCardView(context, null, R.style.CharacterCardStyle) {

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.character_card, this)
        onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            val mainImage = findViewById(R.id.main_image) as ImageView
            val container = findViewById(R.id.container) as View;
            if (hasFocus) {
                container.setBackgroundResource(R.drawable.character_focused)
                mainImage.setBackgroundResource(R.drawable.character_focused)
            } else {
                container.setBackgroundResource(R.drawable.character_not_focused_padding)
                mainImage.setBackgroundResource(R.drawable.character_not_focused)
            }
        }
        isFocusable = true
    }

    fun updateUi(card: Card) {
        val primaryText = findViewById(R.id.primary_text) as TextView
        val imageView = findViewById(R.id.main_image) as ImageView

        primaryText.text = card.title
        if (card.getLocalImageResourceName() != null) {
            val resourceId = card.getLocalImageResourceId(context)
            val bitmap = BitmapFactory
                    .decodeResource(context.resources, resourceId)
            val drawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
            drawable.setAntiAlias(true)
            drawable.cornerRadius = Math.max(bitmap.width, bitmap.height) / 2.0f
            imageView.setImageDrawable(drawable)
        }
    }
}