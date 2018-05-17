/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yomusic.developer.it.yomusic;

import android.os.AsyncTask;

/**
 * Asynchronous task that prepares a Retriever. This asynchronous task essentially calls
 * {@link Retriever#prepareMusicRetriever()} on a {@link Retriever}, which may take some time to
 * run. Upon finishing, it notifies the indicated {@MusicRetrieverPreparedListener}.
 */
public class PrepareRetrieverTask extends AsyncTask<Void, Void, Void> {
    Retriever mRetriever;
    MusicRetrieverPreparedListener mListener;
    String mWhich;

    public PrepareRetrieverTask(Retriever retriever,MusicRetrieverPreparedListener listener,String which) {
        mRetriever = retriever;
        mListener = listener;
        mWhich = which;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        switch (mWhich){
            case "prepareMusicRetriever":
                mRetriever.prepareMusicRetriever();
                break;
            case "prepareFolderRetriever":
                mRetriever.prepareFolderRetriever();
                break;
            case "prepareArtistsRetriever":
                mRetriever.prepareArtistsRetriever();
                break;
            case "prepareAlbumsRetriever":
                mRetriever.prepareAlbumRetriever();
                break;
            case "prepareLastPlayedRetriever":
                mRetriever.prepareLastPlayedRetriever();
                break;
            case "prepareFavouritesRetriever":
                mRetriever.prepareFavouritesRetriever();
                break;
            case "prepareRecentlyAddedRetriever":
                mRetriever.prepareFavouritesRetriever();
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mListener.onMusicRetrieverPrepared();
    }

    public interface MusicRetrieverPreparedListener {
        void onMusicRetrieverPrepared();
    }
}
