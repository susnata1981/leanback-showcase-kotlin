package android.support.v17.leanback.supportleanbackshowcase.cards.presenters

import android.content.Context
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.widget.ImageCardView
import android.view.ContextThemeWrapper
import com.bumptech.glide.Glide

/**
 * Created by susnata on 3/17/18.
 */
open class ImageCardViewPresenter @JvmOverloads constructor(context: Context, cardThemeResId: Int = R.style.DefaultCardTheme) : AbstractCardPresenter<ImageCardView>(ContextThemeWrapper(context, cardThemeResId)) {

    override protected fun onCreateView(): ImageCardView {
//        imageCardView.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                Toast.makeText(getContext(), "Clicked on ImageCardView", Toast.LENGTH_SHORT).show();
        //            }
        //        });
        return ImageCardView(context)
    }

    override fun onBindViewHolder(card: Card, cardView: ImageCardView) {
        cardView.tag = card
        cardView.titleText = card.title
        cardView.contentText = card.description
        if (card.getLocalImageResourceName() != null) {
            val resourceId = context.getResources()
                    .getIdentifier(card.getLocalImageResourceName(),
                            "drawable", context.getPackageName())
            Glide.with(context)
                    .asBitmap()
                    .load(resourceId)
                    .into(cardView.mainImageView)
        }
    }

}