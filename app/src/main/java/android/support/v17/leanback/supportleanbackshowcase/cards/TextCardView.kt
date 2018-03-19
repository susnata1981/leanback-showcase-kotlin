package android.support.v17.leanback.supportleanbackshowcase.cards;

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.widget.BaseCardView
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView

/**
 * Created by susnata on 3/17/18.
 */
class TextCardView(context: Context) : BaseCardView(context, null, R.style.TextCardStyle) {

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.text_icon_card, this)
        isFocusable = true
    }

    fun updateUi(card: Card) {
        val extraText = findViewById(R.id.extra_text) as TextView
        val primaryText = findViewById(R.id.primary_text) as TextView
        val imageView = findViewById(R.id.main_image) as ImageView

        extraText.text = card.extraText
        primaryText.text = card.title

        // Create a rounded drawable.
        val resourceId = card.getLocalImageResourceId(context)
        val bitmap = BitmapFactory
                .decodeResource(context.resources, resourceId)
        val drawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        drawable.setAntiAlias(true)
        drawable.cornerRadius = Math.max(bitmap.width, bitmap.height) / 2.0f
        imageView.setImageDrawable(drawable)
    }

}

