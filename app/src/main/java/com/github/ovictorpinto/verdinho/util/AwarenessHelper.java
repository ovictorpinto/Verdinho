package com.github.ovictorpinto.verdinho.util;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.util.Log;

import com.github.ovictorpinto.verdinho.BuildConfig;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by victorpinto on 03/07/17.
 */

public class AwarenessHelper {
    private static final String TAG = "Awareness Helper";
    private Context context;

    public AwarenessHelper(Context context) {
        this.context = context;
    }

    public void criaFenda(PontoTO pontoTO) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        AwarenessFence exitFence = LocationFence.exiting(pontoTO.getLatitude(), pontoTO.getLongitude(), 100);
        AwarenessFence inFence = LocationFence.in(pontoTO.getLatitude(), pontoTO.getLongitude(), 100, 5 * 1000);

        Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".proximidade.action");
        intent.putExtra(PontoTO.PARAM_ID, pontoTO.getIdPonto());

        PendingIntent myPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Register the fence to receive callbacks.
        // The fence key uniquely identifies the fence.
        FenceUpdateRequest.Builder builder = new FenceUpdateRequest.Builder();
        builder.addFence("FANCE_IN_" + pontoTO.getIdPonto(), inFence, myPendingIntent);
        builder.addFence("FANCE_OUT_" + pontoTO.getIdPonto(), exitFence, myPendingIntent);

        final FenceUpdateRequest request = builder.build();
        Awareness.getFenceClient(context).updateFences(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Fence was successfully registered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Fence could not be registered: " + e.getMessage());
                    }
                });
    }

    public void removeFenda(PontoTO pontoTO) {
        FenceUpdateRequest.Builder builder = new FenceUpdateRequest.Builder();
        builder.removeFence("FANCE_IN_" + pontoTO.getIdPonto());
        builder.removeFence("FANCE_OUT_" + pontoTO.getIdPonto());
        FenceUpdateRequest request = builder.build();
//        Awareness.FenceApi.updateFences(mGoogleApiClient, request).setResultCallback(new ResultCallbacks<Status>() {
//            @Override
//            public void onSuccess(@NonNull Status status) {
//                Log.i(TAG, "Fence successfully removed.");
//            }
//
//            @Override
//            public void onFailure(@NonNull Status status) {
//                Log.i(TAG, "Fence could NOT be removed.");
//            }
//        });

    }
}
