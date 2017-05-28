package apps.amine.bou.readerforselfoss.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import apps.amine.bou.readerforselfoss.R;
import apps.amine.bou.readerforselfoss.api.selfoss.SelfossApi;
import apps.amine.bou.readerforselfoss.api.selfoss.Sources;
import apps.amine.bou.readerforselfoss.api.selfoss.SuccessResponse;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class SourcesListAdapter extends RecyclerView.Adapter<SourcesListAdapter.ViewHolder> {
    private final List<Sources> items;
    private final Activity app;
    private final SelfossApi api;
    private final Context c;
    private final ColorGenerator generator;

    public SourcesListAdapter(Activity activity, List<Sources> items, SelfossApi api) {
        this.app = activity;
        this.items = items;
        this.api = api;
        this.c = app.getBaseContext();

        generator = ColorGenerator.MATERIAL;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(c).inflate(R.layout.source_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Sources itm = items.get(position);

        final ViewHolder fHolder = holder;
        if (itm.getIcon(c).isEmpty()) {
            int color = generator.getColor(itm.getTitle());
            StringBuilder textDrawable = new StringBuilder();
            for(String s : itm.getTitle().split(" "))
            {
                textDrawable.append(s.charAt(0));
            }

            TextDrawable.IBuilder builder = TextDrawable.builder().round();

            TextDrawable drawable = builder.build(textDrawable.toString(), color);
            holder.sourceImage.setImageDrawable(drawable);
        } else {
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

        holder.sourceTitle.setText(itm.getTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout mView;
        ImageView sourceImage;
        TextView sourceTitle;
        Button deleteBtn;

        public ViewHolder(ConstraintLayout itemView) {
            super(itemView);
            mView = itemView;

            handleClickListeners();
        }

        private void handleClickListeners() {
            sourceImage = (ImageView) mView.findViewById(R.id.itemImage);
            sourceTitle = (TextView) mView.findViewById(R.id.sourceTitle);
            deleteBtn = (Button) mView.findViewById(R.id.deleteBtn);

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Sources i = items.get(getAdapterPosition());
                    api.deleteSource(i.getId()).enqueue(new Callback<SuccessResponse>() {
                        @Override
                        public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                            if (response.body() != null && response.body().isSuccess()) {
                                items.remove(getAdapterPosition());
                                notifyItemRemoved(getAdapterPosition());
                                notifyItemRangeChanged(getAdapterPosition(), getItemCount());
                            }
                            else {
                                Toast.makeText(app, "Petit soucis lors de la suppression de la source.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<SuccessResponse> call, Throwable t) {
                            Toast.makeText(app, "Petit soucis lors de la suppression de la source.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });


        }
    }
}
