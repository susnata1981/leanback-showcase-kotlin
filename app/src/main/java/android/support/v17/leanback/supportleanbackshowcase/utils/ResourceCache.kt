package android.support.v17.leanback.supportleanbackshowcase.utils

import android.util.SparseArray
import android.view.View

/**
 * Created by susnata on 3/17/18.
 */
class ResourceCache {

    private val mCachedViews = SparseArray<View>()

    fun <ViewType : View> getViewById(view: View, resId: Int): ViewType {
        var child: View? = mCachedViews.get(resId, null)
        if (child == null) {
            child = view.findViewById(resId)
            mCachedViews.put(resId, child)
        }
        return child as ViewType
    }
}
