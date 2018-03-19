package android.support.v17.leanback.supportleanbackshowcase.cards.presenters

import android.content.Context
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.widget.BaseCardView
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * Created by susnata on 3/17/18.
 */
class SideInfoCardPresenter(context: Context) : AbstractCardPresenter<BaseCardView>(context) {

    override protected fun onCreateView(): BaseCardView {
        val cardView = BaseCardView(context, null,
                R.style.SideInfoCardStyle)
        cardView.isFocusable = true
        cardView.addView(LayoutInflater.from(context).inflate(R.layout.side_info_card, null))
        return cardView
    }

    override fun onBindViewHolder(card: Card, cardView: BaseCardView) {
        val imageView = cardView.findViewById(R.id.main_image) as ImageView
        if (card.getLocalImageResourceName() != null) {
            val width = context.getResources()
                    .getDimension(R.dimen.sidetext_image_card_width)
            val height = context.getResources()
                    .getDimension(R.dimen.sidetext_image_card_height)
            val resourceId = context.getResources()
                    .getIdentifier(card.getLocalImageResourceName(),
                            "drawable", context.getPackageName())
            val myOptions = RequestOptions()
                    .override(width.toInt(), height.toInt())
            Glide.with(context)
                    .asBitmap()
                    .load(resourceId)
                    .apply(myOptions)
                    .into(imageView)
        }

        val primaryText = cardView.findViewById(R.id.primary_text) as TextView
        primaryText.text = card.title

        val secondaryText = cardView.findViewById(R.id.secondary_text) as TextView
        secondaryText.text = card.description

        val extraText = cardView.findViewById(R.id.extra_text) as TextView
        extraText.text = card.extraText
    }

}
