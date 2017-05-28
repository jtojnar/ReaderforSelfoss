package apps.amine.bou.readerforselfoss.adapters;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import apps.amine.bou.readerforselfoss.R;
import apps.amine.bou.readerforselfoss.api.selfoss.Item;
import apps.amine.bou.readerforselfoss.api.selfoss.SelfossApi;
import apps.amine.bou.readerforselfoss.api.selfoss.SuccessResponse;
import apps.amine.bou.readerforselfoss.utils.customtabs.CustomTabActivityHelper;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.like.LikeButton;
import com.like.OnLikeListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static apps.amine.bou.readerforselfoss.utils.LinksUtilsKt.buildCustomTabsIntent;
import static apps.amine.bou.readerforselfoss.utils.LinksUtilsKt.openItemUrl;


public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {
    private final boolean clickBehavior;
    private final boolean articleViewer;
    private final boolean internalBrowser;
    private final ColorGenerator generator;
    private SelfossApi api;
    private Context c;
    private List<Item> items;
    private List<Boolean> bars;
    private Activity app;
    private CustomTabActivityHelper helper;

    public ItemListAdapter(Activity a, List<Item> myObject, SelfossApi selfossApi,
            CustomTabActivityHelper mCustomTabActivityHelper, boolean clickBehavior,
            boolean internalBrowser, boolean articleViewer) {
        app = a;
        items = myObject;
        api = selfossApi;
        helper = mCustomTabActivityHelper;
        c = app.getApplicationContext();
        this.clickBehavior = clickBehavior;
        this.internalBrowser = internalBrowser;
        this.articleViewer = articleViewer;
        bars = new ArrayList<>(Collections.nCopies(items.size() + 1, false));
        generator = ColorGenerator.MATERIAL;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(c).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item itm = items.get(position);


        holder.saveBtn.setLiked((itm.getStarred()));
        holder.title.setText(Html.fromHtml(itm.getTitle()));

        String sourceAndDate = itm.getSourcetitle();
        long d;
        try {
            d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(itm.getDatetime()).getTime();
            sourceAndDate += " " +
                    DateUtils.getRelativeTimeSpanString(
                            d,
                            new Date().getTime(),
                            DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_RELATIVE
                    );
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.sourceTitleAndDate.setText(sourceAndDate);

        if (itm.getThumbnail(c).isEmpty()) {
            int sizeInInt = 46;
            int sizeInDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, sizeInInt, c.getResources()
                            .getDisplayMetrics());

            int marginInInt = 16;
            int marginInDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, marginInInt, c.getResources()
                            .getDisplayMetrics());

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.sourceImage.getLayoutParams();
            params.height = sizeInDp;
            params.width = sizeInDp;
            params.setMargins(marginInDp, 0, 0, 0);
            holder.sourceImage.setLayoutParams(params);

            if (itm.getIcon(c).isEmpty()) {
                int color = generator.getColor(itm.getSourcetitle());
                StringBuilder textDrawable = new StringBuilder();
                for(String s : itm.getSourcetitle().split(" "))
                {
                    textDrawable.append(s.charAt(0));
                }

                TextDrawable.IBuilder builder = TextDrawable.builder().round();

                TextDrawable drawable = builder.build(textDrawable.toString(), color);
                holder.sourceImage.setImageDrawable(drawable);
            } else {

                final ViewHolder fHolder = holder;
                Glide.with(c).load(itm.getIcon(c)).asBitmap().centerCrop().into(new BitmapImageViewTarget(holder.sourceImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(c.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        fHolder.sourceImage.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }
        } else {
            Glide.with(c).load(itm.getThumbnail(c)).asBitmap().centerCrop().into(holder.sourceImage);
        }

        if (bars.get(position)) {
            holder.actionBar.setVisibility(View.VISIBLE);
        } else {
            holder.actionBar.setVisibility(View.GONE);
        }

        holder.saveBtn.setLiked(itm.getStarred());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    private void doUnmark(final Item i, final int position) {
        Snackbar s = Snackbar
            .make(app.findViewById(R.id.coordLayout), R.string.marked_as_read, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo_string, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    items.add(position, i);
                    notifyItemInserted(position);

                    api.unmarkItem(i.getId()).enqueue(new Callback<SuccessResponse>() {
                        @Override
                        public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {}

                        @Override
                        public void onFailure(Call<SuccessResponse> call, Throwable t) {
                            items.remove(i);
                            notifyItemRemoved(position);
                            doUnmark(i, position);
                        }
                    });
                }
            });

        View view = s.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        s.show();
    }

    public void removeItemAtIndex(final int position) {

        final Item i = items.get(position);

        items.remove(i);
        notifyItemRemoved(position);

        api.markItem(i.getId()).enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                doUnmark(i, position);

            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
                Toast.makeText(app, app.getString(R.string.cant_mark_read), Toast.LENGTH_SHORT).show();
                items.add(i);
                notifyItemInserted(position);
            }
        });

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LikeButton saveBtn;
        ImageButton browserBtn;
        ImageButton shareBtn;
        public RelativeLayout actionBar;
        ImageView sourceImage;
        TextView title;
        TextView sourceTitleAndDate;
        public ConstraintLayout mView;

        public ViewHolder(ConstraintLayout itemView) {
            super(itemView);
            mView = itemView;
            handleClickListeners();
            handleCustomTabActions();
        }

        private void handleClickListeners() {
            actionBar = (RelativeLayout) mView.findViewById(R.id.actionBar);
            sourceImage = (ImageView) mView.findViewById( R.id.itemImage);
            title = (TextView) mView.findViewById( R.id.title);
            sourceTitleAndDate = (TextView) mView.findViewById( R.id.sourceTitleAndDate);
            saveBtn = (LikeButton) mView.findViewById( R.id.favButton);
            shareBtn = (ImageButton) mView.findViewById( R.id.shareBtn);
            browserBtn = (ImageButton) mView.findViewById( R.id.browserBtn);


            saveBtn.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    Item i = items.get(getAdapterPosition());
                    api.starrItem(i.getId()).enqueue(new Callback<SuccessResponse>() {
                        @Override
                        public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {}

                        @Override
                        public void onFailure(Call<SuccessResponse> call, Throwable t) {
                            saveBtn.setLiked(false);
                            Toast.makeText(c, R.string.cant_mark_favortie, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    Item i = items.get(getAdapterPosition());
                    api.unstarrItem(i.getId()).enqueue(new Callback<SuccessResponse>() {
                        @Override
                        public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {}

                        @Override
                        public void onFailure(Call<SuccessResponse> call, Throwable t) {
                            saveBtn.setLiked(true);
                            Toast.makeText(c, R.string.cant_unmark_favortie, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            shareBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Item i = items.get(getAdapterPosition());
                    Intent sendIntent = new Intent();
                    sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, i.getLinkDecoded());
                    sendIntent.setType("text/plain");
                    c.startActivity(Intent.createChooser(sendIntent, c.getString(R.string.share)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            });

            browserBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Item i = items.get(getAdapterPosition());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(i.getLinkDecoded()));
                    c.startActivity(intent);
                }
            });
        }


        private void handleCustomTabActions() {
            final CustomTabsIntent customTabsIntent = buildCustomTabsIntent(c);
            helper.bindCustomTabsService(app);


            if (!clickBehavior) {
                mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openItemUrl(items.get(getAdapterPosition()),
                            customTabsIntent,
                            internalBrowser,
                            articleViewer,
                            app,
                            c);
                    }
                });
                mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        actionBarShowHide();
                        return true;
                    }
                });
            } else {
                mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        actionBarShowHide();
                    }
                });
                mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        openItemUrl(items.get(getAdapterPosition()),
                                customTabsIntent,
                                internalBrowser,
                                articleViewer,
                                app,
                                c);
                        return true;
                    }
                });
            }
        }

        private void actionBarShowHide() {
            bars.set(getAdapterPosition(), true);
            if (actionBar.getVisibility() == View.GONE)
                actionBar.setVisibility(View.VISIBLE);
            else
                actionBar.setVisibility(View.GONE);
        }
    }
}
