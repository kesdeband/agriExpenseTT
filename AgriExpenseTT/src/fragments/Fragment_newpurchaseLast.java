package fragments;

import helper.DHelper;
import helper.DataManager;
import helper.DbHelper;
import helper.DbQuery;

import java.util.Calendar;
import java.util.Date;

import com.example.agriexpensett.R;
import com.example.agriexpensett.localCycle;
import com.example.agriexpensett.R.id;
import com.example.agriexpensett.R.layout;
import com.example.agriexpensett.rpurchaseendpoint.model.RPurchase;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class Fragment_newpurchaseLast extends Fragment{
	View view;
	EditText et_qty;
	EditText et_cost;
	String category;
	String resource;
	String quantifier;
	localCycle currC=null;
	int resId;
	SQLiteDatabase db;
	DbHelper dbh;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view=inflater.inflate(R.layout.fragment_newpurchase_last, container, false);
		//curr=savedInstanceState.getParcelable("details");
		et_qty=(EditText)view.findViewById(R.id.et_newPurchaselast_qty);
		et_cost=(EditText)view.findViewById(R.id.et_newPurchaselast_cost);
		category=getArguments().getString("category");
		resource=getArguments().getString("resource");
		quantifier=getArguments().getString("quantifier");
		if(category.equals(DHelper.cat_labour)){
			et_qty.setHint("Number of "+quantifier+"'s "+resource+" is going to work");
			et_cost.setHint("Cost of all "+quantifier+"'s "+resource+" will work for");
		}else if(category.equals(DHelper.cat_fertilizer)||category.equals(DHelper.cat_soilAmendment)){
			et_qty.setHint("Number of "+quantifier+"'s of "+resource);
			et_cost.setHint("Cost of all "+quantifier+"s");
		}else if(category.equals(DHelper.cat_chemical)){
			et_qty.setHint("Number of "+quantifier+"'s of "+resource);
			et_cost.setHint("Total cost of all "+resource);
		}else{
			et_qty.setHint("Number of "+resource+" "+quantifier+"s");
			et_cost.setHint("Cost of all "+resource+" "+quantifier+"s");
		}
		dbh=new DbHelper(getActivity().getBaseContext());
		db=dbh.getReadableDatabase();
		
		Button btn_done=(Button)view.findViewById(R.id.btn_newpurchaselast_done);
		resId=DbQuery.getNameResourceId(db, dbh, resource);
		Click c=new Click();
		btn_done.setOnClickListener(c);
		return view;
	}
	public class Click implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.btn_newpurchaselast_done){
				double qty,cost;
				if( ((et_qty.getText().toString()).equals(null))||((et_qty.getText().toString()).equals(""))  ){
					Toast.makeText(getActivity(),"Enter Quantity", Toast.LENGTH_SHORT).show();
					return;
				}else{
					qty=Double.parseDouble(et_qty.getText().toString());
				}
				if( (et_cost.getText().toString().equals(null)) || ((et_cost.getText().toString()).equals("")) ){
					Toast.makeText(getActivity(),"Enter Cost", Toast.LENGTH_SHORT).show();
					return;
				}else{
					cost=Double.parseDouble(et_cost.getText().toString());
				}
				System.out.println("qty "+qty+" cost"+cost);
				DataManager dm=new DataManager(getActivity().getBaseContext(),db,dbh);
				try{
					currC=getArguments().getParcelable("cycle");
				}catch (Exception e){}
				//this is for when labour is 'purchased'/hired for a single cycle
				if(category.equals(DHelper.cat_labour)&&currC!=null){
					//insert purchase
					dm.insertPurchase(resId, quantifier, qty, category, cost);
					int pId=DbQuery.getLast(db, dbh,DbHelper.TABLE_RESOURCE_PURCHASES);
					RPurchase p=DbQuery.getAPurchase(db, dbh, pId);
					//use all of the qty of that purchase in the given cycle
					dm.insertCycleUse(currC.getId(), p.getPId(), qty, p.getType());
					dm.updatePurchase(p.getPId(),(p.getQtyRemaining()-qty));
					//cost=(Double.valueOf(df.format(cost)));
					dm.updateCycleSpent(currC.getId(), currC.getTotalSpent()+cost);
				}else{
					dm.insertPurchase(resId, quantifier, qty, category, cost);
				}
				//dm.insertPurchase(resourceId, quantifier, qty, type, cost);
				getActivity().finish();
			}
		}
		
	}
}