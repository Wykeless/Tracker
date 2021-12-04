package com.poe.tracker;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterCollections  extends Filter { //(Pervaiz, 2021)

    //arraylist in which we want to search
    ArrayList<ModelCollection> filterList;

    //adapter in which filet need to be implemented
    AdapterCollections adapterCollections;

    //constructor
    public FilterCollections(ArrayList<ModelCollection> filterList, AdapterCollections adapterCollections) {
        this.filterList = filterList;
        this.adapterCollections = adapterCollections;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if(constraint !=null && constraint.length()>0){
            //change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCollection> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //validate
                if(filterList.get(i).getCollection_name().toUpperCase().contains(constraint)){
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
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filer changes
        adapterCollections.collectionArrayList = (ArrayList<ModelCollection>)results.values;

        //notify changes
        adapterCollections.notifyDataSetChanged();
    }
}

/* Reference list
* Pervaiz, A. (2021). Book App Firebase | 03 Add Book Category | Android Studio | Java. [online]
* www.youtube.com. Available at: https://www.youtube.com/watch?v=TkBos_Flc4k [Accessed 14 Jul. 2021].
* */