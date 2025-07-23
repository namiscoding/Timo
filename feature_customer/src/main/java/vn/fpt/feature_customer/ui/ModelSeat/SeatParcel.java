package vn.fpt.feature_customer.ui.ModelSeat;

import android.os.Parcel;
import android.os.Parcelable;

public class SeatParcel implements Parcelable {
    private String row;
    private int col;
    private boolean active;

    // ... constructor, getter, setter

    protected SeatParcel(Parcel in) {
        row = in.readString();
        col = in.readInt();
        active = in.readByte() != 0;
    }

    public static final Creator<SeatParcel> CREATOR = new Creator<SeatParcel>() {
        @Override
        public SeatParcel createFromParcel(Parcel in) {
            return new SeatParcel(in);
        }

        @Override
        public SeatParcel[] newArray(int size) {
            return new SeatParcel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(row);
        parcel.writeInt(col);
        parcel.writeByte((byte) (active ? 1 : 0));
    }
}