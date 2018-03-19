package android.support.v17.leanback.supportleanbackshowcase.cards.presenters

import android.content.Context
import android.support.v17.leanback.supportleanbackshowcase.cards.CharacterCardView
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import java.security.AccessController.getContext

/**
 * Created by susnata on 3/17/18.
 */
class CharacterCardPresenter(context: Context) : AbstractCardPresenter<CharacterCardView>(context) {

    override protected fun onCreateView(): CharacterCardView {
        return CharacterCardView(context)
    }

    override fun onBindViewHolder(card: Card, cardView: CharacterCardView) {
        cardView.updateUi(card)
    }
}