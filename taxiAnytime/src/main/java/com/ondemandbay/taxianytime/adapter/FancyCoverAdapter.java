/*
 * Copyright 2013 David Schreiber
 *           2013 John Paul Nalog
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ondemandbay.taxianytime.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.ondemandbay.taxianytime.models.VehicalType;
import com.ondemandbay.taxianytime.R;


public class FancyCoverAdapter extends FancyCoverFlowAdapter {

	// =============================================================================
	// Private members
	// =============================================================================

//	private int[] images = { R.drawable.checkbox, R.drawable.checkbox,
//			R.drawable.checkbox };
	private ImageOptions imageOptions;
//	private AQuery aQuery;
	private ArrayList<VehicalType> listType;
	private Context context;

	// =============================================================================
	// Supertype overrides
	// =============================================================================
	public FancyCoverAdapter(Context context, ArrayList<VehicalType> listType) {
		imageOptions = new ImageOptions();

		imageOptions.fileCache = true;
		imageOptions.memCache = true;
		imageOptions.fallback = R.drawable.ic_launcher;
//		aQuery = new AQuery(context);
		this.context = context;
		this.listType = listType;
	}

	@Override
	public int getCount() {
		// return images.length;
		return listType.size();
	}

	@Override
	public Integer getItem(int i) {
		return i;
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getCoverFlowItem(int i, View reuseableView, ViewGroup viewGroup) {
		// ImageView imageView = null;
		//
		// if (reuseableView != null) {
		// imageView = (ImageView) reuseableView;
		// } else {
		// imageView = new ImageView(viewGroup.getContext());
		// imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		// imageView
		// .setLayoutParams(new FancyCoverFlow.LayoutParams(200, 150));
		//
		// }
		// String icon = listType.get(i).getIcon();
		// AppLog.Log("Fancy", "icon " + i + icon);
		// aQuery.id(imageView).image(icon, imageOptions);
		// imageView.setImageResource(this.getItem(i));

		HolderView holderView;
		View view = reuseableView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) viewGroup.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.vehicle_item, null);
			holderView = new HolderView();
			holderView.ivVehicle = (ImageView) view
					.findViewById(R.id.ivVehicle);
			view.setTag(holderView);
		} else {
			holderView = (HolderView) view.getTag();
		}

		new AQuery(view).id(holderView.ivVehicle).image(
				listType.get(i).getIcon(), imageOptions);
		int width = (int) context.getResources().getDimension(
				R.dimen.image_type_height_width);
		view.setLayoutParams(new FancyCoverFlow.LayoutParams(width, width));
		return view;
	}

	private class HolderView {
		ImageView ivVehicle;
	}
}
