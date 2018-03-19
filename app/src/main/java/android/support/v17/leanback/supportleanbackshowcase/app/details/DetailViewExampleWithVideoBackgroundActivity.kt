package android.support.v17.leanback.supportleanbackshowcase.app.details

import android.app.Activity
import android.os.Bundle
import android.support.v17.leanback.app.DetailsFragment
import android.support.v17.leanback.supportleanbackshowcase.R

/**
 * Contains a [DetailsFragment] with video background in order to display more details
 * for a given card.
 */

class DetailViewExampleWithVideoBackgroundActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_example)

        if (savedInstanceState == null) {
            val fragment = DetailViewExampleWithVideoBackgroundFragment()
            fragmentManager.beginTransaction()
                    .replace(R.id.details_fragment, fragment)
                    .commit()
        }
    }

    companion object {
        internal val BUY_MOVIE_REQUEST = 987
    }
}
