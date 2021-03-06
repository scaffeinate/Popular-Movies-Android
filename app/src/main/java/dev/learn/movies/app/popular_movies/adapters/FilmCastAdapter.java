package dev.learn.movies.app.popular_movies.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.learn.movies.app.popular_movies.R;
import dev.learn.movies.app.popular_movies.common.cast.Cast;
import dev.learn.movies.app.popular_movies.utils.DisplayUtils;
import dev.learn.movies.app.popular_movies.utils.URIBuilderUtils;

/**
 * FilmCastAdapter - RecyclerView Adapter for Film casts
 */

public class FilmCastAdapter extends RecyclerView.Adapter<FilmCastAdapter.FilmCastHolder> {

    private final OnItemClickHandler mHandler;
    private final int minScreenSize, maxScreenSize;
    private List<Cast> mFilmCastList;

    public FilmCastAdapter(Activity activity, OnItemClickHandler handler) {
        mFilmCastList = new ArrayList<>();
        mHandler = handler;
        int[] screen = DisplayUtils.getScreenMetrics(activity);
        minScreenSize = Math.min(screen[0], screen[1]);
        maxScreenSize = Math.max(screen[0], screen[1]);
    }

    @Override
    public FilmCastHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film_cast, parent, false);
        return new FilmCastHolder(view);
    }

    @Override
    public void onBindViewHolder(FilmCastHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mFilmCastList.size();
    }

    public void setFilmCastList(List<Cast> filmCastList) {
        this.mFilmCastList = filmCastList;
        notifyDataSetChanged();
    }

    class FilmCastHolder extends RecyclerView.ViewHolder {

        private final TextView mActorNameTextView;
        private final TextView mCharacterNameTextView;
        private final ImageView mProfilePicImageView;

        public FilmCastHolder(View itemView) {
            super(itemView);
            mActorNameTextView = itemView.findViewById(R.id.text_view_cast_name);
            mCharacterNameTextView = itemView.findViewById(R.id.text_view_character_name);
            mProfilePicImageView = itemView.findViewById(R.id.image_view_cast_photo);
            adjusPosterSize();
        }

        public void bind(int position) {
            Cast cast = mFilmCastList.get(position);
            if (cast != null) {
                String profilePath = cast.getProfilePath();
                String characterName = cast.getCharacter();
                String actorName = cast.getName();

                DisplayUtils.fitImageInto(mProfilePicImageView,
                        URIBuilderUtils.buildImageResourceUri(profilePath, URIBuilderUtils.IMAGE_SIZE_SMALL));

                if (!TextUtils.isEmpty(actorName)) {
                    mActorNameTextView.setText(actorName);
                }

                if (!TextUtils.isEmpty(characterName)) {
                    mCharacterNameTextView.setText(characterName);
                }
            }
        }

        private void adjusPosterSize() {
            mProfilePicImageView.setLayoutParams(new FrameLayout.LayoutParams((minScreenSize / 3),
                    (int) (maxScreenSize / 3.15)));
        }
    }
}
