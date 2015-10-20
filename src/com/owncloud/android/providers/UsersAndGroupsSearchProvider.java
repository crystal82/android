/**
 *   ownCloud Android client application
 *
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


package com.owncloud.android.providers;

import android.accounts.Account;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.users.GetRemoteUsersOrGroupsOperation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Content provider for search suggestions, to search for users and groups existing in an ownCloud server.
 */
public class UsersAndGroupsSearchProvider extends ContentProvider {

    private static final String TAG = UsersAndGroupsSearchProvider.class.getSimpleName();

    private static final String[] COLUMNS = {
        BaseColumns._ID,
        SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };

    private static final int SEARCH = 1;

    private static final int RESULTS_PER_PAGE = 50;
    private static final int REQUESTED_PAGE = 1;

    public static final String AUTHORITY = UsersAndGroupsSearchProvider.class.getCanonicalName();
    public static final String ACTION_SHARE_WITH = AUTHORITY + ".action.SHARE_WITH";
    public static final String DATA_USER = AUTHORITY + ".data.user";
    public static final String DATA_GROUP = AUTHORITY + ".data.group";

    private UriMatcher mUriMatcher;

    @Nullable
    @Override
    public String getType(Uri uri) {
        // TODO implement
        return null;
    }

    @Override
    public boolean onCreate() {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
        return true;
    }

    /**
     * TODO description
     *
     * Reference: http://developer.android.com/guide/topics/search/adding-custom-suggestions.html#CustomContentProvider
     *
     * @param uri           Content {@link Uri}, formattted as
     *                      "content://com.owncloud.android.providers.UsersAndGroupsSearchProvider/" +
     *                      {@link android.app.SearchManager#SUGGEST_URI_PATH_QUERY} + "/" + 'userQuery'
     * @param projection    Expected to be NULL.
     * @param selection     Expected to be NULL.
     * @param selectionArgs Expected to be NULL.
     * @param sortOrder     Expected to be NULL.
     * @return              Cursor with users and groups in the ownCloud server that match 'userQuery'.
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log_OC.d(TAG, "query received in thread " + Thread.currentThread().getName());

        int match = mUriMatcher.match(uri);
        switch (match) {
            case SEARCH:
                return searchForUsersOrGroups(uri);

            default:
                return null;
        }
    }

    private Cursor searchForUsersOrGroups(Uri uri) {
        MatrixCursor response = null;


        String userQuery = uri.getLastPathSegment().toLowerCase();


        /// need to trust on the AccountUtils to get the current account since the query in the client side is not
        /// directly started by our code, but from SearchView implementation
        Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());

        /// request to the OC server about users and groups matching userQuery
        GetRemoteUsersOrGroupsOperation searchRequest = new GetRemoteUsersOrGroupsOperation(
                userQuery, REQUESTED_PAGE, RESULTS_PER_PAGE
        );
        RemoteOperationResult result = searchRequest.execute(account, getContext());
        List<Pair<String, Byte>> names = new ArrayList<Pair<String, Byte>>();
        if (result.isSuccess()) {
            for (Object o : result.getData()) {
                names.add((Pair<String, Byte>) o);
            }
        }

        /// convert the responses from the OC server to the expected format
        if (names.size() > 0) {
            response = new MatrixCursor(COLUMNS);
            Iterator<Pair<String, Byte>> namesIt = names.iterator();
            int count = 0;
            Pair<String, Byte> item;
            String displayName;
            Uri dataUri;
            Uri userBaseUri = new Uri.Builder().scheme("content").authority(DATA_USER).build();
            Uri groupBaseUri = new Uri.Builder().scheme("content").authority(DATA_GROUP).build();

            while (namesIt.hasNext()) {
                item = namesIt.next();
                if (GetRemoteUsersOrGroupsOperation.GROUP_TYPE.equals(item.second)) {
                    displayName = getContext().getString(R.string.share_group_clarification, item.first);
                    dataUri = Uri.withAppendedPath(groupBaseUri, item.first);
                } else {
                    displayName = item.first;
                    dataUri = Uri.withAppendedPath(userBaseUri, item.first);
                }
                response.newRow()
                    .add(count++)             // BaseColumns._ID
                    .add(displayName)        // SearchManager.SUGGEST_COLUMN_TEXT_1
                    .add(dataUri);
            }
        }

        return response;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO implementation
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO implementation
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO implementation
        return 0;
    }

}