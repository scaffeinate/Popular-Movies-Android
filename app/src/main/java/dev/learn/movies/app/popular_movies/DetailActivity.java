package dev.learn.movies.app.popular_movies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nex3z.flowlayout.FlowLayout;

import java.net.URL;
import java.util.List;

import dev.learn.movies.app.popular_movies.common.Genre;
import dev.learn.movies.app.popular_movies.common.MovieDetail;
import dev.learn.movies.app.popular_movies.common.Review;
import dev.learn.movies.app.popular_movies.common.ReviewsResult;
import dev.learn.movies.app.popular_movies.network.HTTPHelper;
import dev.learn.movies.app.popular_movies.network.NetworkLoader;
import dev.learn.movies.app.popular_movies.network.NetworkLoaderCallback;
import dev.learn.movies.app.popular_movies.util.DisplayUtils;

/**
 * DetailActivity - To show the movie details
 */
public class DetailActivity extends AppCompatActivity implements NetworkLoaderCallback, View.OnClickListener {

    public static final String MOVIE_ID = "movie_id";
    public static final String MOVIE_NAME = "movie_name";

    private static final int MOVIE_DETAILS_LOADER_ID = 100;
    private static final int MOVIE_REVIEWS_LOADER_ID = 101;

    private final Gson gson = new Gson();
    private String movieName = "";
    private long movieId = 0L;
    private ConstraintLayout mMovieDetailLayout;
    private AppBarLayout mAppBarLayout;
    private FrameLayout mPosterLayout;
    //private ProgressBar mProgressBar;
    //private TextView mErrorMessageDisplay;
    private ImageView mBackdropImageView;
    private ImageView mPosterImageView;
    private TextView mMovieTitleTextView;
    private TextView mMovieRuntimeTextView;
    private FlowLayout mGenresLayout;
    private TextView mMovieRatingTextView;
    private TextView mNumMovieRatingTextView;
    private TextView mMovieTaglineTextView;
    private TextView mMoviePlotTextView;
    private FloatingActionButton mFavoriteButton;
    private boolean favored = false;
    private NetworkLoader mMovieDetailsLoader;
    private NetworkLoader mMovieReviewsLoader;
    private RatingBar mMovieRatingBar;
    private RecyclerView mReviewsRecyclerView;
    private MovieReviewsAdapter mMovieReviewsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mMovieDetailsLoader = new NetworkLoader(this, this);
        mMovieReviewsLoader = new NetworkLoader(this, this);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        mMovieDetailLayout = findViewById(R.id.layout_movie_detail);
        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mPosterLayout = findViewById(R.id.layout_poster);
        //mProgressBar = findViewById(R.id.pb_loading_indicator);
        //mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);

        mBackdropImageView = findViewById(R.id.image_view_backdrop);
        mPosterImageView = findViewById(R.id.image_view_poster);
        mMovieTitleTextView = findViewById(R.id.tv_movie_title);
        mMovieRuntimeTextView = findViewById(R.id.tv_movie_runtime);
        mGenresLayout = findViewById(R.id.layout_genres);
        mReviewsRecyclerView = findViewById(R.id.rv_user_reviews);
        mMovieRatingTextView = findViewById(R.id.tv_movie_rating);
        mNumMovieRatingTextView = findViewById(R.id.tv_movie_rating_num);
        mMovieTaglineTextView = findViewById(R.id.tv_movie_tagline);
        mMoviePlotTextView = findViewById(R.id.tv_movie_plot);
        mFavoriteButton = findViewById(R.id.btn_fav);
        mMovieRatingBar = findViewById(R.id.rb_movie_rating);

        mFavoriteButton.setOnClickListener(this);

        mMovieReviewsAdapter = new MovieReviewsAdapter();
        mReviewsRecyclerView.setAdapter(mMovieReviewsAdapter);
        mReviewsRecyclerView.setLayoutManager(layoutManager);
        mReviewsRecyclerView.setNestedScrollingEnabled(false);

        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();

        // Show back button in ActionBar
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("");
        }

        /* If savedInstanceState is not null then fetch movieId and movieName
         * Else try to get from Intent Bundle Extra
         */
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.getExtras() != null) {
                Bundle bundle = intent.getExtras();
                movieId = bundle.getLong(MOVIE_ID, 0L);
                movieName = bundle.getString(MOVIE_NAME, "");
            }
        } else {
            movieId = (savedInstanceState.containsKey(MOVIE_ID)) ? savedInstanceState.getLong(MOVIE_ID) : 0L;
            movieName = (savedInstanceState.containsKey(MOVIE_NAME) ? savedInstanceState.getString(MOVIE_NAME) : "");
        }

        adjustImageLayouts();
        if (movieId != 0) {
            fetchMovie();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MOVIE_ID, movieId);
        outState.putString(MOVIE_NAME, movieName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        return true;
    }

    /**
     * Overrides onLoadStarted() from NetworkLoaderCallback
     */
    @Override
    public void onLoadStarted() {
        showProgressBar();
    }

    /**
     * Overrides onLoadFinished() from NetworkLoaderCallback
     *
     * @param s AsyncTask result String
     */
    @Override
    public void onLoadFinished(Loader loader, String s) {
        switch (loader.getId()) {
            case MOVIE_DETAILS_LOADER_ID:
                MovieDetail movieDetail = (s == null) ? null : gson.fromJson(s, MovieDetail.class);
                if (movieDetail == null) {
                    showErrorMessage();
                } else {
                    loadIntoView(movieDetail);
                    showMovieDetails();
                }
                break;
            case MOVIE_REVIEWS_LOADER_ID:
                ReviewsResult reviewsResult = (s == null) ? null : gson.fromJson(s, ReviewsResult.class);
                if (reviewsResult == null || reviewsResult.getResults() == null) {
                    showErrorMessage();
                } else {
                    List<Review> reviewList = reviewsResult.getResults();
                    mMovieReviewsAdapter.setReviewList(reviewList);
                }
                break;
        }
    }

    /**
     * Fetches movie details using the NetworkLoader if Network connection is present.
     * Otherwise shows an error message.
     */
    private void fetchMovie() {
        if (HTTPHelper.isNetworkEnabled(this)) {
            loadMovieDetails();
            loadMovieReviews();
        } else {
            //DisplayUtils.setNoNetworkConnectionMessage(this, mErrorMessageDisplay);
            showErrorMessage();
        }
    }

    private void loadMovieDetails() {
        URL url = HTTPHelper.buildMovieDetailsURL(String.valueOf(movieId));
        Bundle args = new Bundle();
        args.putSerializable(NetworkLoader.URL_EXTRA, url);
        getSupportLoaderManager().initLoader(MOVIE_DETAILS_LOADER_ID, args, mMovieDetailsLoader);
    }

    private void loadMovieReviews() {
        URL url = HTTPHelper.buildMovieReviewsURL(String.valueOf(movieId));
        Bundle args = new Bundle();
        args.putSerializable(NetworkLoader.URL_EXTRA, url);
        getSupportLoaderManager().initLoader(MOVIE_REVIEWS_LOADER_ID, args, mMovieReviewsLoader);
    }

    /**
     * Formats and sets the movie details into appropriate views
     *
     * @param movieDetail MovieDetail Bean
     */
    private void loadIntoView(MovieDetail movieDetail) {
        int year = DisplayUtils.getYear(movieDetail.getReleaseDate());
        double voteAverage = movieDetail.getVoteAverage();
        String backdropURL = movieDetail.getBackdropPath();
        String posterURL = movieDetail.getPosterPath();
        String title = movieDetail.getTitle();
        String runningTime = movieDetail.getRuntime() + " min";
        String rating = String.valueOf(voteAverage);
        String voteCount = "(" + movieDetail.getVoteCount() + ")";
        String tagline = movieDetail.getTagline();
        String moviePlot = movieDetail.getOverview();
        List<Genre> genres = movieDetail.getGenres();

        if (backdropURL != null) {
            Uri backdropUri = HTTPHelper.buildImageResourceUri(backdropURL, HTTPHelper.IMAGE_SIZE_XLARGE);
            DisplayUtils.fitImageInto(mBackdropImageView, backdropUri, null);
        }

        if (posterURL != null) {
            Uri posterUri = HTTPHelper.buildImageResourceUri(posterURL, HTTPHelper.IMAGE_SIZE_SMALL);
            DisplayUtils.fitImageInto(mPosterImageView, posterUri, null);
        }

        mMovieTitleTextView.setText(DisplayUtils.formatTitle(title, year));

        mMovieRuntimeTextView.setText(runningTime);

        DisplayUtils.addGenres(genres, mGenresLayout, this);

        mMovieRatingBar.setRating((float) voteAverage);

        mMovieRatingTextView.setText(rating);

        mNumMovieRatingTextView.setText(voteCount);

        mMovieTaglineTextView.setText(DisplayUtils.formatTagline(this, tagline));

        if (moviePlot != null && !moviePlot.isEmpty()) {
            mMoviePlotTextView.setText(moviePlot);
        }
    }

    /**
     * Shows ProgressBar, Hides ErrorMessage and MovieDetailLayout
     */
    private void showProgressBar() {
        /*mProgressBar.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);*/
        mMovieDetailLayout.setVisibility(View.INVISIBLE);
        mFavoriteButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows MovieDetailLayout, Hides ProgressBar and ErrorMessage
     */
    private void showMovieDetails() {
        mMovieDetailLayout.setVisibility(View.VISIBLE);
        mFavoriteButton.setVisibility(View.VISIBLE);
        /*mProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);*/
    }

    /**
     * Shows ErrorMessage, Hides ProgressBar and MovieDetailLayout
     */
    private void showErrorMessage() {
        /*mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);*/
        mFavoriteButton.setVisibility(View.INVISIBLE);
        mMovieDetailLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * Based on the screen size and orientation scales the parent image layouts.
     */
    private void adjustImageLayouts() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;

        int max = Math.max(screenHeight, screenWidth);
        int min = Math.min(screenHeight, screenWidth);

        //mBackdropLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (max / 2.75)));
        mAppBarLayout.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (max / 2.25)));
        mPosterLayout.setLayoutParams(new ConstraintLayout.LayoutParams((min / 3), (int) (max / 3.5)));
    }

    @Override
    public void onClick(View v) {
        if (favored) {
            mFavoriteButton.setImageResource(R.drawable.ic_heart_outline_white_24dp);
        } else {
            mFavoriteButton.setImageResource(R.drawable.ic_heart_white_24dp);
        }
        favored = !favored;
    }
}
