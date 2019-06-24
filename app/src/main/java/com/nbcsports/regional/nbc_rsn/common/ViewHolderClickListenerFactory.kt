package com.nbcsports.regional.nbc_rsn.common

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit

abstract class ViewHolderClickListenerFactory<in T: Fragment>(
        private val hostFragment: T?,
        protected val team: Team,
        protected val feed: List<FeedComponent>
) {
    fun getClickListener(vh: RecyclerView.ViewHolder): Disposable {
        return RxView.clicks(vh.itemView)
                .observeOn(AndroidSchedulers.mainThread())
                .throttleFirst(500L, TimeUnit.MILLISECONDS)
                .subscribe(getClickListener(vh, hostFragment))
    }

    protected abstract fun getClickListener(vh: RecyclerView.ViewHolder, fragment: T?): ViewHolderClickListener

    fun getLongClickListener(vh: RecyclerView.ViewHolder): Disposable {
        return RxView.longClicks(vh.itemView)
                .observeOn(AndroidSchedulers.mainThread())
                .throttleFirst(500L, TimeUnit.MILLISECONDS)
                .subscribe(ViewHolderShareListener(hostFragment, vh, team))
    }

    fun getExternalLinkClickListener(vh: RecyclerView.ViewHolder, linkProvider: ExternalLinkProvider, intentHelper: IntentHelper): Disposable {
        return RxView.clicks(vh.itemView)
                .observeOn(AndroidSchedulers.mainThread())
                .throttleFirst(500L, TimeUnit.MILLISECONDS)
                .subscribe(ViewHolderExternalLinkListener(linkProvider, intentHelper))
    }
}

abstract class ViewHolderClickListener(
        private val viewHolder: RecyclerView.ViewHolder,
        protected val team: Team,
        protected val mValues: List<FeedComponent>
) : Consumer<Any> {

    final override fun accept(v: Any?) {
        if (!checkViewHolderType(viewHolder)) {
            throw IllegalArgumentException("Wrong ViewHolder type.")
        }

        // Check if data menu ftue is in process and not done yet and is shown long
        // enough if so, marks data menu ftue as done (scenario 5)
        if (!FtueUtil.hasDoneDataMenuFtue()
                && NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()
                && FtueUtil.isDataMenuMsgShownLongEnough()){
            // Set data menu ftue done to true
            FtueUtil.setHasDoneDataMenuFtue(true)
        }

        onViewHolderClicked(viewHolder.adapterPosition)
    }

    protected abstract fun checkViewHolderType(vh: RecyclerView.ViewHolder): Boolean
    protected abstract fun onViewHolderClicked(adapterPos: Int)
}

class ViewHolderShareListener(
        private val hostFragment: Fragment?,
        private val viewHolder: RecyclerView.ViewHolder,
        private val team: Team
) : Consumer<Any> {

    override fun accept(v: Any?) {
        if (viewHolder !is ViewHolderTypeBase) {
            return
        }

        // Check if data menu ftue is in process and not done yet and is shown long
        // enough, if so, marks data menu ftue as done (scenario 5)
        if (!FtueUtil.hasDoneDataMenuFtue()
                && NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()
                && FtueUtil.isDataMenuMsgShownLongEnough()){
            // Set data menu ftue done to true
            FtueUtil.setHasDoneDataMenuFtue(true)
        }

        val shareInfo = NativeShareUtils.generateShareInfo(
                hostFragment,
                viewHolder.mItem,
                viewHolder.mItem.title, null,
                team.teamId)

        if (shareInfo != null) {
            NativeShareUtils.share(shareInfo)
        }

    }
}

class ViewHolderExternalLinkListener(
        private val linkProvider: ExternalLinkProvider,
        private val intentHelper: IntentHelper
) : Consumer<Any> {
    override fun accept(t: Any?) {
        // Check if data menu ftue is in process and not done yet and is shown long
        // enough, if so, marks data menu ftue as done (scenario 5)
        if (!FtueUtil.hasDoneDataMenuFtue()
                && NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()
                && FtueUtil.isDataMenuMsgShownLongEnough()){
            // Set data menu ftue done to true
            FtueUtil.setHasDoneDataMenuFtue(true)
        }

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(linkProvider.getExternalLink()))
        intentHelper.startActivity(browserIntent)
    }
}

interface ExternalLinkProvider {
    fun getExternalLink(): String
}

interface IntentHelper {
    fun startActivity(intent: Intent?)
}