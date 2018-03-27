package com.example.nutri_000.testinggauge;



import android.bluetooth.BluetoothGatt;
import android.os.Parcel;
import android.os.Parcelable;



/**

 * Created by neuronifier on 9/6/2017.

 */



public class BleNotification implements Parcelable {
    float valueX;
    float valueY;
    float valueZ;
    String gatt;



    @Override
    public int describeContents() {
        return 0;
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(valueX);
        dest.writeString(gatt);

        dest.writeFloat(valueY);
        dest.writeFloat(valueZ);
    }

    public static final Parcelable.Creator<BleNotification> CREATOR = new Parcelable.Creator<BleNotification>() {
        public BleNotification createFromParcel(Parcel in) {
            return new BleNotification(in);
        }



        public BleNotification[] newArray(int size) {

            return new BleNotification[size];

        }

    };

    //parcelling part
    private BleNotification(Parcel in) {
        valueX = in.readFloat();
        gatt = in.readString();

        valueY = in.readFloat();
        valueZ = in.readFloat();
    }

    //constructor
   /* public BleNotification(float value, String gatt){
        this.valueX = value;
        this.gatt = gatt;
    }*/

    public BleNotification(float valueX, float valueY, float valueZ, String gatt){
        this.valueX = valueX;
        this.valueY=valueY;
        this.valueZ=valueZ;
        this.gatt=gatt;
    }

}