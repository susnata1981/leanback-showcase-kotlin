package android.support.v17.leanback.supportleanbackshowcase.cards.presenters

import android.content.Context
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.PresenterSelector
import java.util.HashMap

/**
 * Created by susnata on 3/17/18.
 */
class CardPresenterSelector(private val mContext: Context) : PresenterSelector() {
    private val presenters = HashMap<Card.Type?, Presenter>()

    override fun getPresenter(item: Any): Presenter {
        if (item !is Card)
            throw RuntimeException(
                    String.format("The PresenterSelector only supports data items of type '%s'",
                            Card::class.java.name))
        var presenter: Presenter? = presenters[item.type]
        if (presenter == null) {
            when (item.type) {
                Card.Type.SINGLE_LINE -> presenter = SingleLineCardPresenter(mContext)
                Card.Type.VIDEO_GRID -> presenter = VideoCardViewPresenter(mContext, R.style.VideoGridCardTheme)
                Card.Type.MOVIE, Card.Type.MOVIE_BASE, Card.Type.MOVIE_COMPLETE, Card.Type.SQUARE_BIG, Card.Type.GRID_SQUARE, Card.Type.GAME -> {
                    var themeResId = R.style.MovieCardSimpleTheme
                    if (item.type === Card.Type.MOVIE_BASE) {
                        themeResId = R.style.MovieCardBasicTheme
                    } else if (item.type === Card.Type.MOVIE_COMPLETE) {
                        themeResId = R.style.MovieCardCompleteTheme
                    } else if (item.type === Card.Type.SQUARE_BIG) {
                        themeResId = R.style.SquareBigCardTheme
                    } else if (item.type === Card.Type.GRID_SQUARE) {
                        themeResId = R.style.GridCardTheme
                    } else if (item.type === Card.Type.GAME) {
                        themeResId = R.style.GameCardTheme
                    }
                    presenter = ImageCardViewPresenter(mContext, themeResId)
                }
                Card.Type.SIDE_INFO -> presenter = SideInfoCardPresenter(mContext)
                Card.Type.TEXT -> presenter = TextCardPresenter(mContext)
                Card.Type.ICON -> presenter = IconCardPresenter(mContext)
                Card.Type.CHARACTER -> presenter = CharacterCardPresenter(mContext)
                else -> presenter = ImageCardViewPresenter(mContext)
            }
        }
        presenters.put(item.type, presenter)
        return presenter
    }

}
