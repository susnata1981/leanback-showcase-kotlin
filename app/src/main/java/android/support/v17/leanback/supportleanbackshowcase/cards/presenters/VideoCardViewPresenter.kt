package android.support.v17.leanback.supportleanbackshowcase.cards.presenters

import android.content.Context
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.supportleanbackshowcase.models.VideoCard
import android.support.v17.leanback.widget.ImageCardView
import com.bumptech.glide.Glide

/**
 * Created by susnata on 3/17/18.
 */
class VideoCardViewPresenter : ImageCardViewPresenter {

    constructor(context: Context, cardThemeResId: Int) : super(context, cardThemeResId) {}

    constructor(context: Context) : super(context) {}

    override fun onBindViewHolder(card: Card, cardView: ImageCardView) {
        super.onBindViewHolder(card, cardView)
        val videoCard = card as VideoCard
        Glide.with(context)
                .asBitmap()
                .load(videoCard.imageUrl)
                .into(cardView.mainImageView)

    }
}