package android.support.v17.leanback.supportleanbackshowcase.cards.presenters

import android.content.Context
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.widget.ImageCardView

/**
 * Created by susnata on 3/17/18.
 */
class SingleLineCardPresenter(context: Context) : ImageCardViewPresenter(context, R.style.SingleLineCardTheme) {

    override fun onBindViewHolder(card: Card, cardView: ImageCardView) {
        super.onBindViewHolder(card, cardView)
        val typedArray = context.getTheme().obtainStyledAttributes(R.styleable.lbImageCardView)
        android.util.Log.d("SHAAN", "lbImageCardViewType =" + typedArray.getInt(R.styleable.lbImageCardView_lbImageCardViewType, -1))
        cardView.setInfoAreaBackgroundColor(card.footerColor)
    }
}
