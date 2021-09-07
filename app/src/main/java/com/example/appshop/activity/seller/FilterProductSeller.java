package com.example.appshop.activity.seller;

import android.widget.Filter;

import com.example.appshop.adapter.AdapterProductSeller;
import com.example.appshop.model.Product;

import java.util.ArrayList;

public class FilterProductSeller extends Filter {

    private AdapterProductSeller adapter;
    private ArrayList<Product> filterList;

    public FilterProductSeller(AdapterProductSeller adapter, ArrayList<Product> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if(constraint != null && constraint.length() > 0){
            //search filed not empty, search something, perform search

            //change to upper case, to make case insensitive
            constraint = constraint.toString().toUpperCase();

            //store our filtered list
            ArrayList<Product> filterModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++){
                //check, search by title and category
                if(filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getProductCategory().toUpperCase().contains(constraint)){
                    filterModels.add(filterList.get(i));
                }
            }

            results.count = filterModels.size();
            results.values = filterModels;
        }
        else {
            //search filed not empty, not searching, return original/all

            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.list = (ArrayList<Product>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
