package com.poe.tracker;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterItems extends Filter {

    //arraylist in which we want to search
    ArrayList<ModelItems> filterList;

    //adapter in which filet need to be implemented
    AdapterItems adapterItems;

    //constructor
    public FilterItems(ArrayList<ModelItems> filterList, AdapterItems adapterItems) {
        this.filterList = filterList;
        this.adapterItems = adapterItems;
    }

    @Override
    protected Filter.FilterResults performFiltering(CharSequence constraint) {
        Filter.FilterResults results = new Filter.FilterResults();
        //value should not be null and empty
        if(constraint !=null && constraint.length()>0){
            //change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelItems> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //validate
                if(filterList.get(i).getItem_name().toUpperCase().contains(constraint)){
                    //add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
        //apply filer changes
        adapterItems.itemsArrayList = (ArrayList<ModelItems>)results.values;

        //notify changes
        adapterItems.notifyDataSetChanged();
    }
}
