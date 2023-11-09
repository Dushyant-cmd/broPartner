package com.brorental.bropartner.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.brorental.bropartner.MainActivity;
import com.brorental.bropartner.R;
import com.brorental.bropartner.adapters.HintAdapter;
import com.brorental.bropartner.databinding.FragmentSearchBinding;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.localdb.SharedPref;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SearchFragment extends Fragment {
    public FragmentSearchBinding binding;
    private String TAG = "SearchFragment.java", selectedState = "";
    private ArrayList<String> hintList = new ArrayList<>();
    private AppClass appClass;
    private HintAdapter adapter;
    private FirebaseFirestore mFirestore;
    private String[] cateArr;
    private ArrayList<String> stateList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        adapter = new HintAdapter(getActivity(), hintList);
        appClass = (AppClass) getActivity().getApplication();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(adapter);
        mFirestore = ((AppClass) getActivity().getApplication()).firestore;
        stateList.add("Select a state");
        if (Utility.isNetworkAvailable(getActivity())) {
            getCategories();
            getState();
        } else {
            noNetworkDialog();
        }

        binding.searchView.requestFocus();
        setListeners();

        return binding.getRoot();
    }

    private void setListeners() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                int catePosition = Arrays.asList(cateArr).indexOf(query.trim());
                Log.d(TAG, "onQueryTextSubmit: " + catePosition);
                if(catePosition < 0) {
                    DialogCustoms.showSnackBar(getActivity(), "Select Valid Category", binding.getRoot());
                } else
                    adapter.refreshInterface.refresh(catePosition);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (cateArr != null && !newText.isEmpty()) {
                    hintList.clear();
                    for (int i = 0; i < cateArr.length; i++) {
                        if (cateArr[i].toLowerCase().contains(newText.trim())) {
                            hintList.add(cateArr[i]);
                        }
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    hintList.clear();
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        adapter.addRefreshListener(new UtilsInterface.RefreshInterface() {
            /**Below method will refresh list on MainActivity */
            @Override
            public void refresh(int catePosition) {
                if(!stateList.isEmpty()) {
                    try {
                        MainActivity hostAct = (MainActivity) getActivity();
                        hostAct.getData((String) binding.spinner.getSelectedItem(), hintList.get(catePosition));
                        appClass.sharedPref.setState((String) binding.spinner.getSelectedItem());
                        Utility.hideKeyboardFrom(getActivity(), getContext(), binding.getRoot(), true);
                        hostAct.getSupportFragmentManager().popBackStackImmediate();
                    } catch (IndexOutOfBoundsException e) {
                        DialogCustoms.showSnackBar(getActivity(), "Select Valid Category", binding.getRoot());
                    } catch (Exception e) {
                        Log.d(TAG, "refresh: " + e);
                    }
                } else {
                    DialogCustoms.showSnackBar(getActivity(), "Select state", binding.getRoot());
                }


            }
        });
    }

    private void getState() {
        mFirestore.collection("appData").document("constants")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: " + task.getResult().getString("state"));
                            String[] states = task.getResult().getString("state").split(",");
                            Collections.addAll(stateList, states);
                            if(!appClass.sharedPref.getState().isEmpty()) {
                                int selectedStatePos = stateList.indexOf(appClass.sharedPref.getState());
                                stateList.remove(selectedStatePos);
                                stateList.remove(0);
                                stateList.add(0, appClass.sharedPref.getState());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, stateList);
                            binding.spinner.setAdapter(adapter);
                            binding.spinner.setVisibility(View.VISIBLE);
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }

    private void noNetworkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage("No connection");
        builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Utility.isNetworkAvailable(getActivity())) {
                    getCategories();
                    getState();
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    noNetworkDialog();
                }
            }
        });
        builder.create().show();
    }

    private void getCategories() {
        mFirestore.collection("appData").document("constants")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot d = task.getResult();
                            String str = d.getString("categories").toLowerCase();
                            cateArr = str.split(",");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }
}