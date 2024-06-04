package com.broondle.mp3calar.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.broondle.mp3calar.Constants;
import com.broondle.mp3calar.R;
import com.broondle.mp3calar.Util.Managers.AlertManager;
import com.broondle.mp3calar.Util.Managers.PrefManager;
import com.broondle.mp3calar.Util.Managers.ViewManager;
import com.broondle.mp3calar.databinding.FragmentSettingsBinding;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.EntitlementInfo;
import com.revenuecat.purchases.Offerings;
import com.revenuecat.purchases.Package;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.PurchaseCallback;
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback;
import com.revenuecat.purchases.models.StoreTransaction;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    ImageButton noAdsButton,privacyButton;
    TextView noAdsText;
    Package purchasePackage;
    AlertDialog pd;

    PrefManager prefUtil = PrefManager.shared();

    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://boztekno.com/moodyPrivacy.html"));

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        noAdsButton = binding.noAdsButton;
        privacyButton = binding.privacyButton;
        noAdsText = binding.noAdsText;
        pd = AlertManager.shared().createAlertProgress(requireActivity());

        noAdsButton.setEnabled(false);
        init();

        return root;
    }

    private void init(){
        if(!TextUtils.isEmpty(prefUtil.getStringPref(requireActivity(),Constants.noAdsCheckUserPref))){
            if(prefUtil.getStringPref(requireActivity(),Constants.noAdsCheckUserPref).equals("true")){
                noAdsText.setText(requireActivity().getString(R.string.no_ads_purchased));
            }
        }

        try {
            Purchases.getSharedInstance().getOfferings(new ReceiveOfferingsCallback() {
                @Override
                public void onReceived(@NonNull Offerings offerings) {
                    if (offerings.getCurrent() != null){
                        purchasePackage = offerings.getCurrent().getAnnual();
                        noAdsButton.setEnabled(true);
                    }else{
                        Toast.makeText(binding.getRoot().getContext(), "Error: You cant purchase for now, thanks for understanding!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(@NonNull PurchasesError purchasesError) {
                    noAdsButton.setEnabled(false);
                    Log.e("Purchases ",purchasesError.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        noAdsButton.setOnClickListener(view -> {
            if(!pd.isShowing())
                pd.show();

            Purchases.getSharedInstance().purchasePackage((Activity) binding.getRoot().getContext(), purchasePackage, new PurchaseCallback() {
                @Override
                public void onCompleted(@NonNull StoreTransaction storeTransaction, @NonNull CustomerInfo customerInfo) {
                    EntitlementInfo entitlementInfo = customerInfo.getEntitlements().get("noadsstandart");
                    if(entitlementInfo != null)
                        if (entitlementInfo.isActive()){
                            //purchase succeed
                            Log.e("Purchases ", requireActivity().getString(R.string.no_ads_purchased));
                            prefUtil.setStringPref(binding.getRoot().getContext(), Constants.noAdsCheckUserPref,"true");
                            noAdsButton.setEnabled(false);

                            if(pd.isShowing())
                                pd.dismiss();

                            Toast.makeText(binding.getRoot().getContext(), "Successfully purchased subscription! (No Ads)", Toast.LENGTH_SHORT).show();
                        }
                }

                @Override
                public void onError(@NonNull PurchasesError purchasesError, boolean b) {
                    if(pd.isShowing())
                        pd.dismiss();
                    Toast.makeText(binding.getRoot().getContext(), "Cancelled!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        privacyButton.setOnClickListener(view -> {
            startActivity(browserIntent);
        });

    }

}