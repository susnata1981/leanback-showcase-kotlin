package android.support.v17.leanback.supportleanbackshowcase.cards.presenters

import android.content.Context
import android.support.v17.leanback.supportleanbackshowcase.cards.TextCardView
import android.support.v17.leanback.supportleanbackshowcase.models.Card

/**
 * Created by susnata on 3/17/18.
 */
class TextCardPresenter(context: Context) : AbstractCardPresenter<TextCardView>(context) {

    override protected fun onCreateView(): TextCardView {
        return TextCardView(context)
    }

    override fun onBindViewHolder(card: Card, cardView: TextCardView) {
        cardView.updateUi(card)
    }
}
