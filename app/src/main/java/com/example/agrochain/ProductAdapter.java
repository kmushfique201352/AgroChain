package com.example.agrochain;

import static android.content.Intent.getIntent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.bumptech.glide.Glide;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private Context context;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product == null) {
            Log.e("ProductAdapter", "Product at position " + position + " is null.");
            return;
        }

        try {
            if (holder.productName != null) {
                holder.productName.setText(product.getName());
            } else {
                Log.e("ProductAdapter", "productName TextView is null");
            }

            if (holder.productBrand != null) {
                holder.productBrand.setText(product.getBrand());
            } else {
                Log.e("ProductAdapter", "productBrand TextView is null");
            }

            if (holder.productPrice != null) {
                holder.productPrice.setText(product.getPrice());
            } else {
                Log.e("ProductAdapter", "productPrice TextView is null");
            }

            if (holder.productImage != null) {
                Glide.with(context).load(product.getImageUrl()).into(holder.productImage);
            } else {
                Log.e("ProductAdapter", "productImage ImageView is null");
            }
        } catch (Exception e) {
            Log.e("ProductAdapter", "Error setting text or image", e);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent detailIntent = new Intent(context, ProductDetailActivity.class);
            detailIntent.putExtra("PRODUCT", product);
            if (context instanceof Activity) {
                String userID = ((Activity) context).getIntent().getStringExtra("userID");
                detailIntent.putExtra("userID", userID);
//                String userID = getIntent().getStringExtra("userID");
//                Toast.makeText(this, "User:"+userID, Toast.LENGTH_SHORT).show();
            }
            context.startActivity(detailIntent);
        });
    }



    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productBrand, productPrice, productSize, productOldPrice, productDescription;
        ImageView productImage;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productBrand = itemView.findViewById(R.id.productBrand);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImage = itemView.findViewById(R.id.productImage);
            productSize = itemView.findViewById(R.id.productSizeDetail);
            productOldPrice = itemView.findViewById(R.id.productOldPriceDetail);
            productDescription = itemView.findViewById(R.id.productDescriptionDetail);

            Log.d("ProductViewHolder", "Views initialized: " +
                    "productName=" + (productName != null) +
                    ", productBrand=" + (productBrand != null) +
                    ", productPrice=" + (productPrice != null) +
                    ", productImage=" + (productImage != null) +
                    ", productSize=" + (productSize != null) +
                    ", productOldPrice=" + (productOldPrice != null) +
                    ", productDescription=" + (productDescription != null));
        }
    }

}
