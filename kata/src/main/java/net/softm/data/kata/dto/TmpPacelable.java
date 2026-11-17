package net.softm.data.kata.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * VersionDTOOut
 * 기본DTO
 * @author softm 
 */
public class TmpPacelable implements Parcelable {
		private String tmp;

		public TmpPacelable(String tmp) {
			this.tmp = tmp;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(tmp);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		public static final Parcelable.Creator<TmpPacelable> CREATOR = new Parcelable.Creator<TmpPacelable>() {
			public TmpPacelable createFromParcel(Parcel in) {
				return new TmpPacelable(in);
			}

			public TmpPacelable[] newArray(int size) {
				return new TmpPacelable[size];
			}
		};

		private TmpPacelable(Parcel in) {
			tmp = in.readString();
		}
	}
