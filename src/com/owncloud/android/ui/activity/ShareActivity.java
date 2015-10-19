/**
 *   ownCloud Android client application
 *
 *   @author masensio
 *   @author David A. Velasco
 *   Copyright (C) 2015 ownCloud Inc.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.ui.activity;

import android.accounts.Account;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.owncloud.android.R;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.ui.dialog.LoadingDialog;
import com.owncloud.android.ui.fragment.SearchFragment;
import com.owncloud.android.ui.fragment.ShareFileFragment;

/**
 * Activity for sharing files
 */

public class ShareActivity extends AppCompatActivity
        implements ShareFileFragment.OnShareFragmentInteractionListener,
        SearchFragment.OnSearchFragmentInteractionListener {

    private static final String TAG_SHARE_FRAGMENT = "SHARE_FRAGMENT";
    private static final String TAG_SEARCH_FRAGMENT = "SEARCH_USER_AND_GROUPS_FRAGMENT";

    private static final String DIALOG_WAIT_LOAD_DATA = "DIALOG_WAIT_LOAD_DATA";

    private Account mAccount;
    private OCFile mFile;

    private ShareFileFragment mShareFileFragment;
    private SearchFragment mSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (savedInstanceState != null) {
            mFile = savedInstanceState.getParcelable(FileActivity.EXTRA_FILE);
            mAccount = savedInstanceState.getParcelable(FileActivity.EXTRA_ACCOUNT);

            mShareFileFragment = (ShareFileFragment) getSupportFragmentManager().
                    getFragment(savedInstanceState, TAG_SHARE_FRAGMENT);
            mSearchFragment = (SearchFragment) getSupportFragmentManager().
                    getFragment(savedInstanceState, TAG_SEARCH_FRAGMENT);

            if (mShareFileFragment != null){
                ft.replace(R.id.share_fragment_container, mShareFileFragment, TAG_SHARE_FRAGMENT);

                if (mSearchFragment != null){
                    ft.hide(mShareFileFragment);
                    ft.add(R.id.share_fragment_container, mSearchFragment, TAG_SEARCH_FRAGMENT);
                    ft.addToBackStack(TAG_SEARCH_FRAGMENT);
                }
                ft.commit();
            }

        } else {
            // Read Extras
            mFile = getIntent().getParcelableExtra(FileActivity.EXTRA_FILE);
            mAccount = getIntent().getParcelableExtra(FileActivity.EXTRA_ACCOUNT);

            // Add Share fragment
            mShareFileFragment = ShareFileFragment.newInstance(mFile, mAccount);
            ft.replace(R.id.share_fragment_container, mShareFileFragment, TAG_SHARE_FRAGMENT);
            ft.commit();

            mSearchFragment = null;
        }

        handleIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {
        // Verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        // TODO implement
        Toast.makeText(this, "You want to search for [" + query + "]", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FileActivity.EXTRA_FILE, mFile);
        outState.putParcelable(FileActivity.EXTRA_ACCOUNT, mAccount);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, TAG_SHARE_FRAGMENT, mShareFileFragment);
        if (mSearchFragment != null) {
            getSupportFragmentManager().putFragment(outState, TAG_SEARCH_FRAGMENT, mSearchFragment);
        }

    }

    @Override
    public void showSearchUsersAndGroups() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        mSearchFragment = SearchFragment.newInstance(mFile, mAccount);
        ft.hide(mShareFileFragment);
        ft.add(R.id.share_fragment_container, mSearchFragment, TAG_SEARCH_FRAGMENT);
        ft.addToBackStack(TAG_SEARCH_FRAGMENT);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mSearchFragment != null){
            getSupportFragmentManager().popBackStackImmediate();
            mSearchFragment = null;
        }
    }

    @Override
    public void onShareFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSearchFragmentInteraction(Uri uri) {

    }

    /**
     * Show waiting for loading data
     */
    public void showWaitingLoadDialog() {
        // Construct dialog
        LoadingDialog loading = new LoadingDialog(
                getResources().getString(R.string.common_loading));
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        loading.show(ft, DIALOG_WAIT_LOAD_DATA);

    }


    /**
     * Dismiss waiting for loading data
     */
    public void dismissWaitingLoadDialog(){
        Fragment frag = getSupportFragmentManager().findFragmentByTag(DIALOG_WAIT_LOAD_DATA);
        if (frag != null) {
            LoadingDialog loading = (LoadingDialog) frag;
            loading.dismiss();
        }
    }
}