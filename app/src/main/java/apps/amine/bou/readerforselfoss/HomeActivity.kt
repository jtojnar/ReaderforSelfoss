package apps.amine.bou.readerforselfoss

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.CoordinatorLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import apps.amine.bou.readerforselfoss.adapters.ItemCardAdapter
import apps.amine.bou.readerforselfoss.adapters.ItemListAdapter
import apps.amine.bou.readerforselfoss.api.selfoss.Item
import apps.amine.bou.readerforselfoss.api.selfoss.SelfossApi
import apps.amine.bou.readerforselfoss.api.selfoss.Stats
import apps.amine.bou.readerforselfoss.api.selfoss.SuccessResponse
import apps.amine.bou.readerforselfoss.settings.SettingsActivity
import apps.amine.bou.readerforselfoss.utils.Config
import apps.amine.bou.readerforselfoss.utils.checkAndDisplayStoreApk
import apps.amine.bou.readerforselfoss.utils.checkApkVersion
import apps.amine.bou.readerforselfoss.utils.customtabs.CustomTabActivityHelper
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.InviteEvent
import com.github.stkent.amplify.prompt.DefaultLayoutPromptView
import com.github.stkent.amplify.tracking.Amplify
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.roughike.bottombar.BottomBar
import com.roughike.bottombar.BottomBarTab
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeActivity : AppCompatActivity() {


    private val MENU_PREFERENCES = 12302
    private val REQUEST_INVITE = 13231
    private val REQUEST_INVITE_BYMAIL = 13232
    private var mRecyclerView: RecyclerView? = null
    private var api: SelfossApi? = null
    private var items: List<Item>? = null
    private var mCustomTabActivityHelper: CustomTabActivityHelper? = null

    private var clickBehavior = false
    private var internalBrowser = false
    private var articleViewer = false
    private var shouldBeCardView = false
    private var displayUnreadCount = false
    private var displayAllCount = false
    private var editor: SharedPreferences.Editor? = null

    private val UNREAD_SHOWN = 1
    private val READ_SHOWN = 2
    private val FAV_SHOWN = 3
    private var elementsShown: Int = 0
    private var mBottomBar: BottomBar? = null
    private var mCoordinatorLayout: CoordinatorLayout? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var sharedPref: SharedPreferences? = null
    private var tabNew: BottomBarTab? = null
    private var tabArchive: BottomBarTab? = null
    private var tabStarred: BottomBarTab? = null
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private var fullHeightCards: Boolean = false

    private fun handleSharedPrefs() {
        clickBehavior = this.sharedPref!!.getBoolean("tab_on_tap", false)
        internalBrowser = this.sharedPref!!.getBoolean("prefer_internal_browser", true)
        articleViewer = this.sharedPref!!.getBoolean("prefer_article_viewer", true)
        shouldBeCardView = this.sharedPref!!.getBoolean("card_view_active", false)
        displayUnreadCount = this.sharedPref!!.getBoolean("display_unread_count", true)
        displayAllCount = this.sharedPref!!.getBoolean("display_other_count", false)
        fullHeightCards = this.sharedPref!!.getBoolean("full_height_cards", false)
    }

    override fun onResume() {
        super.onResume()

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        val settings = getSharedPreferences(Config.settingsName, Context.MODE_PRIVATE)
        editor = settings.edit()

        if (BuildConfig.GITHUB_VERSION) {
            checkApkVersion(settings, editor!!, this@HomeActivity, mFirebaseRemoteConfig!!)
        }

        handleSharedPrefs()

        tabNew = mBottomBar!!.getTabWithId(R.id.tab_new)
        tabArchive = mBottomBar!!.getTabWithId(R.id.tab_archive)
        tabStarred = mBottomBar!!.getTabWithId(R.id.tab_fav)


        getElementsAccordingToTab()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            val promptView = findViewById(R.id.prompt_view) as DefaultLayoutPromptView

            Amplify.getSharedInstance().promptIfReady(promptView)
        }

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mFirebaseRemoteConfig!!.setDefaults(R.xml.default_remote_config)

        mCustomTabActivityHelper = CustomTabActivityHelper()

        api = SelfossApi(this)
        items = ArrayList()

        mBottomBar = findViewById(R.id.bottomBar) as BottomBar

        // TODO: clean this hack
        val listenerAlreadySet = booleanArrayOf(false)
        mBottomBar!!.setOnTabSelectListener { tabId ->
            if (listenerAlreadySet[0]) {
                if (tabId == R.id.tab_new) {
                    getUnRead()
                } else if (tabId == R.id.tab_archive) {
                    getRead()
                } else if (tabId == R.id.tab_fav) {
                    getStarred()
                }
                getElementsAccordingToTab()
            } else {
                listenerAlreadySet[0] = true
            }
        }

        mCoordinatorLayout = findViewById(R.id.coordLayout) as CoordinatorLayout
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        mRecyclerView = findViewById(R.id.my_recycler_view) as RecyclerView

        reloadLayoutManager()

        mSwipeRefreshLayout!!.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3)
        mSwipeRefreshLayout!!.setOnRefreshListener { getElementsAccordingToTab() }

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun getSwipeDirs(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                if (elementsShown != UNREAD_SHOWN) {
                    return 0
                } else {
                    return super.getSwipeDirs(recyclerView, viewHolder)
                }
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                try {
                    val i = items!![viewHolder.adapterPosition]
                    val position = items!!.indexOf(i)

                    if (shouldBeCardView) {
                        (mRecyclerView!!.adapter as ItemCardAdapter).removeItemAtIndex(position)
                    } else {
                        (mRecyclerView!!.adapter as ItemListAdapter).removeItemAtIndex(position)
                    }
                    tabNew!!.setBadgeCount(items!!.size - 1)

                } catch (e: IndexOutOfBoundsException) {
                    FirebaseCrash.logcat(Log.ERROR, "SWIPE ERROR", "Swipe index out of bound")
                    FirebaseCrash.report(e)
                }

            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)


        checkAndDisplayStoreApk(this@HomeActivity)

    }

    private fun reloadLayoutManager() {
        val mLayoutManager: RecyclerView.LayoutManager
        if (shouldBeCardView) {
            mLayoutManager = StaggeredGridLayoutManager(calculateNoOfColumns(), StaggeredGridLayoutManager.VERTICAL)
            mLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        } else {
            mLayoutManager = GridLayoutManager(this, calculateNoOfColumns())
        }

        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.setHasFixedSize(true)

        mBottomBar!!.setOnTabReselectListener {
            if (shouldBeCardView) {
                if ((mLayoutManager as StaggeredGridLayoutManager).findFirstCompletelyVisibleItemPositions(null)[0] == 0) {
                    getElementsAccordingToTab()
                } else {
                    mLayoutManager.scrollToPositionWithOffset(0, 0)
                }
            } else {
                if ((mLayoutManager as GridLayoutManager).findFirstCompletelyVisibleItemPosition() == 0) {
                    getElementsAccordingToTab()
                } else {
                    mLayoutManager.scrollToPositionWithOffset(0, 0)
                }
            }
        }
    }

    private fun getElementsAccordingToTab() {
        items = ArrayList()

        when (elementsShown) {
            UNREAD_SHOWN -> getUnRead()
            READ_SHOWN -> getRead()
            FAV_SHOWN -> getStarred()
            else -> getUnRead()
        }
    }

    private fun getUnRead() {
        elementsShown = UNREAD_SHOWN
        api!!.unreadItems.enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.body() != null && response.body().isNotEmpty()) {
                    items = response.body()
                } else {
                    items = ArrayList()
                }
                handleListResult()
                mSwipeRefreshLayout!!.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                mSwipeRefreshLayout!!.isRefreshing = false
                Toast.makeText(this@HomeActivity, R.string.cant_get_new_elements, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getRead() {
        elementsShown = READ_SHOWN
        api!!.readItems.enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.body() != null && response.body().isNotEmpty()) {
                    items = response.body()
                } else {
                    items = ArrayList()
                }
                handleListResult()
                mSwipeRefreshLayout!!.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, R.string.cant_get_read, Toast.LENGTH_SHORT).show()
                mSwipeRefreshLayout!!.isRefreshing = false
            }
        })
    }

    private fun getStarred() {
        elementsShown = FAV_SHOWN
        api!!.starredItems.enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.body() != null && response.body().isNotEmpty()) {
                    items = response.body()
                } else {
                    items = ArrayList()
                }
                handleListResult()
                mSwipeRefreshLayout!!.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, R.string.cant_get_favs, Toast.LENGTH_SHORT).show()
                mSwipeRefreshLayout!!.isRefreshing = false
            }
        })
    }

    private fun handleListResult() {
        reloadLayoutManager()

        val mAdapter: RecyclerView.Adapter<*>
        if (shouldBeCardView) {
            mAdapter = ItemCardAdapter(this, items, api, mCustomTabActivityHelper, internalBrowser, articleViewer, fullHeightCards)
        } else {
            mAdapter = ItemListAdapter(this, items, api, mCustomTabActivityHelper, clickBehavior, internalBrowser, articleViewer)
        }
        mRecyclerView!!.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        if (items!!.isEmpty()) Toast.makeText(this@HomeActivity, R.string.nothing_here, Toast.LENGTH_SHORT).show()

        reloadBadges()
    }

    override fun onStart() {
        super.onStart()
        mCustomTabActivityHelper!!.bindCustomTabsService(this)
    }

    override fun onStop() {
        super.onStop()
        mCustomTabActivityHelper!!.unbindCustomTabsService(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> {
                api!!.update().enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        Toast.makeText(this@HomeActivity,
                                R.string.refresh_success_response, Toast.LENGTH_LONG)
                                .show()
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@HomeActivity, R.string.refresh_failer_message, Toast.LENGTH_SHORT).show()
                    }
                })
                Toast.makeText(this, R.string.refresh_in_progress, Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.readAll -> {
                if (elementsShown == UNREAD_SHOWN) {
                    mSwipeRefreshLayout!!.isRefreshing = false
                    val ids = items!!.map { it.id }

                    api!!.readAll(ids).enqueue(object : Callback<SuccessResponse> {
                        override fun onResponse(call: Call<SuccessResponse>, response: Response<SuccessResponse>) {
                            if (response.body() != null && response.body().isSuccess) {
                                Toast.makeText(this@HomeActivity, R.string.all_posts_read, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@HomeActivity, R.string.all_posts_not_read, Toast.LENGTH_SHORT).show()
                            }
                            mSwipeRefreshLayout!!.isRefreshing = false
                        }

                        override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                            Toast.makeText(this@HomeActivity, R.string.all_posts_not_read, Toast.LENGTH_SHORT).show()
                            mSwipeRefreshLayout!!.isRefreshing = false
                        }
                    })
                    items = ArrayList()
                    handleListResult()
                }
                return true
            }
            R.id.action_disconnect -> {
                editor!!.remove("url")
                editor!!.remove("login")
                editor!!.remove("password")
                editor!!.apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            R.id.action_sources -> {
                val intent2 = Intent(this, SourcesActivity::class.java)
                startActivity(intent2)
                return true
            }
            R.id.action_settings -> {
                val intent3 = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent3, MENU_PREFERENCES)
                return true
            }
            R.id.about -> {
                LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .start(this)
                return true
            }
            R.id.action_share_the_app -> {
                if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
                    val share = AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                            .setMessage(getString(R.string.invitation_message))
                            .setDeepLink(Uri.parse("https://ymbh5.app.goo.gl/qbvQ"))
                            .setCallToActionText(getString(R.string.invitation_cta))
                            .build()
                    startActivityForResult(share, REQUEST_INVITE)
                } else {
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.invitation_message) + " https://ymbh5.app.goo.gl/qbvQ")
                    sendIntent.type = "text/plain"
                    startActivityForResult(sendIntent, REQUEST_INVITE_BYMAIL)
                }
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun reloadBadges() {
        if (displayUnreadCount || displayAllCount) {
            api!!.stats.enqueue(object : Callback<Stats> {
                override fun onResponse(call: Call<Stats>, response: Response<Stats>) {
                    if (response.body() != null) {
                        tabNew!!.setBadgeCount(response.body().unread)
                        if (displayAllCount) {
                            tabArchive!!.setBadgeCount(response.body().total)
                            tabStarred!!.setBadgeCount(response.body().starred)
                        } else {
                            tabArchive!!.removeBadge()
                            tabStarred!!.removeBadge()
                        }
                    }
                }

                override fun onFailure(call: Call<Stats>, t: Throwable) {

                }
            })
        } else {
            tabNew!!.removeBadge()
            tabArchive!!.removeBadge()
            tabStarred!!.removeBadge()
        }
    }

    override fun onActivityResult(req: Int, result: Int, data: Intent?) {
        when (req) {
            MENU_PREFERENCES -> recreate()
            REQUEST_INVITE -> if (result == Activity.RESULT_OK) {
                Answers.getInstance().logInvite(InviteEvent())
            }
            REQUEST_INVITE_BYMAIL -> {
                Answers.getInstance().logInvite(InviteEvent())
                super.onActivityResult(req, result, data)
            }
            else -> super.onActivityResult(req, result, data)
        }

    }

    fun calculateNoOfColumns(): Int {
        val displayMetrics = resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return (dpWidth / 300).toInt()
    }
}
