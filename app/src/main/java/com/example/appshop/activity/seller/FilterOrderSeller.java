package com.example.appshop.activity.seller;

import android.widget.Filter;

import com.example.appshop.adapter.AdapterOrderSeller;
import com.example.appshop.adapter.AdapterProductSeller;
import com.example.appshop.model.OrderSeller;
import com.example.appshop.model.Product;

import java.util.ArrayList;

public class FilterOrderSeller extends Filter {

    private AdapterOrderSeller adapter;
    private ArrayList<OrderSeller> filterList;

    public FilterOrderSeller(AdapterOrderSeller adapter, ArrayList<OrderSeller> filterList) {
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
            ArrayList<OrderSeller> filterModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++){
                //check, search by title and category
                if(filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){
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
        adapter.listOrder = (ArrayList<OrderSeller>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
